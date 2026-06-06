package com.example.hr.kafka.events;


import com.example.hr.payroll.entity.Payroll;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Event cho Notification (Thông báo)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {
    private String notificationType; // EMAIL, SMS, PUSH, IN_APP
    private List<Integer> recipientUserIds;
    private String subject;
    private String message;
    private String priority; // HIGH, MEDIUM, LOW
    private String category; // ATTENDANCE, LEAVE, PAYROLL, PERFORMANCE, RECRUITMENT, TRAINING
    private Integer relatedEntityId;
    private String relatedEntityType;
    private LocalDateTime timestamp;
}
