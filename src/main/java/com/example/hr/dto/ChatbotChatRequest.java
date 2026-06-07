package com.example.hr.dto;

import lombok.Data;

@Data
public class ChatbotChatRequest {
    private String message;
    /** Giá»¯ phiÃªn há»™i thoáº¡i (UUID); náº¿u trá»‘ng, server táº¡o má»›i. */
    private String sessionId;
}


