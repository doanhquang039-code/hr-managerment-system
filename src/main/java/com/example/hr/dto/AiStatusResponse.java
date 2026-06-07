package com.example.hr.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiStatusResponse {
    private boolean enabled;
    private boolean available;
    private String provider;
    private String model;
    private String mode;
    private String message;
}


