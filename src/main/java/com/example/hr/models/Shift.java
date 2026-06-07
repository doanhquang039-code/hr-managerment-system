package com.example.hr.models;

import jakarta.persistence.PreUpdate;

import jakarta.persistence.PrePersist;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Entity
@Table(name = "shifts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Shift {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(nullable = false)
    private String name; // Morning, Afternoon, Night
    
    @Column(nullable = false)
    private LocalTime startTime;
    
    @Column(nullable = false)
    private LocalTime endTime;
    
    private Integer gracePeriodMinutes; // Thá»i gian cho phÃ©p Ä‘áº¿n muá»™n
    
    private Integer overtimeThresholdMinutes; // NgÆ°á»¡ng tÃ­nh overtime
    
    @Column(nullable = false)
    private Boolean isActive = true;
    
    private String description;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
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


