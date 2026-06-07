package com.example.hr.models;

import jakarta.persistence.PreUpdate;

import jakarta.persistence.PrePersist;

import com.example.hr.enums.QRCodeType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * QR Code entity for check-in, asset tracking, document verification
 */
@Entity
@Table(name = "qr_codes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QRCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "code", unique = true, nullable = false, length = 100)
    private String code; // Unique QR code string

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private QRCodeType type; // CHECK_IN, ASSET, DOCUMENT, EVENT

    @Column(length = 200)
    private String name; // Display name (e.g., "Main Office Entrance")

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "location", length = 200)
    private String location; // Physical location

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "scan_count", nullable = false)
    private Integer scanCount = 0;

    @Column(name = "last_scanned_at")
    private LocalDateTime lastScannedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt; // Optional expiry for temporary QR codes

    // Metadata for different types
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata; // JSON string for additional data

    // Business methods
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return isActive && !isExpired();
    }

    public void incrementScanCount() {
        this.scanCount++;
        this.lastScannedAt = LocalDateTime.now();
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


