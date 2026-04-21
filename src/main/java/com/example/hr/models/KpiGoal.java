package com.example.hr.models;

import com.example.hr.enums.KpiStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "kpi_goal")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KpiGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(name = "goal_title", nullable = false, length = 200)
    private String goalTitle;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 50)
    private String category = "INDIVIDUAL"; // INDIVIDUAL, TEAM, DEPARTMENT

    @Column(name = "target_value", precision = 15, scale = 2, nullable = false)
    private BigDecimal targetValue = BigDecimal.ZERO;

    @Column(name = "current_value", precision = 15, scale = 2)
    private BigDecimal currentValue = BigDecimal.ZERO;

    @Column(length = 50)
    private String unit; // %, task, VND, ...

    @Column(precision = 5, scale = 2)
    private BigDecimal weight = BigDecimal.ONE;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private KpiStatus status = KpiStatus.ACTIVE;

    @Column(name = "achievement_pct", insertable = false, updatable = false)
    private BigDecimal achievementPct;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public String getAchievementLabel() {
        if (achievementPct == null) return "0%";
        double pct = achievementPct.doubleValue();
        if (pct >= 100) return "Đạt mục tiêu";
        if (pct >= 80)  return "Gần đạt";
        if (pct >= 50)  return "Đang tiến hành";
        return "Chưa đạt";
    }
}
