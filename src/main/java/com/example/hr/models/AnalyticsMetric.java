package com.example.hr.models;


import com.example.hr.department.entity.Department;
import jakarta.persistence.PreUpdate;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "analytics_metrics")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsMetric {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String metricType; // ATTENDANCE_RATE, EMPLOYEE_COUNT, PAYROLL_COST

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private String period; // DAILY, WEEKLY, MONTHLY, YEARLY

    @Column(nullable = false)
    private Double value;

    @Column(columnDefinition = "TEXT")
    private String metadata; // JSON for additional data

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

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


