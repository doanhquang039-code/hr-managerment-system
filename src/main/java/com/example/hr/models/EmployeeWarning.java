package com.example.hr.models;

import jakarta.persistence.PreUpdate;

import jakarta.persistence.PrePersist;

import com.example.hr.enums.WarningLevel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity cáº£nh cÃ¡o / ká»· luáº­t nhÃ¢n viÃªn.
 */
@Entity
@Table(name = "employee_warning")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeWarning {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "issued_by", nullable = false)
    private User issuedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "warning_level", nullable = false, length = 20)
    private WarningLevel warningLevel = WarningLevel.VERBAL;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "issued_date", nullable = false)
    private LocalDate issuedDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "is_acknowledged")
    private Boolean isAcknowledged = false;

    @Column(name = "acknowledged_at")
    private LocalDateTime acknowledgedAt;

    @Column(name = "attachment_url", length = 500)
    private String attachmentUrl;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // --- Business Logic ---

    /**
     * Kiá»ƒm tra cáº£nh cÃ¡o cÃ²n hiá»‡u lá»±c hay khÃ´ng.
     */
    public boolean isActive() {
        if (expiryDate == null) return true;
        return !expiryDate.isBefore(LocalDate.now());
    }

    /**
     * NhÃ¢n viÃªn xÃ¡c nháº­n Ä‘Ã£ Ä‘á»c cáº£nh cÃ¡o.
     */
    public void acknowledge() {
        this.isAcknowledged = true;
        this.acknowledgedAt = LocalDateTime.now();
    }

    /**
     * Kiá»ƒm tra cÃ³ cáº§n escalation khÃ´ng (cáº£nh cÃ¡o Ä‘Ã£ quÃ¡ 30 ngÃ y mÃ  chÆ°a acknowledge).
     */
    public boolean needsEscalation() {
        if (Boolean.TRUE.equals(isAcknowledged)) return false;
        return issuedDate != null
                && issuedDate.isBefore(LocalDate.now().minusDays(30));
    }

    /**
     * Láº¥y má»©c cáº£nh cÃ¡o tiáº¿p theo náº¿u cáº§n escalation.
     */
    public WarningLevel getNextEscalationLevel() {
        return warningLevel.next();
    }

    /**
     * Kiá»ƒm tra cÃ³ pháº£i level nghiÃªm trá»ng khÃ´ng (FINAL hoáº·c TERMINATION).
     */
    public boolean isSevere() {
        return warningLevel == WarningLevel.FINAL
                || warningLevel == WarningLevel.TERMINATION;
    }

    /**
     * TÃ­nh sá»‘ ngÃ y ká»ƒ tá»« khi ban hÃ nh.
     */
    public long getDaysSinceIssued() {
        if (issuedDate == null) return 0;
        return java.time.temporal.ChronoUnit.DAYS.between(issuedDate, LocalDate.now());
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


