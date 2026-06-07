package com.example.hr.engagement.entity;

import com.example.hr.models.User;
import jakarta.persistence.PreUpdate;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "survey_responses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SurveyResponse {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id", nullable = false)
    private PulseSurvey survey;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // Null náº¿u anonymous
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String answers; // JSON cá»§a cÃ¢u tráº£ lá»i
    
    private LocalDateTime submittedAt;
    
    @PrePersist
    protected void onCreate() {
        updatedAt = LocalDateTime.now();
        createdAt = LocalDateTime.now();
        submittedAt = LocalDateTime.now();
    }

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}


