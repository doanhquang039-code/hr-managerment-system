package com.example.hr.kafka.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditEvent {
    private String actorUsername;
    private String action;
    private String entityType;
    private String entityId;
    private String detail;
    private String ipAddress;
    private LocalDateTime timestamp;
}
