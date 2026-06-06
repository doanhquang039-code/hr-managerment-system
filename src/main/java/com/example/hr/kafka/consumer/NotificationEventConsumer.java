package com.example.hr.kafka.consumer;


import com.example.hr.payroll.entity.Payroll;
import com.example.hr.enums.NotificationType;
import com.example.hr.kafka.events.NotificationEvent;
import com.example.hr.models.User;
import com.example.hr.user.repository.UserRepository;
import com.example.hr.service.EmailService;
import com.example.hr.service.FirebaseNotificationService;
import com.example.hr.service.NotificationService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationEventConsumer {

    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;
    private final ObjectProvider<FirebaseNotificationService> firebaseNotificationServiceProvider;

    @KafkaListener(topics = "${kafka.topics.notifications}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeNotificationEvent(NotificationEvent event) {
        try {
            validateRecipients(event);
            log.info("Received notification event: type={}, category={}, recipients={}",
                    event.getNotificationType(), event.getCategory(), event.getRecipientUserIds().size());

            switch (safe(event.getNotificationType()).toUpperCase()) {
                case "EMAIL" -> sendEmailNotification(event);
                case "SMS" -> sendSMSNotification(event);
                case "PUSH" -> sendPushNotification(event);
                case "IN_APP" -> sendInAppNotification(event);
                default -> throw new IllegalArgumentException("Unknown notification type: " + event.getNotificationType());
            }
        } catch (Exception e) {
            log.error("Error processing notification event: category={}", event.getCategory(), e);
            throw new IllegalStateException("Failed to process notification event", e);
        }
    }

    private void sendEmailNotification(NotificationEvent event) {
        for (User user : recipients(event)) {
            if (user.getEmail() == null || user.getEmail().isBlank()) {
                log.warn("Skip email notification for user {} because email is empty", user.getId());
                continue;
            }
            try {
                emailService.sendNotificationEmail(user.getEmail(), user.getFullName(), event.getSubject(), event.getMessage());
                notificationService.createNotification(user, event.getMessage(), toNotificationType(event), linkFor(event));
            } catch (MessagingException e) {
                throw new IllegalStateException("Email notification failed for user " + user.getId(), e);
            }
        }
    }

    private void sendSMSNotification(NotificationEvent event) {
        for (User user : recipients(event)) {
            notificationService.createNotification(user, "[SMS] " + event.getMessage(), toNotificationType(event), linkFor(event));
        }
    }

    private void sendPushNotification(NotificationEvent event) {
        FirebaseNotificationService pushService = firebaseNotificationServiceProvider.getIfAvailable();
        for (User user : recipients(event)) {
            if (pushService != null && user.getFcmToken() != null && !user.getFcmToken().isBlank()) {
                pushService.sendToDevice(user.getFcmToken(), event.getSubject(), event.getMessage(), Map.of(
                        "category", safe(event.getCategory()),
                        "entityType", safe(event.getRelatedEntityType()),
                        "entityId", event.getRelatedEntityId() != null ? event.getRelatedEntityId().toString() : ""
                ));
            }
            notificationService.createNotification(user, event.getMessage(), toNotificationType(event), linkFor(event));
        }
    }

    private void sendInAppNotification(NotificationEvent event) {
        for (User user : recipients(event)) {
            notificationService.createNotification(user, event.getMessage(), toNotificationType(event), linkFor(event));
        }
    }

    private void validateRecipients(NotificationEvent event) {
        if (event.getRecipientUserIds() == null || event.getRecipientUserIds().isEmpty()) {
            throw new IllegalArgumentException("Notification event has no recipients");
        }
    }

    private List<User> recipients(NotificationEvent event) {
        List<User> users = userRepository.findAllById(event.getRecipientUserIds());
        if (users.size() != event.getRecipientUserIds().size()) {
            log.warn("Notification recipients resolved partially: requested={}, found={}",
                    event.getRecipientUserIds().size(), users.size());
        }
        return users;
    }

    private NotificationType toNotificationType(NotificationEvent event) {
        return switch (safe(event.getCategory()).toUpperCase()) {
            case "ATTENDANCE" -> NotificationType.ATTENDANCE;
            case "LEAVE" -> NotificationType.LEAVE_REQUEST;
            case "PAYROLL" -> NotificationType.PAYROLL;
            default -> switch (safe(event.getPriority()).toUpperCase()) {
                case "HIGH" -> NotificationType.WARNING;
                default -> NotificationType.INFO;
            };
        };
    }

    private String linkFor(NotificationEvent event) {
        return switch (safe(event.getCategory()).toUpperCase()) {
            case "ATTENDANCE" -> "/user/attendance";
            case "LEAVE" -> "/user/leaves";
            case "PAYROLL" -> "/user1/payroll";
            case "RECRUITMENT" -> "/hiring/dashboard";
            case "PERFORMANCE" -> "/user1/reviews";
            case "TRAINING" -> "/lms/my-courses";
            default -> "/notifications";
        };
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
