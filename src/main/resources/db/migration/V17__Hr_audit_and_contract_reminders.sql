-- Nhật ký thao tác HR (tuân thủ / truy vết)
CREATE TABLE IF NOT EXISTS hr_audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    actor_username VARCHAR(100) NOT NULL,
    action VARCHAR(80) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id VARCHAR(64),
    detail TEXT,
    ip_address VARCHAR(45),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_hr_audit_created (created_at),
    INDEX idx_hr_audit_action (action),
    INDEX idx_hr_audit_entity (entity_type, entity_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tránh gửi trùng cùng một mốc nhắc (30/14/7 ngày) cho một hợp đồng
CREATE TABLE IF NOT EXISTS contract_expiry_reminder (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    contract_id INT NOT NULL,
    reminder_days INT NOT NULL,
    sent_at DATETIME NOT NULL,
    UNIQUE KEY uk_contract_reminder (contract_id, reminder_days),
    CONSTRAINT fk_cer_contract FOREIGN KEY (contract_id) REFERENCES contract (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
