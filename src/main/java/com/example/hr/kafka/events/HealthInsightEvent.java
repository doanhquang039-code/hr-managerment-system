package com.example.hr.kafka.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HealthInsightEvent {
    private Integer userId;
    private String username;
    private String role;
    private long wellnessScore;
    private String riskLevel;
    private List<String> flags;
    private List<String> recommendations;
    private Double sleepHours;
    private Integer stressLevel;
    private Integer steps;
    private Double waterLiters;
    private Double overtimeHours;
    private LocalDateTime timestamp;
}
