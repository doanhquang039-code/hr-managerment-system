package com.example.hr.models;

import com.example.hr.enums.DocumentType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity quản lý tài liệu nhân viên (CMND, bằng cấp, chứng chỉ, v.v.).
 */
@Entity
@Table(name = "employee_document")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 30)
    private DocumentType documentType = DocumentType.OTHER;

    @Column(name = "document_name", nullable = false, length = 200)
    private String documentName;

    @Column(name = "file_url", length = 500)
    private String fileUrl;

    @Column(name = "file_size")
    private Long fileSize = 0L;

    @Column(name = "mime_type", length = 100)
    private String mimeType;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt = LocalDateTime.now();

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "is_verified")
    private Boolean isVerified = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by")
    private User verifiedBy;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    /** Google Drive file ID — dùng để share/xóa */
    @Column(name = "drive_file_id", length = 200)
    private String driveFileId;

    /** AWS S3 object key — dùng để download/xóa */
    @Column(name = "s3_key", length = 500)
    private String s3Key;

    // --- Helper methods ---

    /**
     * Kiểm tra tài liệu đã hết hạn chưa.
     */
    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }

    /**
     * Kiểm tra tài liệu sắp hết hạn trong N ngày.
     */
    public boolean isExpiringSoon(int days) {
        if (expiryDate == null) return false;
        return expiryDate.isBefore(LocalDate.now().plusDays(days))
                && !expiryDate.isBefore(LocalDate.now());
    }

    /**
     * Format file size thành human-readable.
     */
    public String getFormattedFileSize() {
        if (fileSize == null || fileSize == 0) return "N/A";
        if (fileSize < 1024) return fileSize + " B";
        if (fileSize < 1024 * 1024) return String.format("%.1f KB", fileSize / 1024.0);
        return String.format("%.1f MB", fileSize / (1024.0 * 1024));
    }

    /**
     * Lấy icon dựa trên loại tài liệu.
     */
    public String getDocumentIcon() {
        return switch (documentType) {
            case IDENTITY_CARD -> "bi-person-badge";
            case DEGREE -> "bi-mortarboard";
            case CERTIFICATE -> "bi-award";
            case CONTRACT_SCAN -> "bi-file-earmark-text";
            case RESUME -> "bi-file-person";
            case HEALTH_CHECK -> "bi-heart-pulse";
            case OTHER -> "bi-file-earmark";
        };
    }
}
