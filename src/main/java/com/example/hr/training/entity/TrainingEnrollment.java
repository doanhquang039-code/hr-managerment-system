package com.example.hr.training.entity;

import jakarta.persistence.PreUpdate;

import jakarta.persistence.PrePersist;

import com.example.hr.enums.EnrollmentStatus;
import com.example.hr.models.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity ghi danh nhÃ¢n viÃªn vÃ o chÆ°Æ¡ng trÃ¬nh Ä‘Ã o táº¡o.
 */
@Entity
@Table(name = "training_enrollment",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "program_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainingEnrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", nullable = false)
    private TrainingProgram program;

    @Column(name = "enrolled_at", nullable = false)
    private LocalDateTime enrolledAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EnrollmentStatus status = EnrollmentStatus.ENROLLED;

    @Column(precision = 5, scale = 2)
    private BigDecimal score;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    @Column(name = "certificate_url", length = 500)
    private String certificateUrl;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // --- Business Logic ---

    /**
     * Kiá»ƒm tra Ä‘áº¡t/khÃ´ng Ä‘áº¡t (threshold: 60 Ä‘iá»ƒm).
     */
    public boolean isPassed() {
        return score != null && score.compareTo(new BigDecimal("60")) >= 0;
    }

    /**
     * Láº¥y grade label dá»±a trÃªn Ä‘iá»ƒm sá»‘.
     */
    public String getGradeLabel() {
        if (score == null) return "ChÆ°a cÃ³ Ä‘iá»ƒm";
        double s = score.doubleValue();
        if (s >= 90) return "Xuáº¥t sáº¯c (A)";
        if (s >= 80) return "Giá»i (B+)";
        if (s >= 70) return "KhÃ¡ (B)";
        if (s >= 60) return "Trung bÃ¬nh (C)";
        return "KhÃ´ng Ä‘áº¡t (F)";
    }

    /**
     * Láº¥y color badge cho grade.
     */
    public String getGradeColor() {
        if (score == null) return "secondary";
        double s = score.doubleValue();
        if (s >= 90) return "success";
        if (s >= 80) return "primary";
        if (s >= 70) return "info";
        if (s >= 60) return "warning";
        return "danger";
    }

    /**
     * HoÃ n thÃ nh khÃ³a há»c.
     */
    public void complete(BigDecimal finalScore) {
        this.score = finalScore;
        this.completedAt = LocalDateTime.now();
        this.status = isPassed() ? EnrollmentStatus.COMPLETED : EnrollmentStatus.FAILED;
    }

    /**
     * Bá» há»c.
     */
    public void drop() {
        this.status = EnrollmentStatus.DROPPED;
    }

    /**
     * Kiá»ƒm tra cÃ³ certificate hay khÃ´ng.
     */
    public boolean hasCertificate() {
        return certificateUrl != null && !certificateUrl.isBlank();
    }

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
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

    public String getCertificateUrl() {
        return certificateUrl;
    }

    public void setCertificateUrl(String certificateUrl) {
        this.certificateUrl = certificateUrl;
    }
}

