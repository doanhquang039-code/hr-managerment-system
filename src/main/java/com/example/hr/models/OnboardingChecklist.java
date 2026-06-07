package com.example.hr.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "onboarding_checklists")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OnboardingChecklist {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false)
    private String taskName;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false)
    private Integer dayOffset; // NgÃ y thá»© máº¥y sau khi join (0 = ngÃ y Ä‘áº§u)
    
    @Column(nullable = false)
    private String category; // PAPERWORK, IT_SETUP, TRAINING, INTRODUCTION, OTHER
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private User assignedTo; // NgÆ°á»i chá»‹u trÃ¡ch nhiá»‡m
    
    @Column(nullable = false)
    private Boolean isCompleted = false;
    
    private LocalDateTime completedAt;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}


