package com.example.hr.dto;

import com.example.hr.enums.NotificationChannel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSendDTO {
    
    private List<Long> userIds;
    private String notificationType;
    private NotificationChannel channel;
    private String subject;
    private String message;
    private Map<String, String> variables;
    private Boolean useTemplate;
    private String templateCode;
}


