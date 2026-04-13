package com.example.hr.models;

import com.example.hr.enums.ReviewStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "performance_review")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reviewer_id")
    private User reviewer;

    @Column(name = "review_period", nullable = false, length = 20)
    private String reviewPeriod; // e.g. "2025-Q1"

    @Column(name = "review_date", nullable = false)
    private LocalDate reviewDate;

    @Column(name = "kpi_score", precision = 5, scale = 2)
    private BigDecimal kpiScore = BigDecimal.ZERO;

    @Column(name = "attitude_score", precision = 5, scale = 2)
    private BigDecimal attitudeScore = BigDecimal.ZERO;

    @Column(name = "teamwork_score", precision = 5, scale = 2)
    private BigDecimal teamworkScore = BigDecimal.ZERO;

    @Column(name = "overall_score", precision = 5, scale = 2)
    private BigDecimal overallScore = BigDecimal.ZERO;

    @Column(columnDefinition = "TEXT")
    private String strengths;

    @Column(columnDefinition = "TEXT")
    private String improvements;

    @Column(columnDefinition = "TEXT")
    private String comments;

    @Enumerated(EnumType.STRING)
    private ReviewStatus status = ReviewStatus.DRAFT;

    // Helper: tính toán overall score từ 3 tiêu chí
    public void calculateOverallScore() {
        if (kpiScore != null && attitudeScore != null && teamworkScore != null) {
            BigDecimal total = kpiScore.add(attitudeScore).add(teamworkScore);
            this.overallScore = total.divide(BigDecimal.valueOf(3), 2, java.math.RoundingMode.HALF_UP);
        }
    }

    // Helper: lấy rating label
    public String getRatingLabel() {
        if (overallScore == null) return "N/A";
        double score = overallScore.doubleValue();
        if (score >= 90) return "Xuất sắc";
        if (score >= 75) return "Tốt";
        if (score >= 60) return "Khá";
        if (score >= 50) return "Trung bình";
        return "Yếu";
    }

    public String getRatingColor() {
        if (overallScore == null) return "secondary";
        double score = overallScore.doubleValue();
        if (score >= 90) return "success";
        if (score >= 75) return "primary";
        if (score >= 60) return "info";
        if (score >= 50) return "warning";
        return "danger";
    }
}
