package com.example.hr.training.entity;

import jakarta.persistence.PreUpdate;

import com.example.hr.models.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "course_enrollments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseEnrollment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
    
    @Column(nullable = false)
    private String status = "ENROLLED"; // ENROLLED, IN_PROGRESS, COMPLETED, FAILED
    
    private Integer progressPercent = 0;
    
    private Integer score;
    
    private LocalDateTime enrolledAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    
    private String certificateUrl;
    
    @PrePersist
    protected void onCreate() {
        updatedAt = LocalDateTime.now();
        createdAt = LocalDateTime.now();
        enrolledAt = LocalDateTime.now();
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

