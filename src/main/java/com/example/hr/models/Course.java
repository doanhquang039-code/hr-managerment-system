package com.example.hr.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "courses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Course {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false)
    private String category; // TECHNICAL, SOFT_SKILLS, COMPLIANCE, LEADERSHIP
    
    private String thumbnailUrl;

    @Column(length = 500)
    private String videoUrl;

    @Column(length = 255)
    private String videoPublicId;
    
    @Column(nullable = false)
    private String level; // BEGINNER, INTERMEDIATE, ADVANCED
    
    private Integer durationMinutes;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id")
    private User instructor;
    
    @Column(nullable = false)
    private Boolean isActive = true;
    
    @Column(nullable = false)
    private Boolean isMandatory = false;
    
    private Integer passingScore; // Điểm đạt (%)
    
    @Column(columnDefinition = "TEXT")
    private String learningObjectives;
    
    @Column(columnDefinition = "TEXT")
    private String prerequisites;
    
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
