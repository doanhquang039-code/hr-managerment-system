package com.example.hr.dto;

import lombok.Data;

@Data
public class ChatbotChatRequest {
    private String message;
    /** Giữ phiên hội thoại (UUID); nếu trống, server tạo mới. */
    private String sessionId;
}
