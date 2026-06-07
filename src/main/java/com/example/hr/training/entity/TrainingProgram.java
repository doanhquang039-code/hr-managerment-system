package com.example.hr.training.entity;

import jakarta.persistence.PreUpdate;

import jakarta.persistence.PrePersist;

import com.example.hr.enums.TrainingStatus;
import com.example.hr.department.entity.Department;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity chÆ°Æ¡ng trÃ¬nh Ä‘Ã o táº¡o ná»™i bá»™/ngoáº¡i bá»™.
 */
@Entity
@Table(name = "training_program")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainingProgram {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "program_name", nullable = false, length = 200)
    private String programName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 100)
    private String instructor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "max_capacity")
    private Integer maxCapacity = 30;

    @Column(length = 200)
    private String location;

    @Column(name = "training_type", length = 30)
    private String trainingType = "INTERNAL";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TrainingStatus status = TrainingStatus.PLANNED;

    @Column(precision = 15, scale = 2)
    private BigDecimal budget = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "program", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TrainingEnrollment> enrollments = new ArrayList<>();

    // --- Business Logic ---

    /**
     * Sá»‘ ngÃ y Ä‘Ã o táº¡o.
     */
    public long getDurationDays() {
        if (startDate == null || endDate == null) return 0;
        return ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    /**
     * Sá»‘ slot cÃ²n trá»‘ng.
     */
    public int getAvailableSlots() {
        int enrolled = enrollments != null ? enrollments.size() : 0;
        return Math.max(0, (maxCapacity != null ? maxCapacity : 30) - enrolled);
    }

    /**
     * Kiá»ƒm tra chÆ°Æ¡ng trÃ¬nh Ä‘Ã£ Ä‘áº§y chÆ°a.
     */
    public boolean isFull() {
        return getAvailableSlots() <= 0;
    }

    /**
     * Kiá»ƒm tra chÆ°Æ¡ng trÃ¬nh Ä‘ang diá»…n ra.
     */
    public boolean isOngoing() {
        LocalDate today = LocalDate.now();
        return startDate != null && endDate != null
                && !today.isBefore(startDate) && !today.isAfter(endDate);
    }

    /**
     * Kiá»ƒm tra chÆ°Æ¡ng trÃ¬nh sáº¯p diá»…n ra (trong vÃ²ng N ngÃ y).
     */
    public boolean isUpcoming(int days) {
        if (startDate == null) return false;
        LocalDate today = LocalDate.now();
        return startDate.isAfter(today) && startDate.isBefore(today.plusDays(days + 1));
    }

    /**
     * TÃ­nh chi phÃ­ trung bÃ¬nh trÃªn má»—i há»c viÃªn.
     */
    public BigDecimal getCostPerEnrollee() {
        if (budget == null || enrollments == null || enrollments.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return budget.divide(BigDecimal.valueOf(enrollments.size()), 0,
                java.math.RoundingMode.HALF_UP);
    }

    /**
     * Tá»· lá»‡ hoÃ n thÃ nh.
     */
    public double getCompletionRate() {
        if (enrollments == null || enrollments.isEmpty()) return 0;
        long completed = enrollments.stream()
                .filter(e -> e.getStatus() == com.example.hr.enums.EnrollmentStatus.COMPLETED)
                .count();
        return (double) completed / enrollments.size() * 100;
    }

    /**
     * Láº¥y progress icon tÃ¹y status.
     */
    public String getStatusIcon() {
        return switch (status) {
            case PLANNED -> "bi-calendar-event";
            case IN_PROGRESS -> "bi-play-circle";
            case COMPLETED -> "bi-check-circle";
            case CANCELLED -> "bi-x-circle";
        };
    }

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Integer getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(Integer maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public TrainingStatus getStatus() {
        return status;
    }

    public void setStatus(TrainingStatus status) {
        this.status = status;
    }
}

