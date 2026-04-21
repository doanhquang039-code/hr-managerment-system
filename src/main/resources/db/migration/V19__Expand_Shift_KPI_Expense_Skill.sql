-- =====================================================
-- V19: Mở rộng - Ca làm việc, KPI Goals, Chi phí, Kỹ năng
-- =====================================================

-- 1. Work Shifts (Ca làm việc)
CREATE TABLE IF NOT EXISTS work_shift (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    shift_name      VARCHAR(100) NOT NULL,
    shift_code      VARCHAR(20) UNIQUE NOT NULL,
    shift_type      VARCHAR(20) NOT NULL DEFAULT 'REGULAR',
    start_time      TIME NOT NULL,
    end_time        TIME NOT NULL,
    break_minutes   INT DEFAULT 60,
    working_hours   DECIMAL(4,2) GENERATED ALWAYS AS (
                        TIMESTAMPDIFF(MINUTE, start_time, end_time) / 60.0 - break_minutes / 60.0
                    ) STORED,
    allowance       DECIMAL(15,2) DEFAULT 0 COMMENT 'Phụ cấp ca',
    is_active       BOOLEAN DEFAULT TRUE,
    description     TEXT,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Shift Assignments (Phân ca nhân viên)
CREATE TABLE IF NOT EXISTS shift_assignment (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    user_id         INT NOT NULL,
    shift_id        INT NOT NULL,
    work_date       DATE NOT NULL,
    actual_check_in  TIME,
    actual_check_out TIME,
    status          VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    note            TEXT,
    assigned_by     INT,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_sa_user    FOREIGN KEY (user_id)      REFERENCES user(id)       ON DELETE CASCADE,
    CONSTRAINT fk_sa_shift   FOREIGN KEY (shift_id)     REFERENCES work_shift(id) ON DELETE CASCADE,
    CONSTRAINT fk_sa_assigner FOREIGN KEY (assigned_by) REFERENCES user(id)       ON DELETE SET NULL,
    CONSTRAINT uk_shift_user_date UNIQUE (user_id, work_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. KPI Goals (Mục tiêu KPI)
CREATE TABLE IF NOT EXISTS kpi_goal (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    user_id         INT NOT NULL,
    department_id   INT,
    goal_title      VARCHAR(200) NOT NULL,
    description     TEXT,
    category        VARCHAR(50) NOT NULL DEFAULT 'INDIVIDUAL',
    target_value    DECIMAL(15,2) NOT NULL DEFAULT 0,
    current_value   DECIMAL(15,2) DEFAULT 0,
    unit            VARCHAR(50),
    weight          DECIMAL(5,2) DEFAULT 1.0 COMMENT 'Trọng số KPI (0-1)',
    start_date      DATE NOT NULL,
    end_date        DATE NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    achievement_pct DECIMAL(5,2) GENERATED ALWAYS AS (
                        CASE WHEN target_value > 0 THEN (current_value / target_value) * 100 ELSE 0 END
                    ) STORED,
    created_by      INT,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_kg_user   FOREIGN KEY (user_id)       REFERENCES user(id)       ON DELETE CASCADE,
    CONSTRAINT fk_kg_dept   FOREIGN KEY (department_id) REFERENCES department(id) ON DELETE SET NULL,
    CONSTRAINT fk_kg_creator FOREIGN KEY (created_by)   REFERENCES user(id)       ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Expense Claims (Yêu cầu hoàn tiền chi phí)
CREATE TABLE IF NOT EXISTS expense_claim (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    user_id         INT NOT NULL,
    claim_title     VARCHAR(200) NOT NULL,
    category        VARCHAR(50) NOT NULL DEFAULT 'OTHER',
    amount          DECIMAL(15,2) NOT NULL,
    currency        VARCHAR(10) DEFAULT 'VND',
    expense_date    DATE NOT NULL,
    description     TEXT,
    receipt_url     VARCHAR(500),
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    approved_by     INT,
    approved_at     DATETIME,
    paid_at         DATETIME,
    rejection_reason TEXT,
    project_code    VARCHAR(100),
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_ec_user     FOREIGN KEY (user_id)    REFERENCES user(id) ON DELETE CASCADE,
    CONSTRAINT fk_ec_approver FOREIGN KEY (approved_by) REFERENCES user(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. Employee Skills (Kỹ năng nhân viên)
CREATE TABLE IF NOT EXISTS employee_skill (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    user_id         INT NOT NULL,
    skill_name      VARCHAR(100) NOT NULL,
    skill_category  VARCHAR(50) NOT NULL DEFAULT 'TECHNICAL',
    skill_level     VARCHAR(20) NOT NULL DEFAULT 'BEGINNER',
    years_exp       DECIMAL(4,1) DEFAULT 0,
    is_certified    BOOLEAN DEFAULT FALSE,
    certificate_name VARCHAR(200),
    certificate_url VARCHAR(500),
    issued_date     DATE,
    expiry_date     DATE,
    verified_by     INT,
    notes           TEXT,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_es_user     FOREIGN KEY (user_id)    REFERENCES user(id) ON DELETE CASCADE,
    CONSTRAINT fk_es_verifier FOREIGN KEY (verified_by) REFERENCES user(id) ON DELETE SET NULL,
    CONSTRAINT uk_user_skill  UNIQUE (user_id, skill_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================
-- Indexes
-- =====================================================
CREATE INDEX idx_ws_type     ON work_shift(shift_type);
CREATE INDEX idx_ws_active   ON work_shift(is_active);
CREATE INDEX idx_sa_user     ON shift_assignment(user_id);
CREATE INDEX idx_sa_date     ON shift_assignment(work_date);
CREATE INDEX idx_sa_status   ON shift_assignment(status);
CREATE INDEX idx_kg_user     ON kpi_goal(user_id);
CREATE INDEX idx_kg_status   ON kpi_goal(status);
CREATE INDEX idx_kg_period   ON kpi_goal(start_date, end_date);
CREATE INDEX idx_ec_user     ON expense_claim(user_id);
CREATE INDEX idx_ec_status   ON expense_claim(status);
CREATE INDEX idx_ec_date     ON expense_claim(expense_date);
CREATE INDEX idx_es_user     ON employee_skill(user_id);
CREATE INDEX idx_es_category ON employee_skill(skill_category);

-- =====================================================
-- Sample Data
-- =====================================================

-- Work Shifts
INSERT INTO work_shift (shift_name, shift_code, shift_type, start_time, end_time, break_minutes, allowance, description) VALUES
('Ca Sáng',   'MORNING',   'REGULAR',  '08:00:00', '17:00:00', 60, 0,        'Ca làm việc buổi sáng tiêu chuẩn'),
('Ca Chiều',  'AFTERNOON', 'REGULAR',  '13:00:00', '22:00:00', 60, 50000,    'Ca làm việc buổi chiều'),
('Ca Đêm',    'NIGHT',     'NIGHT',    '22:00:00', '06:00:00', 60, 200000,   'Ca làm việc ban đêm'),
('Ca Hành Chính', 'ADMIN', 'REGULAR',  '08:30:00', '17:30:00', 60, 0,        'Ca hành chính văn phòng'),
('Ca Cuối Tuần', 'WEEKEND','WEEKEND',  '08:00:00', '17:00:00', 60, 150000,   'Ca làm việc cuối tuần');

-- KPI Goals sample (sẽ gán cho user_id=1 nếu tồn tại)
INSERT INTO kpi_goal (user_id, goal_title, category, target_value, current_value, unit, weight, start_date, end_date, status)
SELECT id, 'Hoàn thành 10 task trong tháng', 'INDIVIDUAL', 10, 3, 'task', 1.0, '2026-04-01', '2026-04-30', 'ACTIVE'
FROM user WHERE id = 1 LIMIT 1;

INSERT INTO kpi_goal (user_id, goal_title, category, target_value, current_value, unit, weight, start_date, end_date, status)
SELECT id, 'Tỷ lệ chấm công đúng giờ >= 95%', 'INDIVIDUAL', 95, 88, '%', 0.8, '2026-04-01', '2026-04-30', 'ACTIVE'
FROM user WHERE id = 1 LIMIT 1;
