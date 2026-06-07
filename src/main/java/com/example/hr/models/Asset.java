package com.example.hr.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "assets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Asset {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(nullable = false, unique = true)
    private String assetCode;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String category; // LAPTOP, PHONE, MONITOR, KEYBOARD, MOUSE, FURNITURE, OTHER
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    private String brand;
    private String model;
    private String serialNumber;
    
    private BigDecimal purchasePrice;
    private LocalDate purchaseDate;
    
    @Column(nullable = false)
    private String status = "AVAILABLE"; // AVAILABLE, ASSIGNED, MAINTENANCE, RETIRED
    
    @Column(nullable = false, name = "`condition`")
    private String condition = "GOOD"; // EXCELLENT, GOOD, FAIR, POOR
    
    private String location;
    
    private String imageUrl;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
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


