package com.example.hr.kafka.consumer;


import com.example.hr.department.entity.Department;
import com.example.hr.kafka.events.EmployeeLifecycleEvent;
import com.example.hr.kafka.events.NotificationEvent;
import com.example.hr.kafka.producer.HREventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;

/**
 * Consumer cho Employee Lifecycle Events
 * Xử lý các sự kiện vòng đời nhân viên
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeLifecycleEventConsumer {

    private final HREventProducer eventProducer;
    private final com.example.hr.user.repository.UserRepository userRepository;

    @KafkaListener(topics = "${kafka.topics.employee-lifecycle}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeEmployeeLifecycleEvent(EmployeeLifecycleEvent event) {
        try {
            log.info("Received employee lifecycle event: employeeId={}, eventType={}", 
                    event.getEmployeeId(), event.getEventType());
            
            switch (event.getEventType()) {
                case "ONBOARDED_PENDING":
                    handleOnboardedPending(event);
                    break;
                case "UPDATED_PENDING":
                    handleUpdatedPending(event);
                    break;
                case "CONTACT_UPDATE_PENDING":
                    handleContactUpdatePending(event);
                    break;
                case "ONBOARDED":
                    handleOnboarded(event);
                    break;
                case "PROMOTED":
                    handlePromoted(event);
                    break;
                case "TRANSFERRED":
                    handleTransferred(event);
                    break;
                case "RESIGNED":
                    handleResigned(event);
                    break;
                case "TERMINATED":
                    handleTerminated(event);
                    break;
                default:
                    log.warn("Unknown employee lifecycle event type: {}", event.getEventType());
            }
            
        } catch (Exception e) {
            log.error("Error processing employee lifecycle event: employeeId={}", 
                    event.getEmployeeId(), e);
        }
    }

    private void handleOnboardedPending(EmployeeLifecycleEvent event) {
        log.info("Creating new user asynchronously: {}", event.getUsername());
        com.example.hr.models.User user = new com.example.hr.models.User();
        user.setUsername(event.getUsername());
        user.setFullName(event.getFullName());
        user.setStatus(com.example.hr.enums.UserStatus.ACTIVE);
        user.setRole(com.example.hr.enums.Role.USER);
        user.setPassword("default_password"); // In production, send email to set password
        userRepository.save(user);
        
        // Trigger the actual onboarded event for notifications
        event.setEventType("ONBOARDED");
        event.setEmployeeId(user.getId());
        eventProducer.publishEmployeeLifecycleEvent(event);
    }

    private void handleUpdatedPending(EmployeeLifecycleEvent event) {
        log.info("Updating user asynchronously: {}", event.getEmployeeId());
        userRepository.findById(event.getEmployeeId()).ifPresent(user -> {
            user.setUsername(event.getUsername());
            user.setFullName(event.getFullName());
            userRepository.save(user);
        });
    }

    private void handleContactUpdatePending(EmployeeLifecycleEvent event) {
        log.info("Updating contact asynchronously: {}", event.getEmployeeId());
        userRepository.findById(event.getEmployeeId()).ifPresent(user -> {
            String reason = event.getReason();
            if (reason != null && reason.contains("|||")) {
                try {
                    String[] parts = reason.split("\\|\\|\\|");
                    javax.crypto.SecretKey key = com.example.hr.util.EncryptionUtils.createSecretKey(parts[0]);
                    String decryptedPhone = com.example.hr.util.EncryptionUtils.decryptAES(parts[1], key);
                    user.setPhoneNumber(decryptedPhone);
                    log.info("Successfully decrypted and updated phone number");
                } catch (Exception e) {
                    log.error("Failed to decrypt phone number", e);
                }
            } else if (reason != null) {
                user.setPhoneNumber(reason);
            }
            userRepository.save(user);
        });
    }

    private void handleOnboarded(EmployeeLifecycleEvent event) {
        log.info("Processing employee onboarding: employee={}, department={}, position={}", 
                event.getFullName(), event.getDepartment(), event.getPosition());
        
        // Gửi welcome notification
        NotificationEvent notification = new NotificationEvent(
                "EMAIL",
                Collections.singletonList(event.getEmployeeId()),
                "Chào mừng đến với công ty!",
                String.format("Chào mừng %s đến với %s với vị trí %s. Ngày bắt đầu: %s",
                        event.getFullName(), event.getDepartment(), 
                        event.getPosition(), event.getEffectiveDate()),
                "HIGH",
                "EMPLOYEE_LIFECYCLE",
                event.getEmployeeId(),
                "EMPLOYEE",
                LocalDateTime.now()
        );
        eventProducer.publishNotificationEvent(notification);
    }

    private void handlePromoted(EmployeeLifecycleEvent event) {
        log.info("Processing employee promotion: employee={}, from={} to={}", 
                event.getFullName(), event.getPosition(), event.getNewPosition());
        
        // Gửi congratulation notification
        NotificationEvent notification = new NotificationEvent(
                "EMAIL",
                Collections.singletonList(event.getEmployeeId()),
                "Chúc mừng thăng chức!",
                String.format("Chúc mừng %s được thăng chức từ %s lên %s. Hiệu lực từ: %s",
                        event.getFullName(), event.getPosition(), 
                        event.getNewPosition(), event.getEffectiveDate()),
                "HIGH",
                "EMPLOYEE_LIFECYCLE",
                event.getEmployeeId(),
                "EMPLOYEE",
                LocalDateTime.now()
        );
        eventProducer.publishNotificationEvent(notification);
    }

    private void handleTransferred(EmployeeLifecycleEvent event) {
        log.info("Processing employee transfer: employee={}, from={} to={}", 
                event.getFullName(), event.getDepartment(), event.getNewDepartment());
        
        // Gửi transfer notification
        NotificationEvent notification = new NotificationEvent(
                "EMAIL",
                Collections.singletonList(event.getEmployeeId()),
                "Thông báo chuyển phòng ban",
                String.format("%s đã được chuyển từ %s sang %s. Hiệu lực từ: %s",
                        event.getFullName(), event.getDepartment(), 
                        event.getNewDepartment(), event.getEffectiveDate()),
                "MEDIUM",
                "EMPLOYEE_LIFECYCLE",
                event.getEmployeeId(),
                "EMPLOYEE",
                LocalDateTime.now()
        );
        eventProducer.publishNotificationEvent(notification);
    }

    private void handleResigned(EmployeeLifecycleEvent event) {
        log.info("Processing employee resignation: employee={}, reason={}", 
                event.getFullName(), event.getReason());
        // TODO: Trigger exit interview, offboarding process
    }

    private void handleTerminated(EmployeeLifecycleEvent event) {
        log.info("Processing employee termination: employee={}, reason={}", 
                event.getFullName(), event.getReason());
        // TODO: Revoke access, collect company assets
    }
}
