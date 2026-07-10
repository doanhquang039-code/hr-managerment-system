package com.example.hr.training.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Lưu trạng thái của một video upload job bất đồng bộ.
 * Admin submit file → job tạo ngay (PENDING) → thread pool xử lý
 * (PROCESSING) → hoàn thành (DONE) hoặc lỗi (FAILED).
 */
@Entity
@Table(name = "video_upload_jobs")
public class VideoUploadJob {

    public enum Status {
        PENDING,     // Đã nhận file, chưa xử lý
        PROCESSING,  // Đang validate + upload Cloudinary
        DONE,        // Upload thành công
        FAILED       // Có lỗi
    }

    @Id
    @Column(name = "id", length = 36)
    private String id; // UUID

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Status status = Status.PENDING;

    /** Đường dẫn file tạm trên disk — xoá sau khi upload xong */
    @Column(name = "temp_file_path", length = 500)
    private String tempFilePath;

    /** Tên file gốc của admin */
    @Column(name = "original_filename", length = 500)
    private String originalFilename;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "tags", length = 500)
    private String tags;

    /** ID của User upload (không dùng FK để tránh lazy loading phức tạp) */
    @Column(name = "uploader_id")
    private Integer uploaderId;

    /** ID của TrainingVideo sau khi tạo thành công */
    @Column(name = "video_id")
    private Integer videoId;

    /** Thông báo lỗi nếu FAILED */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /** Phần trăm tiến độ (0-100), cập nhật trong quá trình xử lý */
    @Column(name = "progress")
    private Integer progress = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    // ----------------------------------------------------------------
    // Getters & Setters
    // ----------------------------------------------------------------

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public String getTempFilePath() { return tempFilePath; }
    public void setTempFilePath(String tempFilePath) { this.tempFilePath = tempFilePath; }

    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public Integer getUploaderId() { return uploaderId; }
    public void setUploaderId(Integer uploaderId) { this.uploaderId = uploaderId; }

    public Integer getVideoId() { return videoId; }
    public void setVideoId(Integer videoId) { this.videoId = videoId; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public Integer getProgress() { return progress; }
    public void setProgress(Integer progress) { this.progress = progress; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
