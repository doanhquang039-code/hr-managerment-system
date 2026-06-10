/**
 * HRMS Settings Panel v1
 * - 5 languages: VI, EN, ZH (Chinese), JA (Japanese), KO (Korean)
 * - Background themes
 * - Brightness control
 * - All client-side, saved to localStorage
 */
(function () {
  'use strict';

  // ==================== DICTIONARIES ====================

  var DICT = {
    en: {
      // Navigation
      'Dashboard': 'Dashboard', 'Nhân viên': 'Employees', 'Phòng ban': 'Departments',
      'Chức vụ': 'Positions', 'Hợp đồng': 'Contracts', 'Bảng lương': 'Payroll',
      'Thanh toán': 'Payments', 'Tài liệu': 'Documents', 'Nghỉ phép': 'Leave Requests',
      'Chấm công': 'Attendance', 'Công việc': 'Tasks', 'Phân công': 'Assignments',
      'Đánh giá KPI': 'KPI Reviews', 'Báo cáo': 'Reports', 'Thông báo công ty': 'Announcements',
      'Tuyển dụng': 'Recruitment', 'Video đào tạo': 'Training Videos', 'Thêm video mới': 'Upload Video',
      'Nhật ký hệ thống': 'Audit Log', 'Nhật ký': 'Audit Log', 'KPI Goals': 'KPI Goals',
      'Mục tiêu KPI': 'KPI Goals', 'Chi phí': 'Expenses',
      'Kỹ năng': 'Skills', 'Kỹ năng NV': 'Employee Skills', 'Ca làm việc': 'Work Shifts', 'Phân ca': 'Shift Assignments',
      'Đăng xuất': 'Logout', 'Hồ sơ cá nhân': 'My Profile', 'Làm thêm giờ': 'Overtime',
      'Phiếu lương': 'Payslip', 'Công việc của tôi': 'My Tasks', 'Thông báo': 'Notifications',
      'Nhóm của tôi': 'My Team', 'Duyệt nghỉ phép': 'Approve Leaves', 'Duyệt OT': 'Approve OT',
      'Tin tuyển dụng': 'Job Postings', 'Ứng viên': 'Candidates',
      'LMS - Khóa học': 'LMS - Courses', 'QR Code': 'QR Code', 'Khảo sát': 'Surveys',
      'Vinh danh': 'Recognition', 'Onboarding': 'Onboarding', 'OKR': 'OKR',
      'Cài đặt': 'Settings', 'Phân tích': 'Analytics',
      'Advanced Dashboard': 'Advanced Dashboard', 'Quản lý User Nâng cao': 'Advanced User Management',
      'Lương': 'Salary', 'Video': 'Video', 'Analytics': 'Analytics', 'Khóa học': 'Courses',
      'Khen thưởng': 'Recognition', 'Cấu hình Hệ thống': 'System Configuration',
      'Audit Logs': 'Audit Logs', 'Backup & Restore': 'Backup & Restore',
      'System Monitor': 'System Monitor', 'Cache & Email': 'Cache & Email',
      'Cloud Storage': 'Cloud Storage',
      // Section labels
      'Cá nhân': 'Personal', 'Hỗ trợ': 'Support', 'Đào tạo': 'Training',
      // Page headings
      'Bảng Điều Khiển': 'Dashboard', 'NHÂN VIÊN': 'EMPLOYEES', 'PHÒNG BAN': 'DEPARTMENTS',
      'CHỨC VỤ': 'POSITIONS', 'CHỜ DUYỆT NGHỈ': 'PENDING LEAVES', 'CÔNG VIỆC': 'TASKS',
      'Chấm Công 7 Ngày Qua': 'Attendance Last 7 Days', 'Nhân Viên Theo Phòng Ban': 'Employees by Dept',
      'Hôm nay:': 'Today:', 'Xem chi tiết': 'View Details', 'Tổng quan đi làm theo ngày': 'Daily attendance',
      'Phân bổ nhân lực': 'Workforce distribution',
      // Buttons
      'Lưu': 'Save', 'Hủy': 'Cancel', 'Xóa': 'Delete', 'Sửa': 'Edit', 'Thêm': 'Add',
      'Tìm kiếm': 'Search', 'Quay lại': 'Back', 'Xác nhận': 'Confirm', 'Lọc': 'Filter',
      'Tất cả': 'All', 'Trạng thái': 'Status', 'Thao tác': 'Action', 'Tìm': 'Search',
      // Status
      'Chờ duyệt': 'Pending', 'Đã duyệt': 'Approved', 'Từ chối': 'Rejected',
      'Đang thực hiện': 'In Progress', 'Hoàn thành': 'Completed', 'Đã hủy': 'Cancelled',
      'Đã thanh toán': 'Paid', 'Chưa thanh toán': 'Unpaid',
      // Table headers
      'Họ và tên': 'Full Name', 'Số điện thoại': 'Phone', 'Ngày vào làm': 'Hire Date',
      'Mã nhân viên': 'Employee Code', 'Số tiền': 'Amount', 'Ghi chú': 'Note',
      'Mô tả': 'Description', 'Từ ngày': 'From', 'Đến ngày': 'To', 'Số ngày': 'Days',
      'Lý do': 'Reason', 'Giờ vào': 'Check In', 'Giờ ra': 'Check Out',
      'Lương cơ bản': 'Base Salary', 'Thưởng': 'Bonus', 'Khấu trừ': 'Deductions',
      'Lương thực nhận': 'Net Salary', 'Không có dữ liệu': 'No data',
      // New sidebar sections
      'Công việc': 'Tasks', 'Tài chính': 'Finance', 'Đánh giá': 'Reviews',
      'Tài sản': 'Assets', 'Trợ lý HR': 'HR Assistant',
      'Danh sách nhân viên': 'Employee List', 'Quản lý': 'Management', 'Nhân sự': 'HR',
      // Page headings
      'Thiết Bị & Tài Sản': 'Equipment & Assets', 'Chấm Công': 'Attendance',
      'Thông Báo Công Ty': 'Company Announcements', 'Yêu Cầu Chi Phí': 'Expense Claims',
      'Tài Liệu Của Tôi': 'My Documents', 'Ca Làm Việc Của Tôi': 'My Work Shifts',
      'Đăng Ký Làm Thêm Giờ': 'Overtime Registration', 'Lịch Sử Chấm Công': 'Attendance History',
      'Duyệt Đơn Làm Thêm Giờ': 'Overtime Approval', 'Nhóm Của Tôi': 'My Team',
      'Trung Tâm Tuyển Dụng': 'Recruitment Center',
      // Labels
      'Tiêu đề': 'Title', 'Danh mục': 'Category', 'Ngày chi': 'Expense Date',
      'Chứng từ': 'Receipt', 'Nộp Yêu Cầu Mới': 'Submit New Claim',
      'Lịch Sử Yêu Cầu': 'Claim History', 'Gửi yêu cầu': 'Submit',
      'Đang sử dụng': 'In Use', 'Đã trả': 'Returned', 'Tổng tài sản': 'Total Assets',
      'Ngày nhận': 'Received', 'Tình trạng': 'Condition', 'Bàn giao bởi': 'Assigned by',
      'Chưa check-in': 'Not checked in', 'Đã hoàn thành ca': 'Shift completed',
      'Xem tất cả': 'View All', 'Thêm nhân viên': 'Add Employee',
      'Giao việc mới': 'New Assignment', 'Truy cập nhanh': 'Quick Access',
      'Hành Động Nhanh': 'Quick Actions', 'Xem chấm công': 'View Attendance',
      'Thêm đánh giá KPI': 'Add KPI Review', 'Đăng tin tuyển dụng': 'Post Job',
    },
    zh: {
      // Navigation
      'Dashboard': '仪表板', 'Nhân viên': '员工', 'Phòng ban': '部门',
      'Chức vụ': '职位', 'Hợp đồng': '合同', 'Bảng lương': '工资单',
      'Thanh toán': '付款', 'Tài liệu': '文件', 'Nghỉ phép': '请假',
      'Chấm công': '考勤', 'Công việc': '任务', 'Phân công': '分配',
      'Đánh giá KPI': 'KPI评估', 'Báo cáo': '报告', 'Thông báo công ty': '公司公告',
      'Tuyển dụng': '招聘', 'Video đào tạo': '培训视频', 'Thêm video mới': '上传视频',
      'Nhật ký hệ thống': '系统日志', 'Nhật ký': '审计日志', 'KPI Goals': 'KPI目标',
      'Mục tiêu KPI': 'KPI目标', 'Chi phí': '费用',
      'Kỹ năng': '技能', 'Kỹ năng NV': '员工技能', 'Ca làm việc': '班次', 'Phân ca': '排班',
      'Đăng xuất': '退出登录', 'Hồ sơ cá nhân': '个人资料', 'Làm thêm giờ': '加班',
      'Phiếu lương': '工资条', 'Công việc của tôi': '我的任务', 'Thông báo': '通知',
      'Nhóm của tôi': '我的团队', 'Duyệt nghỉ phép': '审批请假', 'Duyệt OT': '审批加班',
      'Tin tuyển dụng': '招聘信息', 'Ứng viên': '候选人',
      'LMS - Khóa học': 'LMS - 课程', 'QR Code': '二维码', 'Khảo sát': '调查',
      'Vinh danh': '表彰', 'Onboarding': '入职', 'OKR': 'OKR',
      'Cài đặt': '设置', 'Phân tích': '分析',
      'Advanced Dashboard': '高级仪表板', 'Quản lý User Nâng cao': '高级用户管理',
      'Lương': '工资', 'Video': '视频', 'Analytics': '分析', 'Khóa học': '课程',
      'Khen thưởng': '奖励', 'Cấu hình Hệ thống': '系统配置',
      'Audit Logs': '审计日志', 'Backup & Restore': '备份与恢复',
      'System Monitor': '系统监控', 'Cache & Email': '缓存和邮件',
      'Cloud Storage': '云存储',
      // Section labels
      'Cá nhân': '个人', 'Hỗ trợ': '支持', 'Đào tạo': '培训',
      // Page headings
      'Bảng Điều Khiển': '仪表板', 'NHÂN VIÊN': '员工', 'PHÒNG BAN': '部门',
      'CHỨC VỤ': '职位', 'CHỜ DUYỆT NGHỈ': '待审批', 'CÔNG VIỆC': '任务',
      'Chấm Công 7 Ngày Qua': '近7天考勤', 'Nhân Viên Theo Phòng Ban': '按部门员工',
      'Hôm nay:': '今天:', 'Xem chi tiết': '查看详情',
      // Buttons
      'Lưu': '保存', 'Hủy': '取消', 'Xóa': '删除', 'Sửa': '编辑', 'Thêm': '添加',
      'Tìm kiếm': '搜索', 'Quay lại': '返回', 'Xác nhận': '确认', 'Lọc': '筛选',
      'Tất cả': '全部', 'Trạng thái': '状态', 'Thao tác': '操作', 'Tìm': '搜索',
      // Status
      'Chờ duyệt': '待审批', 'Đã duyệt': '已批准', 'Từ chối': '已拒绝',
      'Đang thực hiện': '进行中', 'Hoàn thành': '已完成', 'Đã hủy': '已取消',
      'Đã thanh toán': '已付款', 'Chưa thanh toán': '未付款',
      // Table headers
      'Họ và tên': '姓名', 'Số điện thoại': '电话', 'Ngày vào làm': '入职日期',
      'Mã nhân viên': '员工编号', 'Số tiền': '金额', 'Ghi chú': '备注',
      'Lý do': '原因', 'Giờ vào': '签到', 'Giờ ra': '签退',
      'Lương cơ bản': '基本工资', 'Thưởng': '奖金', 'Khấu trừ': '扣款',
      'Lương thực nhận': '实发工资', 'Không có dữ liệu': '暂无数据',
      // New
      'Tài chính': '财务', 'Đánh giá': '评价', 'Tài sản': '资产', 'Trợ lý HR': 'HR助手',
      'Danh sách nhân viên': '员工列表', 'Quản lý': '管理', 'Nhân sự': '人事',
      'Thiết Bị & Tài Sản': '设备与资产', 'Thông Báo Công Ty': '公司公告',
      'Yêu Cầu Chi Phí': '费用申请', 'Tài Liệu Của Tôi': '我的文件',
      'Ca Làm Việc Của Tôi': '我的班次', 'Đăng Ký Làm Thêm Giờ': '加班申请',
      'Tiêu đề': '标题', 'Danh mục': '类别', 'Ngày chi': '消费日期',
      'Chứng từ': '凭证', 'Đang sử dụng': '使用中', 'Đã trả': '已归还',
      'Chưa check-in': '未签到', 'Đã hoàn thành ca': '班次已完成',
      'Xem tất cả': '查看全部', 'Thêm nhân viên': '添加员工',
      'Giao việc mới': '新分配', 'Truy cập nhanh': '快速访问',
    },
    ja: {
      // Navigation
      'Dashboard': 'ダッシュボード', 'Nhân viên': '従業員', 'Phòng ban': '部署',
      'Chức vụ': '役職', 'Hợp đồng': '契約', 'Bảng lương': '給与',
      'Thanh toán': '支払い', 'Tài liệu': '書類', 'Nghỉ phép': '休暇申請',
      'Chấm công': '勤怠', 'Công việc': 'タスク', 'Phân công': '割り当て',
      'Đánh giá KPI': 'KPI評価', 'Báo cáo': 'レポート', 'Thông báo công ty': 'お知らせ',
      'Tuyển dụng': '採用', 'Video đào tạo': 'トレーニング動画', 'Thêm video mới': '動画アップロード',
      'Nhật ký hệ thống': '監査ログ', 'Nhật ký': '監査ログ', 'KPI Goals': 'KPI目標',
      'Mục tiêu KPI': 'KPI目標', 'Chi phí': '経費',
      'Kỹ năng': 'スキル', 'Kỹ năng NV': '従業員スキル', 'Ca làm việc': 'シフト', 'Phân ca': 'シフト割り当て',
      'Đăng xuất': 'ログアウト', 'Hồ sơ cá nhân': 'プロフィール', 'Làm thêm giờ': '残業',
      'Phiếu lương': '給与明細', 'Công việc của tôi': '自分のタスク', 'Thông báo': '通知',
      'Nhóm của tôi': '自分のチーム', 'Duyệt nghỉ phép': '休暇承認', 'Duyệt OT': '残業承認',
      'Tin tuyển dụng': '求人情報', 'Ứng viên': '候補者',
      'LMS - Khóa học': 'LMS - コース', 'QR Code': 'QRコード', 'Khảo sát': 'アンケート',
      'Vinh danh': '表彰', 'Onboarding': 'オンボーディング', 'OKR': 'OKR',
      'Cài đặt': '設定', 'Phân tích': '分析',
      'Advanced Dashboard': '詳細ダッシュボード', 'Quản lý User Nâng cao': '高度なユーザー管理',
      'Lương': '給与', 'Video': '動画', 'Analytics': '分析', 'Khóa học': 'コース',
      'Khen thưởng': '報奨', 'Cấu hình Hệ thống': 'システム設定',
      'Audit Logs': '監査ログ', 'Backup & Restore': 'バックアップと復元',
      'System Monitor': 'システム監視', 'Cache & Email': 'キャッシュとメール',
      'Cloud Storage': 'クラウドストレージ',
      // Section labels
      'Cá nhân': '個人', 'Hỗ trợ': 'サポート', 'Đào tạo': 'トレーニング',
      // Page headings
      'Bảng Điều Khiển': 'ダッシュボード', 'NHÂN VIÊN': '従業員', 'PHÒNG BAN': '部署',
      'CHỨC VỤ': '役職', 'CHỜ DUYỆT NGHỈ': '承認待ち', 'CÔNG VIỆC': 'タスク',
      'Chấm Công 7 Ngày Qua': '過去7日間の勤怠', 'Nhân Viên Theo Phòng Ban': '部署別従業員',
      'Hôm nay:': '今日:', 'Xem chi tiết': '詳細を見る',
      // Buttons
      'Lưu': '保存', 'Hủy': 'キャンセル', 'Xóa': '削除', 'Sửa': '編集', 'Thêm': '追加',
      'Tìm kiếm': '検索', 'Quay lại': '戻る', 'Xác nhận': '確認', 'Lọc': 'フィルター',
      'Tất cả': 'すべて', 'Trạng thái': 'ステータス', 'Thao tác': '操作', 'Tìm': '検索',
      // Status
      'Chờ duyệt': '承認待ち', 'Đã duyệt': '承認済み', 'Từ chối': '却下',
      'Đang thực hiện': '進行中', 'Hoàn thành': '完了', 'Đã hủy': 'キャンセル',
      'Đã thanh toán': '支払済み', 'Chưa thanh toán': '未払い',
      // Table headers
      'Họ và tên': '氏名', 'Số điện thoại': '電話番号', 'Ngày vào làm': '入社日',
      'Mã nhân viên': '社員番号', 'Số tiền': '金額', 'Ghi chú': 'メモ',
      'Lý do': '理由', 'Giờ vào': '出勤', 'Giờ ra': '退勤',
      'Lương cơ bản': '基本給', 'Thưởng': 'ボーナス', 'Khấu trừ': '控除',
      'Lương thực nhận': '手取り', 'Không có dữ liệu': 'データなし',
      // New
      'Tài chính': '財務', 'Đánh giá': '評価', 'Tài sản': '資産', 'Trợ lý HR': 'HRアシスタント',
      'Danh sách nhân viên': '従業員一覧', 'Quản lý': '管理', 'Nhân sự': '人事',
      'Thiết Bị & Tài Sản': '設備と資産', 'Thông Báo Công Ty': '社内通知',
      'Yêu Cầu Chi Phí': '経費請求', 'Tài Liệu Của Tôi': '自分の書類',
      'Ca Làm Việc Của Tôi': '自分のシフト', 'Đăng Ký Làm Thêm Giờ': '残業申請',
      'Tiêu đề': 'タイトル', 'Danh mục': 'カテゴリ', 'Ngày chi': '支出日',
      'Chứng từ': '領収書', 'Đang sử dụng': '使用中', 'Đã trả': '返却済み',
      'Chưa check-in': '未出勤', 'Đã hoàn thành ca': 'シフト完了',
      'Xem tất cả': 'すべて見る', 'Thêm nhân viên': '従業員追加',
      'Giao việc mới': '新しい割り当て', 'Truy cập nhanh': 'クイックアクセス',
    }
    ,
    ko: {
      // Navigation
      'Dashboard': '대시보드', 'Nhân viên': '직원', 'Phòng ban': '부서',
      'Chức vụ': '직책', 'Hợp đồng': '계약', 'Bảng lương': '급여',
      'Thanh toán': '지급', 'Tài liệu': '문서', 'Nghỉ phép': '휴가 요청',
      'Chấm công': '근태', 'Công việc': '업무', 'Phân công': '배정',
      'Đánh giá KPI': 'KPI 평가', 'Báo cáo': '보고서', 'Thông báo công ty': '회사 공지',
      'Tuyển dụng': '채용', 'Video đào tạo': '교육 영상', 'Thêm video mới': '영상 업로드',
      'Nhật ký hệ thống': '시스템 로그', 'Nhật ký': '감사 로그', 'KPI Goals': 'KPI 목표',
      'Mục tiêu KPI': 'KPI 목표', 'Chi phí': '비용',
      'Kỹ năng': '기술', 'Kỹ năng NV': '직원 기술', 'Ca làm việc': '근무 교대', 'Phân ca': '교대 배정',
      'Đăng xuất': '로그아웃', 'Hồ sơ cá nhân': '내 프로필', 'Làm thêm giờ': '초과근무',
      'Phiếu lương': '급여명세서', 'Công việc của tôi': '내 업무', 'Thông báo': '알림',
      'Nhóm của tôi': '내 팀', 'Duyệt nghỉ phép': '휴가 승인', 'Duyệt OT': '초과근무 승인',
      'Tin tuyển dụng': '채용 공고', 'Ứng viên': '지원자',
      'LMS - Khóa học': 'LMS - 과정', 'QR Code': 'QR 코드', 'Khảo sát': '설문조사',
      'Vinh danh': '인정', 'Onboarding': '온보딩', 'OKR': 'OKR',
      'Cài đặt': '설정', 'Phân tích': '분석',
      'Advanced Dashboard': '고급 대시보드', 'Quản lý User Nâng cao': '고급 사용자 관리',
      'Lương': '급여', 'Video': '영상', 'Analytics': '분석', 'Khóa học': '과정',
      'Khen thưởng': '포상', 'Cấu hình Hệ thống': '시스템 구성',
      'Audit Logs': '감사 로그', 'Backup & Restore': '백업 및 복원',
      'System Monitor': '시스템 모니터', 'Cache & Email': '캐시 및 이메일',
      'Cloud Storage': '클라우드 스토리지',
      // Section labels
      'Cá nhân': '개인', 'Hỗ trợ': '지원', 'Đào tạo': '교육',
      // Page headings
      'Bảng Điều Khiển': '대시보드', 'NHÂN VIÊN': '직원', 'PHÒNG BAN': '부서',
      'CHỨC VỤ': '직책', 'CHỜ DUYỆT NGHỈ': '승인 대기 휴가', 'CÔNG VIỆC': '업무',
      'Chấm Công 7 Ngày Qua': '최근 7일 근태', 'Nhân Viên Theo Phòng Ban': '부서별 직원',
      'Hôm nay:': '오늘:', 'Xem chi tiết': '자세히 보기',
      // Buttons
      'Lưu': '저장', 'Hủy': '취소', 'Xóa': '삭제', 'Sửa': '수정', 'Thêm': '추가',
      'Tìm kiếm': '검색', 'Quay lại': '뒤로', 'Xác nhận': '확인', 'Lọc': '필터',
      'Tất cả': '전체', 'Trạng thái': '상태', 'Thao tác': '작업', 'Tìm': '검색',
      // Status
      'Chờ duyệt': '승인 대기', 'Đã duyệt': '승인됨', 'Từ chối': '거절됨',
      'Đang thực hiện': '진행 중', 'Hoàn thành': '완료', 'Đã hủy': '취소됨',
      'Đã thanh toán': '지급 완료', 'Chưa thanh toán': '미지급',
      // Table headers
      'Họ và tên': '성명', 'Số điện thoại': '전화번호', 'Ngày vào làm': '입사일',
      'Mã nhân viên': '직원 코드', 'Số tiền': '금액', 'Ghi chú': '메모',
      'Lý do': '사유', 'Giờ vào': '출근', 'Giờ ra': '퇴근',
      'Lương cơ bản': '기본급', 'Thưởng': '상여금', 'Khấu trừ': '공제',
      'Lương thực nhận': '실수령액', 'Không có dữ liệu': '데이터 없음',
      // New
      'Tài chính': '재무', 'Đánh giá': '평가', 'Tài sản': '자산', 'Trợ lý HR': 'HR 도우미',
      'Danh sách nhân viên': '직원 목록', 'Quản lý': '관리', 'Nhân sự': '인사',
      'Thiết Bị & Tài Sản': '장비 및 자산', 'Thông Báo Công Ty': '회사 공지',
      'Yêu Cầu Chi Phí': '비용 청구', 'Tài Liệu Của Tôi': '내 문서',
      'Ca Làm Việc Của Tôi': '내 근무 교대', 'Đăng Ký Làm Thêm Giờ': '초과근무 신청',
      'Tiêu đề': '제목', 'Danh mục': '카테고리', 'Ngày chi': '지출일',
      'Chứng từ': '영수증', 'Đang sử dụng': '사용 중', 'Đã trả': '반납됨',
      'Chưa check-in': '미출근', 'Đã hoàn thành ca': '근무 완료',
      'Xem tất cả': '전체 보기', 'Thêm nhân viên': '직원 추가',
      'Giao việc mới': '새 배정', 'Truy cập nhanh': '빠른 접근',
    }
  };

  // ==================== BACKGROUNDS ====================
  var BACKGROUNDS = [
    { id: 'default', label: 'Default Dark', color: 'linear-gradient(180deg,#0f172a,#1e293b)' },
    { id: 'ocean', label: 'Ocean Blue', color: 'linear-gradient(180deg,#0c1445,#1a237e)' },
    { id: 'forest', label: 'Forest Green', color: 'linear-gradient(180deg,#0a2e1a,#1b5e20)' },
    { id: 'sunset', label: 'Sunset', color: 'linear-gradient(180deg,#4a0e2e,#7b1fa2)' },
    { id: 'midnight', label: 'Midnight', color: 'linear-gradient(180deg,#000000,#1a1a2e)' },
    { id: 'slate', label: 'Slate Gray', color: 'linear-gradient(180deg,#1c2333,#2d3748)' },
    { id: 'crimson', label: 'Crimson', color: 'linear-gradient(180deg,#3b0000,#7f0000)' },
    { id: 'teal', label: 'Teal', color: 'linear-gradient(180deg,#003333,#006064)' },
  ];

  // ==================== STORAGE ====================
  function getScope() {
    var rawScope = (document.body && document.body.getAttribute('data-settings-scope')) || 'global';
    return String(rawScope).replace(/[^a-zA-Z0-9_-]/g, '') || 'global';
  }
  function getStorageKey(key) {
    var scope = getScope();
    return scope === 'global' ? 'hrms_' + key : 'hrms_' + scope + '_' + key;
  }
  function getSetting(key, def) {
    try { return localStorage.getItem(getStorageKey(key)) || def; } catch(e) { return def; }
  }
  function setSetting(key, val) {
    try { localStorage.setItem(getStorageKey(key), val); } catch(e) {}
  }

  // ==================== TRANSLATION ====================
  function getLang() {
    var urlParams = new URLSearchParams(window.location.search);
    return urlParams.get('lang') || getSetting('lang', 'vi');
  }

  var UI_TEXT = {
    vi: {
      'settings.panel.title': 'Cài đặt',
      'settings.panel.language': 'Ngôn ngữ',
      'settings.panel.background': 'Giao diện nền',
      'settings.panel.brightness': 'Độ sáng',
      'settings.panel.fontSize': 'Cỡ chữ',
      'settings.panel.compactMode': 'Chế độ gọn',
      'settings.panel.compactDesc': 'Giảm khoảng cách hiển thị',
      'settings.lang.vi': 'Tiếng Việt',
      'settings.lang.en': 'English',
      'settings.lang.zh': '中文',
      'settings.lang.ja': '日本語',
      'settings.lang.ko': '한국어',
      'settings.font.small': 'Nhỏ',
      'settings.font.medium': 'Vừa',
      'settings.font.large': 'Lớn',
      'admin.settings.title': 'Cài đặt hệ thống',
      'admin.settings.hero.badge': 'Trung tâm điều khiển Admin',
      'admin.settings.hero.desc': 'Quản lý cấu hình vận hành, bảo mật đăng nhập và các tham số HRMS.',
      'admin.settings.viewLogin': 'Xem trang đăng nhập',
      'admin.settings.total': 'Tổng cấu hình',
      'admin.settings.loginVerification': 'Mã xác nhận đăng nhập',
      'admin.settings.codeLengthMetric': 'Ký tự trong mã xác nhận',
      'admin.settings.loginSecurity': 'Bảo mật đăng nhập',
      'admin.settings.loginSecurityDesc': 'Các thay đổi ở đây áp dụng ngay cho form login.',
      'admin.settings.loginSectionTag': 'Đăng nhập',
      'admin.settings.verificationCode': 'Mã xác nhận',
      'admin.settings.enabled': 'Bật',
      'admin.settings.disabled': 'Tắt',
      'admin.settings.enabledHint': 'Nên bật để giảm đăng nhập tự động.',
      'admin.settings.codeLength': 'Độ dài mã',
      'admin.settings.codeLengthHint': 'Giới hạn hợp lệ: 4 đến 8 ký tự.',
      'admin.settings.charset': 'Bộ ký tự sinh mã',
      'admin.settings.charsetHint': 'Đã bỏ các ký tự dễ nhầm như I, O, 0, 1 trong mặc định.',
      'admin.settings.loginPreview': 'Xem trước đăng nhập',
      'admin.settings.sampleCode': 'Mã mẫu',
      'admin.settings.previewHint': 'Admin có thể tắt mã xác nhận khi demo nội bộ, nhưng môi trường thật nên giữ bật.',
      'admin.settings.saveLogin': 'Lưu bảo mật đăng nhập',
      'admin.settings.configList': 'Danh sách cấu hình',
      'admin.settings.configKey': 'Mã cấu hình',
      'admin.settings.configValue': 'Giá trị',
      'admin.settings.description': 'Mô tả',
      'admin.settings.valuePlaceholder': 'Nhập giá trị...',
      'admin.settings.noConfig': 'Chưa có cấu hình nào',
      'admin.settings.saveAll': 'Lưu tất cả thay đổi',
      'admin.settings.addNew': 'Thêm cấu hình mới',
      'admin.settings.addKeyPlaceholder': 'VD: MAX_FILE_SIZE',
      'admin.settings.addValuePlaceholder': 'VD: 10MB',
      'admin.settings.addDescPlaceholder': 'Kích thước file tối đa',
      'nav.loginSettings': 'Cài đặt đăng nhập'
    },
    en: {
      'settings.panel.title': 'Settings',
      'settings.panel.language': 'Language',
      'settings.panel.background': 'Background Theme',
      'settings.panel.brightness': 'Brightness',
      'settings.panel.fontSize': 'Font Size',
      'settings.panel.compactMode': 'Compact Mode',
      'settings.panel.compactDesc': 'Reduce padding and spacing',
      'settings.lang.vi': 'Vietnamese',
      'settings.lang.en': 'English',
      'settings.lang.zh': 'Chinese',
      'settings.lang.ja': 'Japanese',
      'settings.lang.ko': 'Korean',
      'settings.font.small': 'Small',
      'settings.font.medium': 'Medium',
      'settings.font.large': 'Large',
      'admin.settings.title': 'System Settings',
      'admin.settings.hero.badge': 'Admin Control Center',
      'admin.settings.hero.desc': 'Manage runtime settings, login security, and HRMS parameters.',
      'admin.settings.viewLogin': 'View login page',
      'admin.settings.total': 'Total settings',
      'admin.settings.loginVerification': 'Login verification code',
      'admin.settings.codeLengthMetric': 'Verification code characters',
      'admin.settings.loginSecurity': 'Login Security',
      'admin.settings.loginSecurityDesc': 'Changes here apply immediately to the login form.',
      'admin.settings.loginSectionTag': 'Login',
      'admin.settings.verificationCode': 'Verification code',
      'admin.settings.enabled': 'Enabled',
      'admin.settings.disabled': 'Disabled',
      'admin.settings.enabledHint': 'Keep this enabled to reduce automated sign-in attempts.',
      'admin.settings.codeLength': 'Code length',
      'admin.settings.codeLengthHint': 'Valid range: 4 to 8 characters.',
      'admin.settings.charset': 'Code character set',
      'admin.settings.charsetHint': 'The default set excludes confusing characters such as I, O, 0, and 1.',
      'admin.settings.loginPreview': 'Login preview',
      'admin.settings.sampleCode': 'Sample code',
      'admin.settings.previewHint': 'Admins can disable verification for internal demos, but production should keep it enabled.',
      'admin.settings.saveLogin': 'Save login security',
      'admin.settings.configList': 'Settings list',
      'admin.settings.configKey': 'Setting key',
      'admin.settings.configValue': 'Value',
      'admin.settings.description': 'Description',
      'admin.settings.valuePlaceholder': 'Enter value...',
      'admin.settings.noConfig': 'No settings yet',
      'admin.settings.saveAll': 'Save all changes',
      'admin.settings.addNew': 'Add new setting',
      'admin.settings.addKeyPlaceholder': 'E.g. MAX_FILE_SIZE',
      'admin.settings.addValuePlaceholder': 'E.g. 10MB',
      'admin.settings.addDescPlaceholder': 'Maximum file size',
      'nav.loginSettings': 'Login Settings'
    },
    zh: {
      'settings.panel.title': '设置',
      'settings.panel.language': '语言',
      'settings.panel.background': '背景主题',
      'settings.panel.brightness': '亮度',
      'settings.panel.fontSize': '字体大小',
      'settings.panel.compactMode': '紧凑模式',
      'settings.panel.compactDesc': '减少内边距和间距',
      'settings.lang.vi': '越南语',
      'settings.lang.en': '英语',
      'settings.lang.zh': '中文',
      'settings.lang.ja': '日语',
      'settings.lang.ko': '韩语',
      'settings.font.small': '小',
      'settings.font.medium': '中',
      'settings.font.large': '大',
      'admin.settings.title': '系统设置',
      'admin.settings.hero.badge': '管理员控制中心',
      'admin.settings.hero.desc': '管理运行配置、登录安全和 HRMS 参数。',
      'admin.settings.viewLogin': '查看登录页',
      'admin.settings.total': '设置总数',
      'admin.settings.loginVerification': '登录验证码',
      'admin.settings.codeLengthMetric': '验证码字符数',
      'admin.settings.loginSecurity': '登录安全',
      'admin.settings.loginSecurityDesc': '这里的更改会立即应用到登录表单。',
      'admin.settings.loginSectionTag': '登录',
      'admin.settings.verificationCode': '验证码',
      'admin.settings.enabled': '启用',
      'admin.settings.disabled': '关闭',
      'admin.settings.enabledHint': '建议启用以减少自动登录尝试。',
      'admin.settings.codeLength': '验证码长度',
      'admin.settings.codeLengthHint': '有效范围：4 到 8 个字符。',
      'admin.settings.charset': '验证码字符集',
      'admin.settings.charsetHint': '默认字符集已排除 I、O、0、1 等易混字符。',
      'admin.settings.loginPreview': '登录预览',
      'admin.settings.sampleCode': '示例验证码',
      'admin.settings.previewHint': '管理员可在内部演示时关闭验证码，但生产环境建议保持启用。',
      'admin.settings.saveLogin': '保存登录安全设置',
      'admin.settings.configList': '设置列表',
      'admin.settings.configKey': '设置键',
      'admin.settings.configValue': '值',
      'admin.settings.description': '描述',
      'admin.settings.valuePlaceholder': '输入值...',
      'admin.settings.noConfig': '暂无设置',
      'admin.settings.saveAll': '保存所有更改',
      'admin.settings.addNew': '新增设置',
      'admin.settings.addKeyPlaceholder': '例如：MAX_FILE_SIZE',
      'admin.settings.addValuePlaceholder': '例如：10MB',
      'admin.settings.addDescPlaceholder': '最大文件大小',
      'nav.loginSettings': '登录设置'
    },
    ja: {
      'settings.panel.title': '設定',
      'settings.panel.language': '言語',
      'settings.panel.background': '背景テーマ',
      'settings.panel.brightness': '明るさ',
      'settings.panel.fontSize': '文字サイズ',
      'settings.panel.compactMode': 'コンパクトモード',
      'settings.panel.compactDesc': '余白と間隔を減らします',
      'settings.lang.vi': 'ベトナム語',
      'settings.lang.en': '英語',
      'settings.lang.zh': '中国語',
      'settings.lang.ja': '日本語',
      'settings.lang.ko': '韓国語',
      'settings.font.small': '小',
      'settings.font.medium': '中',
      'settings.font.large': '大',
      'admin.settings.title': 'システム設定',
      'admin.settings.hero.badge': '管理者コントロールセンター',
      'admin.settings.hero.desc': '実行設定、ログインセキュリティ、HRMS パラメータを管理します。',
      'admin.settings.viewLogin': 'ログインページを表示',
      'admin.settings.total': '設定数',
      'admin.settings.loginVerification': 'ログイン確認コード',
      'admin.settings.codeLengthMetric': '確認コードの文字数',
      'admin.settings.loginSecurity': 'ログインセキュリティ',
      'admin.settings.loginSecurityDesc': 'ここでの変更はログインフォームにすぐ反映されます。',
      'admin.settings.loginSectionTag': 'ログイン',
      'admin.settings.verificationCode': '確認コード',
      'admin.settings.enabled': '有効',
      'admin.settings.disabled': '無効',
      'admin.settings.enabledHint': '自動ログイン試行を減らすため有効にしておくことを推奨します。',
      'admin.settings.codeLength': 'コード長',
      'admin.settings.codeLengthHint': '有効範囲：4～8文字。',
      'admin.settings.charset': 'コード文字セット',
      'admin.settings.charsetHint': '既定では I、O、0、1 など紛らわしい文字を除外しています。',
      'admin.settings.loginPreview': 'ログインプレビュー',
      'admin.settings.sampleCode': 'サンプルコード',
      'admin.settings.previewHint': '内部デモでは無効にできますが、本番環境では有効のままにしてください。',
      'admin.settings.saveLogin': 'ログインセキュリティを保存',
      'admin.settings.configList': '設定一覧',
      'admin.settings.configKey': '設定キー',
      'admin.settings.configValue': '値',
      'admin.settings.description': '説明',
      'admin.settings.valuePlaceholder': '値を入力...',
      'admin.settings.noConfig': '設定はまだありません',
      'admin.settings.saveAll': 'すべての変更を保存',
      'admin.settings.addNew': '新しい設定を追加',
      'admin.settings.addKeyPlaceholder': '例：MAX_FILE_SIZE',
      'admin.settings.addValuePlaceholder': '例：10MB',
      'admin.settings.addDescPlaceholder': '最大ファイルサイズ',
      'nav.loginSettings': 'ログイン設定'
    },
    ko: {
      'settings.panel.title': '설정',
      'settings.panel.language': '언어',
      'settings.panel.background': '배경 테마',
      'settings.panel.brightness': '밝기',
      'settings.panel.fontSize': '글자 크기',
      'settings.panel.compactMode': '간결 모드',
      'settings.panel.compactDesc': '여백과 간격을 줄입니다',
      'settings.lang.vi': '베트남어',
      'settings.lang.en': '영어',
      'settings.lang.zh': '중국어',
      'settings.lang.ja': '일본어',
      'settings.lang.ko': '한국어',
      'settings.font.small': '작게',
      'settings.font.medium': '보통',
      'settings.font.large': '크게',
      'admin.settings.title': '시스템 설정',
      'admin.settings.hero.badge': '관리자 제어 센터',
      'admin.settings.hero.desc': '운영 설정, 로그인 보안, HRMS 매개변수를 관리합니다.',
      'admin.settings.viewLogin': '로그인 페이지 보기',
      'admin.settings.total': '전체 설정',
      'admin.settings.loginVerification': '로그인 인증 코드',
      'admin.settings.codeLengthMetric': '인증 코드 문자 수',
      'admin.settings.loginSecurity': '로그인 보안',
      'admin.settings.loginSecurityDesc': '여기 변경 사항은 로그인 폼에 즉시 적용됩니다.',
      'admin.settings.loginSectionTag': '로그인',
      'admin.settings.verificationCode': '인증 코드',
      'admin.settings.enabled': '켜짐',
      'admin.settings.disabled': '꺼짐',
      'admin.settings.enabledHint': '자동 로그인 시도를 줄이려면 켜두는 것을 권장합니다.',
      'admin.settings.codeLength': '코드 길이',
      'admin.settings.codeLengthHint': '유효 범위: 4~8자.',
      'admin.settings.charset': '코드 문자 집합',
      'admin.settings.charsetHint': '기본값은 I, O, 0, 1처럼 헷갈리는 문자를 제외합니다.',
      'admin.settings.loginPreview': '로그인 미리보기',
      'admin.settings.sampleCode': '샘플 코드',
      'admin.settings.previewHint': '내부 데모에서는 인증 코드를 끌 수 있지만 운영 환경에서는 켜두는 것이 좋습니다.',
      'admin.settings.saveLogin': '로그인 보안 저장',
      'admin.settings.configList': '설정 목록',
      'admin.settings.configKey': '설정 키',
      'admin.settings.configValue': '값',
      'admin.settings.description': '설명',
      'admin.settings.valuePlaceholder': '값 입력...',
      'admin.settings.noConfig': '아직 설정이 없습니다',
      'admin.settings.saveAll': '모든 변경 저장',
      'admin.settings.addNew': '새 설정 추가',
      'admin.settings.addKeyPlaceholder': '예: MAX_FILE_SIZE',
      'admin.settings.addValuePlaceholder': '예: 10MB',
      'admin.settings.addDescPlaceholder': '최대 파일 크기',
      'nav.loginSettings': '로그인 설정'
    }
  };

  Object.assign(UI_TEXT.vi, {
    'settings.panel.title': 'Cài đặt',
    'settings.panel.language': 'Ngôn ngữ',
    'settings.panel.background': 'Giao diện nền',
    'settings.panel.brightness': 'Độ sáng',
    'settings.panel.fontSize': 'Cỡ chữ',
    'settings.panel.compactMode': 'Chế độ gọn',
    'settings.panel.compactDesc': 'Giảm khoảng cách hiển thị',
    'settings.lang.vi': 'Tiếng Việt',
    'settings.lang.zh': '中文',
    'settings.lang.ja': '日本語',
    'settings.lang.ko': '한국어',
    'settings.font.small': 'Nhỏ',
    'settings.font.medium': 'Vừa',
    'settings.font.large': 'Lớn',
    'admin.settings.title': 'Cài đặt hệ thống',
    'admin.settings.hero.badge': 'Trung tâm điều khiển Admin',
    'admin.settings.hero.desc': 'Quản lý cấu hình vận hành, bảo mật đăng nhập và các tham số HRMS.',
    'admin.settings.viewLogin': 'Xem trang đăng nhập',
    'admin.settings.total': 'Tổng cấu hình',
    'admin.settings.loginVerification': 'Mã xác nhận đăng nhập',
    'admin.settings.codeLengthMetric': 'Ký tự trong mã xác nhận',
    'admin.settings.loginSecurity': 'Bảo mật đăng nhập',
    'admin.settings.loginSecurityDesc': 'Các thay đổi ở đây áp dụng ngay cho form login.',
    'admin.settings.loginSectionTag': 'Đăng nhập',
    'admin.settings.verificationCode': 'Mã xác nhận',
    'admin.settings.enabled': 'Bật',
    'admin.settings.disabled': 'Tắt',
    'admin.settings.enabledHint': 'Nên bật để giảm đăng nhập tự động.',
    'admin.settings.codeLength': 'Độ dài mã',
    'admin.settings.codeLengthHint': 'Giới hạn hợp lệ: 4 đến 8 ký tự.',
    'admin.settings.charset': 'Bộ ký tự sinh mã',
    'admin.settings.charsetHint': 'Đã bỏ các ký tự dễ nhầm như I, O, 0, 1 trong mặc định.',
    'admin.settings.loginPreview': 'Xem trước đăng nhập',
    'admin.settings.sampleCode': 'Mã mẫu',
    'admin.settings.previewHint': 'Admin có thể tắt mã xác nhận khi demo nội bộ, nhưng môi trường thật nên bật.',
    'admin.settings.saveLogin': 'Lưu cài đặt đăng nhập',
    'admin.settings.configList': 'Danh sách cài đặt',
    'admin.settings.configKey': 'Khóa cài đặt',
    'admin.settings.configValue': 'Giá trị',
    'admin.settings.description': 'Mô tả',
    'admin.settings.valuePlaceholder': 'Nhập giá trị...',
    'admin.settings.noConfig': 'Chưa có cài đặt',
    'admin.settings.saveAll': 'Lưu tất cả thay đổi',
    'admin.settings.addNew': 'Thêm cài đặt mới',
    'admin.settings.addKeyPlaceholder': 'Ví dụ: MAX_FILE_SIZE',
    'admin.settings.addValuePlaceholder': 'Ví dụ: 10MB',
    'admin.settings.addDescPlaceholder': 'Kích thước file tối đa',
    'nav.loginSettings': 'Cài đặt đăng nhập'
  });

  Object.assign(UI_TEXT.vi, {
    'settings.panel.title': 'C\u00e0i \u0111\u1eb7t',
    'settings.panel.language': 'Ng\u00f4n ng\u1eef',
    'settings.panel.background': 'Giao di\u1ec7n n\u1ec1n',
    'settings.panel.brightness': '\u0110\u1ed9 s\u00e1ng',
    'settings.panel.fontSize': 'C\u1ee1 ch\u1eef',
    'settings.panel.compactMode': 'Ch\u1ebf \u0111\u1ed9 g\u1ecdn',
    'settings.panel.compactDesc': 'Gi\u1ea3m kho\u1ea3ng c\u00e1ch hi\u1ec3n th\u1ecb',
    'settings.lang.vi': 'Ti\u1ebfng Vi\u1ec7t',
    'settings.lang.zh': '\u4e2d\u6587',
    'settings.lang.ja': '\u65e5\u672c\u8a9e',
    'settings.lang.ko': '\ud55c\uad6d\uc5b4',
    'settings.font.small': 'Nh\u1ecf',
    'settings.font.medium': 'V\u1eeba',
    'settings.font.large': 'L\u1edbn',
    'nav.loginSettings': 'C\u00e0i \u0111\u1eb7t \u0111\u0103ng nh\u1eadp'
  });

  function uiText(lang, key) {
    return (UI_TEXT[lang] && UI_TEXT[lang][key]) || UI_TEXT.vi[key] || key;
  }

  function applyExplicitTranslations(lang) {
    document.documentElement.setAttribute('lang', lang);
    document.querySelectorAll('[data-i18n]').forEach(function(el) {
      el.textContent = uiText(lang, el.getAttribute('data-i18n'));
    });
    document.querySelectorAll('[data-i18n-placeholder]').forEach(function(el) {
      el.setAttribute('placeholder', uiText(lang, el.getAttribute('data-i18n-placeholder')));
    });
    document.querySelectorAll('[data-i18n-title]').forEach(function(el) {
      el.setAttribute('title', uiText(lang, el.getAttribute('data-i18n-title')));
    });
    document.querySelectorAll('[data-i18n-document-title]').forEach(function(el) {
      document.title = uiText(lang, el.getAttribute('data-i18n-document-title')) + ' - HRMS';
    });
  }

  function translatePage(lang) {
    if (lang === 'vi' || !DICT[lang]) return;
    var dict = DICT[lang];
    var keys = Object.keys(dict).sort(function(a,b){ return b.length - a.length; });

    var walker = document.createTreeWalker(document.body, NodeFilter.SHOW_TEXT, null, false);
    var nodes = [];
    var n;
    while ((n = walker.nextNode())) {
      var p = n.parentNode;
      if (p && p.nodeName !== 'SCRIPT' && p.nodeName !== 'STYLE' && n.nodeValue.trim()) {
        nodes.push(n);
      }
    }
    nodes.forEach(function(node) {
      var t = node.nodeValue;
      for (var i = 0; i < keys.length; i++) {
        var vi = keys[i];
        if (t.indexOf(vi) !== -1) {
          t = t.split(vi).join(dict[vi]);
        }
      }
      if (t !== node.nodeValue) node.nodeValue = t;
    });

    // Placeholders
    document.querySelectorAll('[placeholder]').forEach(function(el) {
      var ph = el.getAttribute('placeholder');
      for (var i = 0; i < keys.length; i++) {
        if (ph.indexOf(keys[i]) !== -1) ph = ph.split(keys[i]).join(dict[keys[i]]);
      }
      el.setAttribute('placeholder', ph);
    });
  }

  function fixVietnameseMojibake() {
    var replacements = {
      'H? so cá nhân': 'Hồ sơ cá nhân',
      'Ch?m công': 'Chấm công',
      'Ca làm vi?c': 'Ca làm việc',
      'Ngh? phép': 'Nghỉ phép',
      'Làm thêm gi?': 'Làm thêm giờ',
      'Phi?u luong': 'Phiếu lương',
      'Công vi?c': 'Công việc',
      'c?a tôi': 'của tôi',
      'K? nang': 'Kỹ năng',
      'Tài li?u': 'Tài liệu',
      'Tài s?n': 'Tài sản',
      'H? tr?': 'Hỗ trợ',
      'Tr? lý HR': 'Trợ lý HR',
      'dào t?o': 'đào tạo',
      'Ðang xu?t': 'Đăng xuất',
      'L?ch s?': 'Lịch sử',
      'T?ng phi?u luong': 'Tổng phiếu lương',
      'Luong th?c nh?n g?n nh?t': 'Lương thực nhận gần nhất',
      'Chua có': 'Chưa có',
      'B? ph?n nhân s?': 'Bộ phận nhân sự',
      'chua t?o': 'chưa tạo',
      'cho b?n': 'cho bạn',
      'liên h?': 'liên hệ',
      'du?c h? tr?': 'được hỗ trợ',
      'Nam ': 'Năm ',
      'Danh sách công vi?c du?c giao': 'Danh sách công việc được giao',
      'tr?ng thái th?c hi?n': 'trạng thái thực hiện',
      'T?ng công vi?c': 'Tổng công việc',
      'Ch? x? lý': 'Chờ xử lý',
      'Ðang làm': 'Đang làm',
      'Ðánh giá': 'Đánh giá',
      'Ðánh Giá KPI C?a Tôi': 'Đánh giá KPI của tôi',
      'Theo dõi hi?u su?t': 'Theo dõi hiệu suất',
      'k?t qu?': 'kết quả',
      't?ng k? dánh giá': 'từng kỳ đánh giá',
      'Chua có dánh giá nào': 'Chưa có đánh giá nào',
      'Các dánh giá KPI c?a b?n s? hi?n th? t?i dây': 'Các đánh giá KPI của bạn sẽ hiển thị tại đây',
      'qu?n lý hoàn thành dánh giá': 'quản lý hoàn thành đánh giá',
      'T?ng l?n dánh giá': 'Tổng lần đánh giá',
      'k? du?c ghi nh?n': 'kỳ được ghi nhận',
      'Ði?m trung bình': 'Điểm trung bình',
      'Ði?m KPI cao nh?t': 'Điểm KPI cao nhất',
      'di?m t?t nh?t d?t du?c': 'điểm tốt nhất đạt được',
      'Ðã du?c duy?t': 'Đã được duyệt',
      'k? dánh giá hoàn t?t': 'kỳ đánh giá hoàn tất',
      'Bi?u Ð? Ðánh Giá G?n Nh?t': 'Biểu đồ đánh giá gần nhất',
      'K?:': 'Kỳ:',
      'Thái d?': 'Thái độ',
      'Ði?m m?nh': 'Điểm mạnh',
      'C?n c?i thi?n': 'Cần cải thiện',
      'Ðánh giá b?i': 'Đánh giá bởi'
    };
    var keys = Object.keys(replacements);
    var walker = document.createTreeWalker(document.body, NodeFilter.SHOW_TEXT, null, false);
    var nodes = [];
    var node;
    while ((node = walker.nextNode())) {
      var parent = node.parentNode;
      if (parent && parent.nodeName !== 'SCRIPT' && parent.nodeName !== 'STYLE' && node.nodeValue.trim()) {
        nodes.push(node);
      }
    }
    nodes.forEach(function(textNode) {
      var value = textNode.nodeValue;
      keys.forEach(function(key) {
        value = value.split(key).join(replacements[key]);
      });
      textNode.nodeValue = value;
    });
    document.querySelectorAll('[placeholder], [title]').forEach(function(el) {
      ['placeholder', 'title'].forEach(function(attr) {
        var value = el.getAttribute(attr);
        if (!value) return;
        keys.forEach(function(key) {
          value = value.split(key).join(replacements[key]);
        });
        el.setAttribute(attr, value);
      });
    });
  }

  // ==================== BACKGROUND ====================
  function applyBackground(bgId) {
    var bg = BACKGROUNDS.find(function(b){ return b.id === bgId; }) || BACKGROUNDS[0];
    var sidebars = document.querySelectorAll('.sidebar, [class*="sidebar"]');
    sidebars.forEach(function(s) {
      s.style.background = bg.color;
    });
    setSetting('bg', bgId);
  }

  // ==================== FONT SIZE ====================
  function applyFontSize(size) {
    var sizes = { small: '13px', medium: '14px', large: '16px' };
    document.documentElement.style.fontSize = sizes[size] || '14px';
    setSetting('fontSize', size);
  }

  // ==================== COMPACT MODE ====================
  function applyCompactMode(enabled) {
    if (enabled === 'true' || enabled === true) {
      document.body.classList.add('hrms-compact');
      if (!document.getElementById('hrms-compact-style')) {
        var s = document.createElement('style');
        s.id = 'hrms-compact-style';
        s.textContent = '.hrms-compact .sidebar a{padding:6px 20px!important;} .hrms-compact .data-table td{padding:8px 12px!important;} .hrms-compact .stat-card{padding:16px!important;}';
        document.head.appendChild(s);
      }
    } else {
      document.body.classList.remove('hrms-compact');
    }
    setSetting('compact', enabled ? 'true' : 'false');
  }
  function applyBrightness(val) {
    document.documentElement.style.filter = 'brightness(' + val + ')';
    setSetting('brightness', val);
  }

  // ==================== SETTINGS PANEL UI ====================
  function localizeSettingsPanel(panel, lang) {
    var replacements = [
      ['Language', uiText(lang, 'settings.panel.language')],
      ['Background Theme', uiText(lang, 'settings.panel.background')],
      ['Brightness', uiText(lang, 'settings.panel.brightness')],
      ['Font Size', uiText(lang, 'settings.panel.fontSize')],
      ['Compact Mode', uiText(lang, 'settings.panel.compactMode')],
      ['Reduce padding & spacing', uiText(lang, 'settings.panel.compactDesc')]
    ];

    var walker = document.createTreeWalker(panel, NodeFilter.SHOW_TEXT, null, false);
    var nodes = [];
    var node;
    while ((node = walker.nextNode())) {
      nodes.push(node);
    }

    nodes.forEach(function(textNode) {
      var value = textNode.nodeValue;
      replacements.forEach(function(pair) {
        if (value.indexOf(pair[0]) !== -1) {
          value = pair[1];
        }
      });
      textNode.nodeValue = value;
    });
  }

  function createSettingsPanel() {
    // Remove existing
    var existing = document.getElementById('hrms-settings-panel');
    if (existing) existing.remove();

    var currentLang = getLang();
    var currentBg = getSetting('bg', 'default');
    var currentBrightness = getSetting('brightness', '1');
    var settingsScope = getScope();
    var scopeLabels = {
      global: 'Admin: ap dung toan he thong',
      'admin-dashboard': 'Admin: ap dung dashboard admin',
      'manager-dashboard': 'Chi ap dung dashboard MANAGER',
      'hiring-dashboard': 'Chi ap dung dashboard HIRING',
      'user-dashboard': 'Chi ap dung dashboard USER'
    };
    var settingsScopeLabel = scopeLabels[settingsScope] || 'Chi ap dung dashboard role nay';

    var langFlags = { vi: '🇻🇳', en: '🇬🇧', zh: '🇨🇳', ja: '🇯🇵', ko: '🇰🇷' };
    var langNames = { vi: 'Tiếng Việt', en: 'English', zh: '中文', ja: '日本語', ko: '한국어' };

    langFlags = { vi: 'VI', en: 'EN', zh: 'ZH', ja: 'JA', ko: 'KO' };
    langNames = {
      vi: uiText(currentLang, 'settings.lang.vi'),
      en: uiText(currentLang, 'settings.lang.en'),
      zh: uiText(currentLang, 'settings.lang.zh'),
      ja: uiText(currentLang, 'settings.lang.ja'),
      ko: uiText(currentLang, 'settings.lang.ko')
    };

    var panel = document.createElement('div');
    panel.id = 'hrms-settings-panel';
    panel.innerHTML = [
      '<div id="hrms-settings-overlay" style="position:fixed;inset:0;z-index:9998;background:rgba(0,0,0,0.5);" onclick="document.getElementById(\'hrms-settings-panel\').remove()"></div>',
      '<div style="position:fixed;right:22px;bottom:174px;z-index:10002;width:320px;background:#1e293b;border-radius:16px;box-shadow:0 20px 60px rgba(0,0,0,0.5);border:1px solid rgba(255,255,255,0.1);overflow:hidden;">',
        // Header
        '<div style="background:linear-gradient(135deg,#6366f1,#8b5cf6);padding:16px 20px;display:flex;align-items:center;justify-content:space-between;">',
          '<div style="display:flex;align-items:center;gap:10px;">',
            '<span style="font-size:1.3rem;">⚙️</span>',
            '<span style="color:white;font-weight:700;font-size:1rem;">' + uiText(currentLang, 'settings.panel.title') + '</span>',
          '</div>',
          '<button onclick="document.getElementById(\'hrms-settings-panel\').remove()" style="background:rgba(255,255,255,0.2);border:none;color:white;width:28px;height:28px;border-radius:50%;cursor:pointer;font-size:1rem;display:flex;align-items:center;justify-content:center;">✕</button>',
        '</div>',
        '<div style="padding:20px;max-height:70vh;overflow-y:auto;">',
          '<div style="margin-bottom:16px;padding:10px 12px;border-radius:12px;background:rgba(99,102,241,0.14);border:1px solid rgba(99,102,241,0.22);color:#c7d2fe;font-size:0.78rem;font-weight:600;">' + settingsScopeLabel + '</div>',

          // Language section
          '<div style="margin-bottom:20px;">',
            '<div style="color:#94a3b8;font-size:0.72rem;font-weight:700;text-transform:uppercase;letter-spacing:1px;margin-bottom:10px;">🌐 Language / 语言 / 言語</div>',
            '<div style="display:grid;grid-template-columns:1fr 1fr;gap:8px;" id="hrms-lang-grid">',
              Object.keys(langFlags).map(function(l) {
                var isActive = l === currentLang;
                return '<button data-lang="' + l + '" onclick="HRMS.settings.setLang(\'' + l + '\')" style="' +
                  'background:' + (isActive ? 'linear-gradient(135deg,#6366f1,#8b5cf6)' : 'rgba(255,255,255,0.06)') + ';' +
                  'border:1px solid ' + (isActive ? '#6366f1' : 'rgba(255,255,255,0.1)') + ';' +
                  'color:' + (isActive ? 'white' : '#94a3b8') + ';' +
                  'border-radius:10px;padding:10px 12px;cursor:pointer;text-align:left;transition:all 0.2s;font-size:0.85rem;font-weight:' + (isActive ? '700' : '500') + ';">' +
                  langFlags[l] + ' ' + langNames[l] + '</button>';
              }).join(''),
            '</div>',
          '</div>',

          // Background section
          '<div style="margin-bottom:20px;">',
            '<div style="color:#94a3b8;font-size:0.72rem;font-weight:700;text-transform:uppercase;letter-spacing:1px;margin-bottom:10px;">🎨 Background Theme</div>',
            '<div style="display:grid;grid-template-columns:repeat(4,1fr);gap:8px;">',
              BACKGROUNDS.map(function(bg) {
                var isActive = bg.id === currentBg;
                return '<div onclick="HRMS.settings.setBg(\'' + bg.id + '\')" title="' + bg.label + '" style="' +
                  'width:100%;aspect-ratio:1;border-radius:10px;cursor:pointer;' +
                  'background:' + bg.color + ';' +
                  'border:2px solid ' + (isActive ? '#6366f1' : 'transparent') + ';' +
                  'box-shadow:' + (isActive ? '0 0 0 2px rgba(99,102,241,0.5)' : 'none') + ';' +
                  'transition:all 0.2s;position:relative;">' +
                  (isActive ? '<div style="position:absolute;inset:0;display:flex;align-items:center;justify-content:center;color:white;font-size:1rem;">✓</div>' : '') +
                  '</div>';
              }).join(''),
            '</div>',
          '</div>',

          // Brightness section
          '<div style="margin-bottom:16px;">',
            '<div style="color:#94a3b8;font-size:0.72rem;font-weight:700;text-transform:uppercase;letter-spacing:1px;margin-bottom:10px;">☀️ Brightness</div>',
            '<div style="display:flex;align-items:center;gap:12px;">',
              '<span style="color:#64748b;font-size:0.8rem;">🌑</span>',
              '<input type="range" id="hrms-brightness-slider" min="0.5" max="1.2" step="0.05" value="' + currentBrightness + '" ' +
                'oninput="HRMS.settings.setBrightness(this.value)" ' +
                'style="flex:1;accent-color:#6366f1;cursor:pointer;">',
              '<span style="color:#64748b;font-size:0.8rem;">☀️</span>',
              '<span id="hrms-brightness-val" style="color:#94a3b8;font-size:0.78rem;min-width:36px;">' + Math.round(currentBrightness * 100) + '%</span>',
            '</div>',
          '</div>',

          // Font size section
          '<div style="margin-bottom:16px;">',
            '<div style="color:#94a3b8;font-size:0.72rem;font-weight:700;text-transform:uppercase;letter-spacing:1px;margin-bottom:10px;">🔤 Font Size</div>',
            '<div style="display:flex;gap:8px;">',
              ['small','medium','large'].map(function(s) {
                var isActive = getSetting('fontSize','medium') === s;
                var labels = {
                  small: uiText(currentLang, 'settings.font.small'),
                  medium: uiText(currentLang, 'settings.font.medium'),
                  large: uiText(currentLang, 'settings.font.large')
                };
                var sizes = {small:'0.78rem',medium:'0.85rem',large:'0.95rem'};
                return '<button onclick="HRMS.settings.setFontSize(\'' + s + '\')" style="' +
                  'flex:1;padding:8px;border-radius:8px;cursor:pointer;font-size:' + sizes[s] + ';' +
                  'background:' + (isActive ? 'linear-gradient(135deg,#6366f1,#8b5cf6)' : 'rgba(255,255,255,0.06)') + ';' +
                  'border:1px solid ' + (isActive ? '#6366f1' : 'rgba(255,255,255,0.1)') + ';' +
                  'color:' + (isActive ? 'white' : '#94a3b8') + ';font-weight:' + (isActive ? '700' : '500') + ';">' +
                  labels[s] + '</button>';
              }).join(''),
            '</div>',
          '</div>',

          // Compact mode
          '<div style="margin-bottom:8px;">',
            '<div style="display:flex;align-items:center;justify-content:space-between;">',
              '<div>',
                '<div style="color:#e2e8f0;font-size:0.88rem;font-weight:600;">📐 Compact Mode</div>',
                '<div style="color:#64748b;font-size:0.75rem;margin-top:2px;">Reduce padding & spacing</div>',
              '</div>',
              '<label style="position:relative;display:inline-block;width:44px;height:24px;cursor:pointer;">',
                '<input type="checkbox" id="hrms-compact-toggle" ' + (getSetting('compact','false')==='true' ? 'checked' : '') + ' ' +
                  'onchange="HRMS.settings.setCompact(this.checked)" ' +
                  'style="opacity:0;width:0;height:0;">',
                '<span style="position:absolute;inset:0;background:' + (getSetting('compact','false')==='true' ? '#6366f1' : 'rgba(255,255,255,0.15)') + ';border-radius:24px;transition:0.3s;">' +
                  '<span style="position:absolute;left:' + (getSetting('compact','false')==='true' ? '22px' : '2px') + ';top:2px;width:20px;height:20px;background:white;border-radius:50%;transition:0.3s;"></span>' +
                '</span>',
              '</label>',
            '</div>',
          '</div>',

        '</div>',
      '</div>',
    ].join('');

    document.body.appendChild(panel);
    localizeSettingsPanel(panel, currentLang);
    cleanSettingsPanelText(panel, currentLang);
  }

  function cleanSettingsPanelText(panel, lang) {
    var labels = panel.querySelectorAll('div[style*="text-transform:uppercase"]');
    if (labels[0]) labels[0].textContent = uiText(lang, 'settings.panel.language');
    if (labels[1]) labels[1].textContent = uiText(lang, 'settings.panel.background');
    if (labels[2]) labels[2].textContent = uiText(lang, 'settings.panel.brightness');
    if (labels[3]) labels[3].textContent = uiText(lang, 'settings.panel.fontSize');

    var compactToggle = panel.querySelector('#hrms-compact-toggle');
    if (compactToggle) {
      var compactWrap = compactToggle.closest('div[style*="justify-content:space-between"]');
      if (compactWrap) {
        var textBox = compactWrap.firstElementChild;
        if (textBox && textBox.children.length >= 2) {
          textBox.children[0].textContent = uiText(lang, 'settings.panel.compactMode');
          textBox.children[1].textContent = uiText(lang, 'settings.panel.compactDesc');
        }
      }
    }
  }

  // ==================== GEAR BUTTON ====================
  function injectGearButton() {
    if (document.getElementById('hrms-gear-btn')) return;

    var btn = document.createElement('button');
    btn.id = 'hrms-gear-btn';
    btn.innerHTML = '<i class="bi bi-gear-fill" aria-hidden="true"></i>';
    btn.title = 'Settings';
    btn.setAttribute('onclick', 'HRMS.settings.openPanel()');
    btn.style.cssText = [
      'position:fixed',
      'bottom:112px',
      'right:22px',
      'z-index:9998',
      'width:52px',
      'height:52px',
      'border-radius:50%',
      'background:linear-gradient(135deg,#f59e0b,#f97316)',
      'border:none',
      'color:white',
      'font-size:1.15rem',
      'cursor:pointer',
      'box-shadow:0 12px 28px rgba(249,115,22,0.36)',
      'transition:all 0.2s',
      'display:flex',
      'align-items:center',
      'justify-content:center',
    ].join(';');

    btn.addEventListener('mouseenter', function() {
      this.style.transform = 'scale(1.1) rotate(30deg)';
      this.style.boxShadow = '0 16px 34px rgba(249,115,22,0.5)';
    });
    btn.addEventListener('mouseleave', function() {
      this.style.transform = 'scale(1) rotate(0deg)';
      this.style.boxShadow = '0 12px 28px rgba(249,115,22,0.36)';
    });

    document.body.appendChild(btn);
  }

  // ==================== INIT ====================
  function init() {
    fixVietnameseMojibake();

    if (getScope() === 'global' && window.location.pathname.indexOf('/admin') !== 0) {
      return;
    }

    var lang = getLang();

    // Apply saved settings
    applyBackground(getSetting('bg', 'default'));
    applyBrightness(getSetting('brightness', '1'));
    applyFontSize(getSetting('fontSize', 'medium'));
    applyCompactMode(getSetting('compact', 'false'));

    // Translate if not VI
    if (lang !== 'vi') {
      translatePage(lang);
    }

    applyExplicitTranslations(lang);

    // Inject gear button
    injectGearButton();
  }

  // ==================== PUBLIC API ====================
  window.HRMS = window.HRMS || {};
  window.HRMS.settings = {
    openPanel: function() { createSettingsPanel(); },

    setLang: function(lang) {
      setSetting('lang', lang);
      var url = new URL(window.location.href);
      url.searchParams.set('lang', lang);
      window.location.href = url.toString();
    },

    setBg: function(bgId) {
      applyBackground(bgId);
      createSettingsPanel();
    },

    setBrightness: function(val) {
      applyBrightness(val);
      var el = document.getElementById('hrms-brightness-val');
      if (el) el.textContent = Math.round(val * 100) + '%';
    },

    setFontSize: function(size) {
      applyFontSize(size);
      createSettingsPanel();
    },

    setCompact: function(enabled) {
      applyCompactMode(enabled);
    }
  };

  // Auto-init
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();
