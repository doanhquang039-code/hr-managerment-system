package com.example.hr.kafka.consumer;

import com.example.hr.kafka.events.NotificationEvent;
import com.example.hr.kafka.events.RecruitmentEvent;
import com.example.hr.kafka.producer.HREventProducer;
import com.example.hr.recruitment.service.CandidateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;

/**
 * Consumer cho Recruitment Events
 * Xử lý các sự kiện tuyển dụng
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RecruitmentEventConsumer {

    private final CandidateService candidateService;
    private final HREventProducer eventProducer;

    @KafkaListener(topics = "${kafka.topics.recruitment}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeRecruitmentEvent(RecruitmentEvent event) {
        try {
            log.info("Received recruitment event: applicationId={}, eventType={}, candidate={}", 
                    event.getApplicationId(), event.getEventType(), event.getCandidateName());
            
            switch (event.getEventType()) {
                case "APPLICATION_RECEIVED":
                    handleApplicationReceived(event);
                    break;
                case "SCREENING":
                    handleScreening(event);
                    break;
                case "INTERVIEW_SCHEDULED":
                    handleInterviewScheduled(event);
                    break;
                case "OFFER_SENT":
                    handleOfferSent(event);
                    break;
                case "HIRED":
                    handleHired(event);
                    break;
                case "REJECTED":
                    handleRejected(event);
                    break;
                default:
                    log.warn("Unknown recruitment event type: {}", event.getEventType());
            }
            
        } catch (Exception e) {
            log.error("Error processing recruitment event: applicationId={}", 
                    event.getApplicationId(), e);
            throw new IllegalStateException("Failed to process recruitment event", e);
        }
    }

    private void handleApplicationReceived(RecruitmentEvent event) {
        log.info("Processing application received: candidate={}, job={}", 
                event.getCandidateName(), event.getJobTitle());
        candidateService.moveToStage(event.getApplicationId(), "APPLIED");
    }

    private void handleScreening(RecruitmentEvent event) {
        log.info("Processing screening: candidate={}", event.getCandidateName());
        candidateService.moveToStage(event.getApplicationId(), "SCREENING");
    }

    private void handleInterviewScheduled(RecruitmentEvent event) {
        log.info("Processing interview scheduled: candidate={}, interviewer={}, date={}", 
                event.getCandidateName(), event.getInterviewerName(), event.getInterviewDate());
        candidateService.moveToStage(event.getApplicationId(), "INTERVIEW");
        if (event.getInterviewerId() != null) {
            publishRecruitmentNotification(event, event.getInterviewerId(), "Lich phong van moi",
                    String.format("Phong van ung vien %s cho vi tri %s luc %s",
                            event.getCandidateName(), event.getJobTitle(), event.getInterviewDate()));
        }
    }

    private void handleOfferSent(RecruitmentEvent event) {
        log.info("Processing offer sent: candidate={}, job={}", 
                event.getCandidateName(), event.getJobTitle());
        candidateService.moveToStage(event.getApplicationId(), "OFFER");
    }

    private void handleHired(RecruitmentEvent event) {
        log.info("Processing hired: candidate={}, job={}", 
                event.getCandidateName(), event.getJobTitle());
        candidateService.hireCandidate(event.getApplicationId());
    }

    private void handleRejected(RecruitmentEvent event) {
        log.info("Processing rejection: candidate={}", event.getCandidateName());
        candidateService.rejectCandidate(event.getApplicationId(), "Rejected by recruitment workflow");
    }

    private void publishRecruitmentNotification(RecruitmentEvent event, Integer userId, String subject, String message) {
        NotificationEvent notification = new NotificationEvent(
                "IN_APP",
                Collections.singletonList(userId),
                subject,
                message,
                "MEDIUM",
                "RECRUITMENT",
                event.getApplicationId(),
                "CANDIDATE",
                LocalDateTime.now()
        );
        eventProducer.publishNotificationEvent(notification);
    }
}
