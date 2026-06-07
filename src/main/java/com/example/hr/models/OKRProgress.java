package com.example.hr.models;

import jakarta.persistence.PreUpdate;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "okr_progress")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OKRProgress {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "key_result_id", nullable = false)
    private KeyResult keyResult;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by", nullable = false)
    private User updatedBy;

    @Column(nullable = false)
    private Double previousValue;

    @Column(nullable = false)
    private Double newValue;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(columnDefinition = "TEXT")
    private String achievements;

    @Column(columnDefinition = "TEXT")
    private String challenges;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        updatedAt = LocalDateTime.now();
        createdAt = LocalDateTime.now();
    }

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}


