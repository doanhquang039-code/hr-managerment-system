package com.example.hr.models;


import com.example.hr.department.entity.Department;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Entity
@Table(name = "team_budgets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamBudget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private User manager;

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false)
    private Integer month;

    @Column(name = "allocated_budget", precision = 15, scale = 2)
    private BigDecimal allocatedBudget;

    @Column(name = "spent_budget", precision = 15, scale = 2)
    private BigDecimal spentBudget = BigDecimal.ZERO;

    @Column(name = "remaining_budget", precision = 15, scale = 2)
    private BigDecimal remainingBudget;

    @Column(length = 100)
    private String category; // SALARY, TRAINING, EQUIPMENT, TRAVEL, OTHER

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 50)
    private String status; // ACTIVE, CLOSED, EXCEEDED

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void calculateRemainingBudget() {
        if (allocatedBudget != null && spentBudget != null) {
            this.remainingBudget = allocatedBudget.subtract(spentBudget);
        }
    }

    public double getUtilizationPercent() {
        if (allocatedBudget == null || spentBudget == null || allocatedBudget.compareTo(BigDecimal.ZERO) <= 0) {
            return 0.0;
        }
        BigDecimal percent = spentBudget
                .divide(allocatedBudget, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        return Math.min(100.0, percent.doubleValue());
    }

    public boolean isOverBudget() {
        return remainingBudget != null && remainingBudget.compareTo(BigDecimal.ZERO) < 0;
    }
}


