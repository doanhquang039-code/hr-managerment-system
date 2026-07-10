package com.example.hr.training.dto;

/**
 * DTO trả về khi client poll trạng thái upload job.
 * Trả về dạng JSON qua REST endpoint GET /admin/videos/upload-status/{jobId}.
 */
public class VideoUploadStatusDto {

    private String jobId;
    private String status;      // PENDING | PROCESSING | DONE | FAILED
    private int progress;       // 0-100
    private String message;     // Mô tả trạng thái hiện tại (hiển thị cho user)
    private Integer videoId;    // Có giá trị khi status=DONE
    private String errorDetail; // Có giá trị khi status=FAILED

    // ---- static factory methods ----

    public static VideoUploadStatusDto pending(String jobId) {
        VideoUploadStatusDto dto = new VideoUploadStatusDto();
        dto.jobId = jobId;
        dto.status = "PENDING";
        dto.progress = 5;
        dto.message = "Đang chờ xử lý...";
        return dto;
    }

    public static VideoUploadStatusDto processing(String jobId, int progress, String msg) {
        VideoUploadStatusDto dto = new VideoUploadStatusDto();
        dto.jobId = jobId;
        dto.status = "PROCESSING";
        dto.progress = progress;
        dto.message = msg;
        return dto;
    }

    public static VideoUploadStatusDto done(String jobId, Integer videoId) {
        VideoUploadStatusDto dto = new VideoUploadStatusDto();
        dto.jobId = jobId;
        dto.status = "DONE";
        dto.progress = 100;
        dto.message = "Upload hoàn thành!";
        dto.videoId = videoId;
        return dto;
    }

    public static VideoUploadStatusDto failed(String jobId, String error) {
        VideoUploadStatusDto dto = new VideoUploadStatusDto();
        dto.jobId = jobId;
        dto.status = "FAILED";
        dto.progress = 0;
        dto.message = "Upload thất bại.";
        dto.errorDetail = error;
        return dto;
    }

    // ---- Getters ----

    public String getJobId() { return jobId; }
    public String getStatus() { return status; }
    public int getProgress() { return progress; }
    public String getMessage() { return message; }
    public Integer getVideoId() { return videoId; }
    public String getErrorDetail() { return errorDetail; }
}
