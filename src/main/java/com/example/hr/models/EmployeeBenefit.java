package com.example.hr.models;

import jakarta.persistence.PreUpdate;

import jakarta.persistence.PrePersist;

import com.example.hr.enums.BenefitStatus;
import com.example.hr.enums.BenefitType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Entity phÃºc lá»£i nhÃ¢n viÃªn (báº£o hiá»ƒm, há»— trá»£, v.v.).
 */
@Entity
@Table(name = "employee_benefit")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeBenefit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "benefit_type", nullable = false, length = 30)
    private BenefitType benefitType;

    @Column(name = "benefit_name", nullable = false, length = 200)
    private String benefitName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "monetary_value", precision = 15, scale = 2)
    private BigDecimal monetaryValue = BigDecimal.ZERO;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BenefitStatus status = BenefitStatus.ACTIVE;

    @Column(length = 200)
    private String provider;

    @Column(name = "policy_number", length = 100)
    private String policyNumber;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // --- Business Logic ---

    /**
     * Kiá»ƒm tra benefit Ä‘Ã£ háº¿t háº¡n chÆ°a.
     */
    public boolean isExpired() {
        return endDate != null && endDate.isBefore(LocalDate.now());
    }

    /**
     * Kiá»ƒm tra benefit sáº¯p háº¿t háº¡n (trong 30 ngÃ y).
     */
    public boolean isExpiringSoon() {
        return isExpiringSoon(30);
    }

    /**
     * Kiá»ƒm tra benefit sáº¯p háº¿t háº¡n trong N ngÃ y.
     */
    public boolean isExpiringSoon(int days) {
        if (endDate == null) return false;
        LocalDate today = LocalDate.now();
        return endDate.isAfter(today) && endDate.isBefore(today.plusDays(days + 1));
    }

    /**
     * TÃ­nh sá»‘ thÃ¡ng cÃ²n láº¡i.
     */
    public long getRemainingMonths() {
        if (endDate == null) return -1; // vÃ´ thá»i háº¡n
        LocalDate today = LocalDate.now();
        if (endDate.isBefore(today)) return 0;
        return ChronoUnit.MONTHS.between(today, endDate);
    }

    /**
     * TÃ­nh tá»•ng chi phÃ­ dá»±a trÃªn thá»i gian sá»­ dá»¥ng.
     */
    public BigDecimal getTotalCost() {
        if (monetaryValue == null || startDate == null) return BigDecimal.ZERO;
        LocalDate end = endDate != null ? endDate : LocalDate.now();
        long months = ChronoUnit.MONTHS.between(startDate, end);
        if (months <= 0) months = 1;
        return monetaryValue.multiply(BigDecimal.valueOf(months));
    }

    /**
     * Auto-expire náº¿u Ä‘Ã£ quÃ¡ endDate.
     */
    public boolean autoExpireIfNeeded() {
        if (isExpired() && status == BenefitStatus.ACTIVE) {
            this.status = BenefitStatus.EXPIRED;
            return true;
        }
        return false;
    }

    /**
     * Láº¥y icon theo loáº¡i phÃºc lá»£i.
     */
    public String getBenefitIcon() {
        return switch (benefitType) {
            case HEALTH_INSURANCE -> "bi-hospital";
            case LIFE_INSURANCE -> "bi-shield-check";
            case MATERNITY -> "bi-balloon-heart";
            case HOUSING -> "bi-house";
            case TRANSPORTATION -> "bi-bus-front";
            case MEAL -> "bi-cup-hot";
            case EDUCATION -> "bi-book";
            case PHONE -> "bi-phone";
            case OTHER -> "bi-gift";
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
}


