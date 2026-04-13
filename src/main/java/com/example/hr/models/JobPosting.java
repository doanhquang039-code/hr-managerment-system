package com.example.hr.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "job_posting")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobPosting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 255)
    private String title;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "position_id")
    private JobPosition position;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String requirements;

    @Column(name = "salary_min", precision = 15, scale = 2)
    private BigDecimal salaryMin;

    @Column(name = "salary_max", precision = 15, scale = 2)
    private BigDecimal salaryMax;

    private LocalDate deadline;

    @Column(length = 20)
    private String status = "OPEN"; // OPEN, CLOSED, FILLED

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
