package com.example.hr.models;

import com.example.hr.enums.ReviewStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "performance_reviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private User employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private User reviewer;

    @Column(name = "review_cycle", length = 100)
    private String reviewCycle; // Q1-2026, H1-2026, Annual-2026

    @Column(name = "review_date", nullable = false)
    private LocalDate reviewDate;

    @Column(name = "review_period_start")
    private LocalDate reviewPeriodStart;

    @Column(name = "review_period_end")
    private LocalDate reviewPeriodEnd;

    @Column(name = "overall_score")
    private Integer overallScore; // 1-5 or 1-10

    @Column(name = "technical_skills_score")
    private Integer technicalSkillsScore;

    @Column(name = "soft_skills_score")
    private Integer softSkillsScore;

    @Column(name = "productivity_score")
    private Integer productivityScore;

    @Column(name = "teamwork_score")
    private Integer teamworkScore;

    @Column(name = "leadership_score")
    private Integer leadershipScore;

    @Column(name = "strengths", columnDefinition = "TEXT")
    private String strengths;

    @Column(name = "areas_for_improvement", columnDefinition = "TEXT")
    private String areasForImprovement;

    @Column(name = "achievements", columnDefinition = "TEXT")
    private String achievements;

    @Column(name = "goals_for_next_period", columnDefinition = "TEXT")
    private String goalsForNextPeriod;

    @Column(name = "training_recommendations", columnDefinition = "TEXT")
    private String trainingRecommendations;

    @Column(name = "employee_comments", columnDefinition = "TEXT")
    private String employeeComments;

    @Column(name = "manager_comments", columnDefinition = "TEXT")
    private String managerComments;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private ReviewStatus status = ReviewStatus.DRAFT;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "acknowledged_at")
    private LocalDateTime acknowledgedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Alias method for compatibility
    public User getUser() {
        return this.employee;
    }

    // Calculate overall score based on individual scores
    public void calculateOverallScore() {
        if (technicalSkillsScore != null && softSkillsScore != null && 
            productivityScore != null && teamworkScore != null) {
            
            int total = technicalSkillsScore + softSkillsScore + productivityScore + teamworkScore;
            int count = 4;
            
            if (leadershipScore != null) {
                total += leadershipScore;
                count++;
            }
            
            this.overallScore = Math.round((float) total / count);
        }
    }
}


