-- V10: Seed dữ liệu mẫu cho bảng Attendance (Chấm công)
-- Giả sử user có ID 1, 2, 3 đã tồn tại trong DB

INSERT IGNORE INTO attendance (user_id, attendance_date, check_in_time, check_out_time, status, note) VALUES
-- Tuần này (tháng 4/2026)
(1, '2026-03-30', '08:05:00', '17:35:00', 'PRESENT',     NULL),
(1, '2026-03-31', '08:45:00', '17:30:00', 'LATE',        'Kẹt xe'),
(1, '2026-04-01', '07:58:00', '17:30:00', 'PRESENT',     NULL),
(1, '2026-04-02', '08:00:00', NULL,        'PRESENT',     NULL),

(2, '2026-03-30', '08:00:00', '17:30:00', 'PRESENT',     NULL),
(2, '2026-03-31', '08:10:00', '16:45:00', 'EARLY_LEAVE', 'Có việc gia đình'),
(2, '2026-04-01', '08:00:00', '17:30:00', 'PRESENT',     NULL),
(2, '2026-04-02', '08:05:00', NULL,        'PRESENT',     NULL),

(3, '2026-03-30', NULL,       NULL,        'ABSENT',      'Nghỉ phép'),
(3, '2026-03-31', '09:00:00', '17:30:00', 'LATE',        'Mất điện'),
(3, '2026-04-01', '08:00:00', '17:30:00', 'PRESENT',     NULL),
(3, '2026-04-02', '08:20:00', NULL,        'LATE',        NULL);
COMMIT;