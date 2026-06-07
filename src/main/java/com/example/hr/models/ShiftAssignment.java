package com.example.hr.models;

import jakarta.persistence.PreUpdate;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "shift_assignments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShiftAssignment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_id", nullable = false)
    private Shift shift;
    
    @Column(nullable = false)
    private LocalDate assignedDate;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by")
    private User assignedBy;
    
    private String notes;
    
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        updatedAt = LocalDateTime.now();
        createdAt = LocalDateTime.now();
    }

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}


