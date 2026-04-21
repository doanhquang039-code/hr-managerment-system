package com.example.hr.models;

import com.example.hr.enums.ExpenseStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "expense_claim")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseClaim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "claim_title", nullable = false, length = 200)
    private String claimTitle;

    @Column(length = 50)
    private String category = "OTHER"; // TRAVEL, MEAL, EQUIPMENT, TRAINING, OTHER

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(length = 10)
    private String currency = "VND";

    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "receipt_url", length = 500)
    private String receiptUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExpenseStatus status = ExpenseStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "project_code", length = 100)
    private String projectCode;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
