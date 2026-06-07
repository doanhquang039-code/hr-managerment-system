package com.example.hr.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "benefit_enrollments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BenefitEnrollment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "benefit_plan_id", nullable = false)
    private BenefitPlan benefitPlan;
    
    @Column(nullable = false)
    private LocalDate enrollmentDate;
    
    private LocalDate effectiveDate;
    private LocalDate endDate;
    
    @Column(nullable = false)
    private String status = "ACTIVE"; // ACTIVE, CANCELLED, EXPIRED
    
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


