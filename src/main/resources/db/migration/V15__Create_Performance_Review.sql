-- V15: Performance Review & Recruitment Tables

-- 1. Bảng Đánh giá hiệu suất
CREATE TABLE IF NOT EXISTS performance_review (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    reviewer_id INT,
    review_period VARCHAR(20) NOT NULL, -- e.g. '2025-Q1'
    review_date DATE NOT NULL,
    kpi_score DECIMAL(5,2) DEFAULT 0,
    attitude_score DECIMAL(5,2) DEFAULT 0,
    teamwork_score DECIMAL(5,2) DEFAULT 0,
    overall_score DECIMAL(5,2) DEFAULT 0,
    strengths TEXT,
    improvements TEXT,
    comments TEXT,
    status VARCHAR(20) DEFAULT 'DRAFT', -- DRAFT, SUBMITTED, APPROVED
    CONSTRAINT fk_review_user FOREIGN KEY (user_id) REFERENCES user(id),
    CONSTRAINT fk_review_reviewer FOREIGN KEY (reviewer_id) REFERENCES user(id)
);

-- 2. Bảng Tin tuyển dụng
CREATE TABLE IF NOT EXISTS job_posting (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    department_id INT,
    position_id INT,
    description TEXT,
    requirements TEXT,
    salary_min DECIMAL(15,2),
    salary_max DECIMAL(15,2),
    deadline DATE,
    status VARCHAR(20) DEFAULT 'OPEN', -- OPEN, CLOSED, FILLED
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_posting_dept FOREIGN KEY (department_id) REFERENCES department(id),
    CONSTRAINT fk_posting_pos FOREIGN KEY (position_id) REFERENCES jobposition(id)
);

-- 3. Bảng Ứng viên
CREATE TABLE IF NOT EXISTS candidate (
    id INT AUTO_INCREMENT PRIMARY KEY,
    job_posting_id INT,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    cv_url VARCHAR(500),
    applied_date DATE DEFAULT (CURRENT_DATE),
    status VARCHAR(30) DEFAULT 'NEW', -- NEW, SCREENING, INTERVIEW, OFFER, HIRED, REJECTED
    notes TEXT,
    CONSTRAINT fk_candidate_posting FOREIGN KEY (job_posting_id) REFERENCES job_posting(id)
);
