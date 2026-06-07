package com.example.hr.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiInsightResponse {
    private String scope;
    private String mode;
    private String summary;
    private Map<String, Object> metrics;
    private List<String> recommendations;
    private LocalDateTime generatedAt;
}


