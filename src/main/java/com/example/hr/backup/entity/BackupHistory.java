package com.example.hr.backup.entity;

import com.example.hr.models.User;
import jakarta.persistence.PreUpdate;

import jakarta.persistence.PrePersist;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "backup_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BackupHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "backup_name", nullable = false, length = 255)
    private String backupName;

    @Column(name = "backup_type", length = 50)
    private String backupType; // FULL, INCREMENTAL, DIFFERENTIAL

    @Column(name = "backup_scope", length = 50)
    private String backupScope; // DATABASE, FILES, BOTH

    @Column(name = "file_path", length = 500)
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize; // bytes

    @Column(name = "storage_location", length = 100)
    private String storageLocation; // LOCAL, AWS_S3, GOOGLE_DRIVE

    @Column(name = "storage_url", length = 500)
    private String storageUrl;

    @Column(name = "status", length = 20)
    private String status; // PENDING, IN_PROGRESS, COMPLETED, FAILED

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "duration")
    private Long duration; // seconds

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "is_scheduled")
    private Boolean isScheduled = false;

    @Column(name = "retention_days")
    private Integer retentionDays = 30;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // Alias methods for compatibility with services
    public String getBackupPath() {
        return this.filePath;
    }

    public void setBackupPath(String backupPath) {
        this.filePath = backupPath;
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


