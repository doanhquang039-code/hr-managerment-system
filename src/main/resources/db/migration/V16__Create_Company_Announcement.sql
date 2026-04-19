-- Thông báo nội bộ: toàn công ty (department_id NULL) hoặc theo phòng ban
CREATE TABLE IF NOT EXISTS company_announcement (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    author_id INT NOT NULL,
    department_id INT NULL COMMENT 'NULL = gửi toàn công ty',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    priority VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    published_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ca_author FOREIGN KEY (author_id) REFERENCES user(id) ON DELETE CASCADE,
    CONSTRAINT fk_ca_department FOREIGN KEY (department_id) REFERENCES department(id) ON DELETE SET NULL,
    INDEX idx_ca_published (published_at DESC),
    INDEX idx_ca_active (active),
    INDEX idx_ca_dept (department_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
