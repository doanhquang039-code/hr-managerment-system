CREATE TABLE IF NOT EXISTS password_reset_requests (
    id INT AUTO_INCREMENT PRIMARY KEY,
    account_identifier VARCHAR(120) NOT NULL,
    user_id INT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    request_source VARCHAR(40) NOT NULL DEFAULT 'USERNAME',
    requester_message VARCHAR(500) NULL,
    admin_note VARCHAR(500) NULL,
    resolved_by INT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at DATETIME NULL,
    CONSTRAINT fk_password_reset_requests_user
        FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE SET NULL,
    CONSTRAINT fk_password_reset_requests_resolver
        FOREIGN KEY (resolved_by) REFERENCES user(id) ON DELETE SET NULL,
    INDEX idx_password_reset_requests_status (status),
    INDEX idx_password_reset_requests_created_at (created_at)
);
