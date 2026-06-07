package com.example.hr.models;

import com.example.hr.training.entity.Course;
import com.example.hr.training.entity.CourseLesson;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "quizzes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Quiz {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id")
    private CourseLesson lesson;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "reward_top1")
    private String rewardTop1;

    @Column(name = "reward_top2")
    private String rewardTop2;

    @Column(name = "reward_top3")
    private String rewardTop3;
    
    private Integer timeLimit; // Giá»›i háº¡n thá»i gian (phÃºt)
    
    private Integer passingScore; // Äiá»ƒm Ä‘áº¡t (%)
    
    @Column(nullable = false)
    private Boolean isActive = true;
    
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


