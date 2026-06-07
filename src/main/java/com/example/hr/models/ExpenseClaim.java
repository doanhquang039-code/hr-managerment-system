package com.example.hr.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "expense_claims")
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
    
    @Column(nullable = false)
    private String claimNumber; // MÃ£ Ä‘Æ¡n
    
    @Column(nullable = false)
    private String category; // TRAVEL, MEAL, EQUIPMENT, TRAINING, OTHER
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false)
    private BigDecimal amount;
    
    @Column(nullable = false)
    private LocalDate expenseDate;
    
    private String receiptUrl; // URL hÃ³a Ä‘Æ¡n
    
    @Column(nullable = false)
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED, PAID
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;
    
    private LocalDateTime approvedAt;
    
    private String rejectionReason;
    
    private LocalDateTime paidAt;
    
    private LocalDateTime createdAt;
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
}


