package com.example.hr.models;

import jakarta.persistence.PreUpdate;

import jakarta.persistence.PrePersist;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * QR Code scan history
 */
@Entity
@Table(name = "qr_code_scans")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QRCodeScan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "qr_code_id", nullable = false)
    private QRCode qrCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "scanned_at", nullable = false)
    private LocalDateTime scannedAt = LocalDateTime.now();

    @Column(name = "scan_type", length = 30)
    private String scanType; // CHECK_IN, CHECK_OUT, VIEW, etc.

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "device_info", length = 200)
    private String deviceInfo;

    @Column(name = "location_lat")
    private Double locationLat;

    @Column(name = "location_lng")
    private Double locationLng;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "is_successful", nullable = false)
    private Boolean isSuccessful = true;

    @Column(name = "error_message")
    private String errorMessage;

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
}


