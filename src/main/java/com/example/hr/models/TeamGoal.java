package com.example.hr.models;


import com.example.hr.department.entity.Department;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "team_goals")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private User manager;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "goal_type", length = 50)
    private String goalType; // QUARTERLY, ANNUAL, PROJECT

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "target_value")
    private Integer targetValue;

    @Column(name = "current_value")
    private Integer currentValue = 0;

    @Column(name = "progress_percentage")
    private Integer progressPercentage = 0;

    @Column(length = 50)
    private String status; // NOT_STARTED, IN_PROGRESS, COMPLETED, CANCELLED

    @Column(length = 50)
    private String priority; // LOW, MEDIUM, HIGH, CRITICAL

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void calculateProgress() {
        if (targetValue != null && targetValue > 0 && currentValue != null) {
            this.progressPercentage = (int) ((currentValue * 100.0) / targetValue);
            if (this.progressPercentage >= 100) {
                this.status = "COMPLETED";
            }
        }
    }
}


