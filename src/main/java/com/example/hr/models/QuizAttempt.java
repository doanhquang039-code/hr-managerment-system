package com.example.hr.models;

import jakarta.persistence.PreUpdate;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "quiz_attempts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizAttempt {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;
    
    private Integer score;
    private Integer totalPoints;
    
    @Column(nullable = false)
    private Boolean passed = false;
    
    @Column(columnDefinition = "TEXT")
    private String answers; // JSON cá»§a cÃ¢u tráº£ lá»i
    
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    
    @PrePersist
    protected void onCreate() {
        updatedAt = LocalDateTime.now();
        createdAt = LocalDateTime.now();
        startedAt = LocalDateTime.now();
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


