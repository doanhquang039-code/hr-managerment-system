-- 12. Bảng Chuyển Khoản (Payment) - Quản lý các giao dịch chuyển khoản lương, thưởng, v.v.
CREATE TABLE IF NOT EXISTS payment (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    payroll_id INT,
    payment_type VARCHAR(30) NOT NULL COMMENT 'SALARY: Lương, BONUS: Thưởng tháng, REWARD: Thưởng nhiệm vụ, ADVANCE: Tạm ứng',
    amount DECIMAL(15, 2) NOT NULL COMMENT 'Số tiền thanh toán',
    payment_method VARCHAR(30) NOT NULL DEFAULT 'BANK_TRANSFER' COMMENT 'BANK_TRANSFER, CASH, CHECK',
    account_number VARCHAR(50) COMMENT 'Số tài khoản nhân viên',
    bank_name VARCHAR(100) COMMENT 'Tên ngân hàng',
    transaction_id VARCHAR(100) UNIQUE COMMENT 'Mã giao dịch từ ngân hàng',
    payment_date DATE COMMENT 'Ngày thực hiện thanh toán',
    payment_status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED',
    notes TEXT COMMENT 'Ghi chú thêm',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_payment_user FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    CONSTRAINT fk_payment_payroll FOREIGN KEY (payroll_id) REFERENCES payroll(id) ON DELETE SET NULL,
    
    -- Index để tối ưu tìm kiếm
    INDEX idx_payment_user (user_id),
    INDEX idx_payment_payroll (payroll_id),
    INDEX idx_payment_status (payment_status),
    INDEX idx_payment_date (payment_date),
    INDEX idx_payment_type (payment_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
