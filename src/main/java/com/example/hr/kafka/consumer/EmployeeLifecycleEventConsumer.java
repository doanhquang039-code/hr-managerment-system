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

    @KafkaListener(topics = "${kafka.topics.employee-lifecycle}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeEmployeeLifecycleEvent(EmployeeLifecycleEvent event) {
        try {
            log.info("Received employee lifecycle event: employeeId={}, eventType={}", 
                    event.getEmployeeId(), event.getEventType());
            
            switch (event.getEventType()) {
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
