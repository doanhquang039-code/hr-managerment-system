package com.example.hr.dto;

import lombok.Data;

@Data
public class ChatbotRateRequest {
    private int messageId;
    /** 1â€“5 sao */
    private Integer rating;
}


