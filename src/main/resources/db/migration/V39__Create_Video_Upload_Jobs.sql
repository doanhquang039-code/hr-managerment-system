-- Migration: Tạo bảng lưu trạng thái video upload job bất đồng bộ
-- Mỗi lần admin upload video sẽ tạo 1 record ở đây
-- Job được xử lý bởi @Async thread pool, client polling để lấy kết quả

CREATE TABLE IF NOT EXISTS video_upload_jobs (
    id               VARCHAR(36)   NOT NULL PRIMARY KEY COMMENT 'UUID của job',
    status           VARCHAR(20)   NOT NULL DEFAULT 'PENDING'
                         COMMENT 'PENDING | PROCESSING | DONE | FAILED',
    temp_file_path   VARCHAR(500)  NULL     COMMENT 'Đường dẫn file tạm trên server',
    original_filename VARCHAR(500) NULL     COMMENT 'Tên file gốc do admin upload',
    title            VARCHAR(255)  NULL,
    description      TEXT          NULL,
    category         VARCHAR(100)  NULL,
    tags             VARCHAR(500)  NULL,
    uploader_id      BIGINT        NULL     COMMENT 'ID của user thực hiện upload',
    video_id         INT           NULL     COMMENT 'ID TrainingVideo sau khi tạo thành công',
    error_message    TEXT          NULL     COMMENT 'Nội dung lỗi nếu status=FAILED',
    progress         INT           NOT NULL DEFAULT 0 COMMENT 'Tiến độ 0-100',
    created_at       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_vuj_uploader (uploader_id),
    INDEX idx_vuj_status   (status),
    INDEX idx_vuj_updated  (updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Bảng theo dõi trạng thái upload video bất đồng bộ';
