package com.example.hr.recruitment.entity;

import jakarta.persistence.PreUpdate;

import jakarta.persistence.PrePersist;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "candidates")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Candidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "resume_url", length = 500)
    private String resumeUrl;

    @Column(name = "cover_letter", columnDefinition = "TEXT")
    private String coverLetter;

    @Column(name = "linkedin_url", length = 500)
    private String linkedinUrl;

    @Column(name = "portfolio_url", length = 500)
    private String portfolioUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_posting_id")
    private JobPosting jobPosting;

    @Column(name = "current_stage", length = 50)
    private String currentStage; // APPLIED, SCREENING, INTERVIEW, OFFER, HIRED, REJECTED

    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;

    @Column(length = 100)
    private String currentCompany;

    @Column(name = "current_position", length = 100)
    private String currentPosition;

    @Column(name = "expected_salary")
    private Integer expectedSalary;

    @Column(name = "availability_date")
    private String availabilityDate;

    @Column(columnDefinition = "TEXT")
    private String skills;

    @Column(columnDefinition = "TEXT")
    private String education;

    @Column(name = "overall_score")
    private Integer overallScore; // 1-100

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "source", length = 50)
    private String source; // WEBSITE, LINKEDIN, REFERRAL, AGENCY

    @Column(name = "applied_at")
    private LocalDateTime appliedAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public boolean isActive() {
        return !("HIRED".equals(currentStage) || "REJECTED".equals(currentStage));
    }

    // Alias methods for compatibility with existing code
    public String getStatus() {
        return this.currentStage;
    }

    public void setStatus(String status) {
        this.currentStage = status;
    }

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(LocalDateTime appliedAt) {
        this.appliedAt = appliedAt;
    }
}

