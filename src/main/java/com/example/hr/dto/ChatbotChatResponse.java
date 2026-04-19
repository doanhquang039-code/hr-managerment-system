package com.example.hr.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatbotChatResponse {
    private int messageId;
    private String sessionId;
    private String intent;
    private String reply;
    private boolean escalated;
}
