package com.example.hr.kafka.consumer;

import com.example.hr.kafka.events.LeaveRequestEvent;
import com.example.hr.kafka.events.NotificationEvent;
import com.example.hr.kafka.producer.HREventProducer;
import com.example.hr.enums.LeaveType;
import com.example.hr.enums.BalanceOperation;
import com.example.hr.models.User;
import com.example.hr.user.repository.UserRepository;
import com.example.hr.leave.service.AdvancedLeaveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;

/**
 * Consumer cho Leave Request Events
 * Xử lý các sự kiện đơn xin nghỉ phép
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LeaveRequestEventConsumer {

    private final HREventProducer eventProducer;
    private final UserRepository userRepository;
    private final AdvancedLeaveService advancedLeaveService;

    @KafkaListener(topics = "${kafka.topics.leave-requests}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeLeaveRequestEvent(LeaveRequestEvent event) {
        try {
            log.info("Received leave request event: userId={}, eventType={}, leaveType={}", 
                    event.getUserId(), event.getEventType(), event.getLeaveType());
            
            switch (event.getEventType()) {
                case "SUBMITTED":
                    handleLeaveSubmitted(event);
                    break;
                case "APPROVED":
                    handleLeaveApproved(event);
                    break;
                case "REJECTED":
                    handleLeaveRejected(event);
                    break;
                case "CANCELLED":
                    handleLeaveCancelled(event);
                    break;
                default:
                    log.warn("Unknown leave request event type: {}", event.getEventType());
            }
            
        } catch (Exception e) {
            log.error("Error processing leave request event: userId={}", event.getUserId(), e);
            throw new IllegalStateException("Failed to process leave request event", e);
        }
    }

    private void handleLeaveSubmitted(LeaveRequestEvent event) {
        log.info("Processing leave submission: user={}, days={}", 
                event.getFullName(), event.getTotalDays());
        
        // Gửi notification cho manager
        NotificationEvent notification = new NotificationEvent(
                "EMAIL",
                Collections.singletonList(event.getApprovedBy()),
                "Đơn xin nghỉ phép mới",
                String.format("%s đã gửi đơn xin nghỉ phép %s từ %s đến %s (%s ngày)",
                        event.getFullName(), event.getLeaveType(), 
                        event.getStartDate(), event.getEndDate(), event.getTotalDays()),
                "MEDIUM",
                "LEAVE",
                event.getLeaveRequestId(),
                "LEAVE_REQUEST",
                LocalDateTime.now()
        );
        eventProducer.publishNotificationEvent(notification);
    }

    private void handleLeaveApproved(LeaveRequestEvent event) {
        log.info("Processing leave approval: user={}, approver={}", 
                event.getFullName(), event.getApproverName());

        User user = userRepository.findById(event.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + event.getUserId()));
        advancedLeaveService.updateLeaveBalance(
                user,
                LeaveType.valueOf(event.getLeaveType()),
                event.getStartDate().getYear(),
                event.getTotalDays(),
                BalanceOperation.ADD_USED_DAYS
        );
        
        // Gửi notification cho employee
        NotificationEvent notification = new NotificationEvent(
                "EMAIL",
                Collections.singletonList(event.getUserId()),
                "Đơn nghỉ phép đã được duyệt",
                String.format("Đơn nghỉ phép của bạn từ %s đến %s đã được %s phê duyệt",
                        event.getStartDate(), event.getEndDate(), event.getApproverName()),
                "HIGH",
                "LEAVE",
                event.getLeaveRequestId(),
                "LEAVE_REQUEST",
                LocalDateTime.now()
        );
        eventProducer.publishNotificationEvent(notification);
    }

    private void handleLeaveRejected(LeaveRequestEvent event) {
        log.info("Processing leave rejection: user={}, approver={}", 
                event.getFullName(), event.getApproverName());
        
        // Gửi notification cho employee
        NotificationEvent notification = new NotificationEvent(
                "EMAIL",
                Collections.singletonList(event.getUserId()),
                "Đơn nghỉ phép bị từ chối",
                String.format("Đơn nghỉ phép của bạn từ %s đến %s đã bị %s từ chối. Lý do: %s",
                        event.getStartDate(), event.getEndDate(), 
                        event.getApproverName(), event.getReason()),
                "HIGH",
                "LEAVE",
                event.getLeaveRequestId(),
                "LEAVE_REQUEST",
                LocalDateTime.now()
        );
        eventProducer.publishNotificationEvent(notification);
    }

    private void handleLeaveCancelled(LeaveRequestEvent event) {
        log.info("Processing leave cancellation: user={}", event.getFullName());
        NotificationEvent notification = new NotificationEvent(
                "IN_APP",
                Collections.singletonList(event.getUserId()),
                "Don nghi phep da huy",
                String.format("Don nghi phep %s tu %s den %s da duoc huy",
                        event.getLeaveType(), event.getStartDate(), event.getEndDate()),
                "MEDIUM",
                "LEAVE",
                event.getLeaveRequestId(),
                "LEAVE_REQUEST",
                LocalDateTime.now()
        );
        eventProducer.publishNotificationEvent(notification);
    }
}
