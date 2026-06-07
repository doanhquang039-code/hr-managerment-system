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
public class AiAgentResponse {
    private String intent;
    private String scope;
    private String mode;
    private String answer;
    private Integer healthScore;
    private Map<String, Object> dataProfile;
    private List<String> modelsUsed;
    private List<String> toolsUsed;
    private List<Map<String, Object>> findings;
    private List<Map<String, Object>> searchResults;
    private List<String> nextActions;
    private LocalDateTime generatedAt;
}


