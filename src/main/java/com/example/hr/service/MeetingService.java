package com.example.hr.service;

import com.example.hr.models.Meeting;
import com.example.hr.models.User;
import com.example.hr.repository.MeetingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final AuthUserHelper authUserHelper;
    private final AuditEncryptionService auditEncryptionService;

    /**
     * Create new meeting
     */
    @Transactional
    public Meeting createMeeting(Meeting meeting) {
        return createMeeting(meeting, null);
    }

    @Transactional
    public Meeting createMeeting(Meeting meeting, Authentication authentication) {
        String encryptedActor = encryptActor(authUserHelper.getCurrentUser(authentication));
        meeting.setCreatedAt(LocalDateTime.now());
        meeting.setUpdatedAt(LocalDateTime.now());
        meeting.setCreatedByEncrypted(encryptedActor);
        meeting.setUpdatedByEncrypted(encryptedActor);
        meeting.setStatus("SCHEDULED");
        return meetingRepository.save(meeting);
    }

    /**
     * Update meeting
     */
    @Transactional
    public Meeting updateMeeting(Integer id, Meeting meetingData) {
        return updateMeeting(id, meetingData, null);
    }

    @Transactional
    public Meeting updateMeeting(Integer id, Meeting meetingData, Authentication authentication) {
        Meeting meeting = getMeetingById(id);
        
        meeting.setTitle(meetingData.getTitle());
        meeting.setDescription(meetingData.getDescription());
        meeting.setScheduledTime(meetingData.getScheduledTime());
        meeting.setDurationMinutes(meetingData.getDurationMinutes());
        meeting.setLocation(meetingData.getLocation());
        meeting.setMeetingLink(meetingData.getMeetingLink());
        meeting.setAgenda(meetingData.getAgenda());
        meeting.setUpdatedAt(LocalDateTime.now());
        meeting.setUpdatedByEncrypted(encryptActor(authUserHelper.getCurrentUser(authentication)));
        
        return meetingRepository.save(meeting);
    }

    /**
     * Complete meeting with notes
     */
    @Transactional
    public Meeting completeMeeting(Integer id, String notes, String actionItems) {
        Meeting meeting = getMeetingById(id);
        
        meeting.setNotes(notes);
        meeting.setActionItems(actionItems);
        meeting.setStatus("COMPLETED");
        meeting.setUpdatedAt(LocalDateTime.now());
        
        return meetingRepository.save(meeting);
    }

    /**
     * Cancel meeting
     */
    @Transactional
    public void cancelMeeting(Integer id) {
        Meeting meeting = getMeetingById(id);
        meeting.setStatus("CANCELLED");
        meeting.setUpdatedAt(LocalDateTime.now());
        meetingRepository.save(meeting);
    }

    /**
     * Get meeting by ID
     */
    public Meeting getMeetingById(Integer id) {
        return meetingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Meeting not found with id: " + id));
    }

    /**
     * Get all meetings for user
     */
    public List<Meeting> getMeetingsForUser(User user) {
        return meetingRepository.findByUserInvolved(user);
    }

    /**
     * Get upcoming meetings
     */
    public List<Meeting> getUpcomingMeetings(User organizer) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime futureDate = now.plusMonths(1);
        return meetingRepository.findUpcomingMeetings(organizer, now, futureDate);
    }

    /**
     * Get meetings by type
     */
    public List<Meeting> getMeetingsByType(String meetingType, User organizer) {
        return meetingRepository.findByMeetingTypeAndOrganizerOrderByScheduledTimeDesc(meetingType, organizer);
    }

    /**
     * Get all meetings
     */
    public List<Meeting> getAllMeetings() {
        return meetingRepository.findAll();
    }

    /**
     * Get meeting statistics
     */
    public MeetingStatistics getMeetingStatistics(User organizer) {
        long totalMeetings = meetingRepository.findByOrganizerOrderByScheduledTimeDesc(organizer).size();
        long completedMeetings = meetingRepository.countByOrganizerAndStatus(organizer, "COMPLETED");
        long scheduledMeetings = meetingRepository.countByOrganizerAndStatus(organizer, "SCHEDULED");
        long cancelledMeetings = meetingRepository.countByOrganizerAndStatus(organizer, "CANCELLED");
        
        return new MeetingStatistics(
                totalMeetings,
                completedMeetings,
                scheduledMeetings,
                cancelledMeetings
        );
    }

    public record MeetingStatistics(
            long totalMeetings,
            long completedMeetings,
            long scheduledMeetings,
            long cancelledMeetings
    ) {}

    private String encryptActor(User actor) {
        if (actor == null) {
            return auditEncryptionService.encrypt("Khong xac dinh");
        }
        String payload = String.format("%s | %s | %s",
                actor.getUsername(),
                actor.getFullName(),
                actor.getRole() != null ? actor.getRole().name() : "NO_ROLE");
        return auditEncryptionService.encrypt(payload);
    }
}


