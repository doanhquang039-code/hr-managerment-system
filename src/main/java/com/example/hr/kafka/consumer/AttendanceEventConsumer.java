package com.example.hr.kafka.consumer;

import com.example.hr.enums.AttendanceStatus;
import com.example.hr.kafka.events.AttendanceEvent;
import com.example.hr.kafka.events.NotificationEvent;
import com.example.hr.kafka.producer.HREventProducer;
import com.example.hr.attendance.entity.Attendance;
import com.example.hr.models.User;
import com.example.hr.attendance.repository.AttendanceRepository;
import com.example.hr.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceEventConsumer {

    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;
    private final HREventProducer eventProducer;

    @KafkaListener(topics = "${kafka.topics.attendance}", groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void consumeAttendanceEvent(AttendanceEvent event) {
        try {
            log.info("Received attendance event: userId={}, eventType={}, timestamp={}",
                    event.getUserId(), event.getEventType(), event.getTimestamp());

            switch (event.getEventType()) {
                case "CHECK_IN" -> handleCheckIn(event);
                case "CHECK_OUT" -> handleCheckOut(event);
                case "LATE" -> handleLateArrival(event);
                case "EARLY_LEAVE" -> handleEarlyLeave(event);
                case "ABSENT" -> handleAbsent(event);
                default -> throw new IllegalArgumentException("Unknown attendance event type: " + event.getEventType());
            }
        } catch (Exception e) {
            log.error("Error processing attendance event: userId={}", event.getUserId(), e);
            throw new IllegalStateException("Failed to process attendance event", e);
        }
    }

    private void handleCheckIn(AttendanceEvent event) {
        Attendance attendance = findOrCreateAttendance(event);
        attendance.setCheckInTime(eventTime(event));
        if (attendance.getStatus() == null || attendance.getStatus() == AttendanceStatus.ABSENT) {
            attendance.setStatus(AttendanceStatus.PRESENT);
        }
        attendance.setNote(buildNote(event, "Check-in"));
        attendanceRepository.save(attendance);
    }

    private void handleCheckOut(AttendanceEvent event) {
        Attendance attendance = findOrCreateAttendance(event);
        attendance.setCheckOutTime(eventTime(event));
        attendance.setNote(buildNote(event, "Check-out"));
        attendanceRepository.save(attendance);
    }

    private void handleLateArrival(AttendanceEvent event) {
        Attendance attendance = findOrCreateAttendance(event);
        attendance.setCheckInTime(eventTime(event));
        attendance.setStatus(AttendanceStatus.LATE);
        attendance.setNote(buildNote(event, "Late arrival"));
        attendanceRepository.save(attendance);
        publishAttendanceNotification(event, "Di muon", "Ban da duoc ghi nhan di muon hom nay.");
    }

    private void handleEarlyLeave(AttendanceEvent event) {
        Attendance attendance = findOrCreateAttendance(event);
        attendance.setCheckOutTime(eventTime(event));
        attendance.setStatus(AttendanceStatus.EARLY_LEAVE);
        attendance.setNote(buildNote(event, "Early leave"));
        attendanceRepository.save(attendance);
        publishAttendanceNotification(event, "Ve som", "Ban da duoc ghi nhan ve som hom nay.");
    }

    private void handleAbsent(AttendanceEvent event) {
        Attendance attendance = findOrCreateAttendance(event);
        attendance.setStatus(AttendanceStatus.ABSENT);
        attendance.setNote(buildNote(event, "Absent"));
        attendanceRepository.save(attendance);
        publishAttendanceNotification(event, "Vang mat", "Ban da duoc ghi nhan vang mat hom nay.");
    }

    private Attendance findOrCreateAttendance(AttendanceEvent event) {
        User user = userRepository.findById(event.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + event.getUserId()));
        LocalDate date = event.getTimestamp() != null ? event.getTimestamp().toLocalDate() : LocalDate.now();
        return attendanceRepository.findByUserAndAttendanceDate(user, date)
                .orElseGet(() -> {
                    Attendance attendance = new Attendance();
                    attendance.setUser(user);
                    attendance.setAttendanceDate(date);
                    attendance.setCreatedAt(LocalDateTime.now());
                    return attendance;
                });
    }

    private LocalTime eventTime(AttendanceEvent event) {
        return event.getTimestamp() != null ? event.getTimestamp().toLocalTime() : LocalTime.now();
    }

    private String buildNote(AttendanceEvent event, String action) {
        StringBuilder note = new StringBuilder(action);
        if (event.getLocation() != null && !event.getLocation().isBlank()) {
            note.append(" at ").append(event.getLocation());
        }
        if (event.getDeviceInfo() != null && !event.getDeviceInfo().isBlank()) {
            note.append(" via ").append(event.getDeviceInfo());
        }
        if (event.getNotes() != null && !event.getNotes().isBlank()) {
            note.append(". ").append(event.getNotes());
        }
        return note.toString();
    }

    private void publishAttendanceNotification(AttendanceEvent event, String subject, String message) {
        NotificationEvent notification = new NotificationEvent(
                "IN_APP",
                List.of(event.getUserId()),
                subject,
                message,
                "MEDIUM",
                "ATTENDANCE",
                event.getAttendanceId(),
                "ATTENDANCE",
                LocalDateTime.now()
        );
        eventProducer.publishNotificationEvent(notification);
    }
}
