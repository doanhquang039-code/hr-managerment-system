package com.example.hr.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "asset_assignments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssetAssignment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false)
    private LocalDate assignedDate;
    
    private LocalDate returnDate;
    
    @Column(nullable = false)
    private String status = "ACTIVE"; // ACTIVE, RETURNED
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by")
    private User assignedBy;
    
    @Column(columnDefinition = "TEXT")
    private String assignmentNotes;
    
    @Column(columnDefinition = "TEXT")
    private String returnNotes;
    
    private String returnCondition; // EXCELLENT, GOOD, FAIR, POOR, DAMAGED
    
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


