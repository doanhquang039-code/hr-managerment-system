package com.example.hr.recruitment.entity;

import com.example.hr.department.entity.Department;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "job_postings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobPosting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String requirements;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id")
    private JobPosition position;

    @Column(name = "employment_type", length = 50)
    private String employmentType; // FULL_TIME, PART_TIME, CONTRACT, INTERNSHIP

    @Column(name = "experience_level", length = 50)
    private String experienceLevel; // ENTRY, MID, SENIOR, EXECUTIVE

    @Column(name = "salary_min", precision = 15, scale = 2)
    private BigDecimal salaryMin;

    @Column(name = "salary_max", precision = 15, scale = 2)
    private BigDecimal salaryMax;

    @Column(length = 100)
    private String location;

    @Column(name = "remote_allowed")
    private Boolean remoteAllowed = false;

    @Column(name = "posting_date")
    private LocalDate postingDate;

    @Column(name = "closing_date")
    private LocalDate closingDate;

    @Column(length = 50)
    private String status; // DRAFT, ACTIVE, CLOSED, CANCELLED

    @Column(name = "posted_by")
    private Integer postedBy;

    @Column(name = "views_count")
    private Integer viewsCount = 0;

    @Column(name = "applications_count")
    private Integer applicationsCount = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public boolean isActive() {
        return "ACTIVE".equals(status) && 
               (closingDate == null || closingDate.isAfter(LocalDate.now()));
    }
}

