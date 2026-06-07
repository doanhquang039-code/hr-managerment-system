/**
 * HRMS UI Enhancements
 * - Breadcrumb auto-generation
 * - Quick Actions menu
 * - Toast notifications
 * - Keyboard shortcuts help
 * - Back-to-top button
 */
(function () {
  'use strict';

  var ADMIN_MENU_I18N = {
    vi: {
      dashboard: 'Dashboard', profile: 'Profile', employees: 'Nhân viên', managerUsers: 'Quản lý Manager',
      hiringUsers: 'Hiring user', normalUsers: 'Nhân viên user', groups: 'Groups', passwordResets: 'Password resets',
      departments: 'Phòng ban', allDepartments: 'Tất cả phòng ban', hrDept: 'Phòng Nhân Sự',
      techDept: 'Phòng Kỹ Thuật', salesDept: 'Phòng Kinh Doanh', positions: 'Chức vụ',
      contracts: 'Hợp đồng', allContracts: 'Tất cả hợp đồng', fullTimeStaff: 'Nhân viên chính thức',
      partTimeStaff: 'Nhân viên part-time', internStaff: 'Nhân viên thực tập', payroll: 'Bảng lương',
      payments: 'Thanh toán', allPayments: 'Tất cả thanh toán', pendingProcess: 'Chờ xử lý',
      completed: 'Hoàn thành', failed: 'Thất bại', documents: 'Tài liệu', leaves: 'Nghỉ phép',
      allLeaves: 'Tất cả đơn nghỉ', pendingApproval: 'Chờ duyệt', approved: 'Đã duyệt',
      rejected: 'Bị từ chối', attendance: 'Chấm công', allAttendance: 'Tất cả chấm công',
      present: 'Có mặt', late: 'Đi muộn', earlyLeave: 'Về sớm', absent: 'Vắng mặt',
      tasks: 'Công việc', viewAll: 'Xem all', overtime: 'Tăng ca', noOvertime: 'Không tăng ca',
      assignments: 'Phân công', pending: 'Chờ', inProgress: 'Đang làm', shifts: 'Ca làm việc',
      allShifts: 'Tất cả ca', morning: 'Ca sáng', afternoon: 'Ca chiều', evening: 'Ca tối',
      night: 'Ca đêm', kpiReviews: 'Đánh giá KPI', kpiGoals: 'Mục tiêu KPI', skills: 'Kỹ năng NV',
      allSkills: 'Tất cả kỹ năng', training: 'Đào tạo', allTraining: 'Tất cả chương trình',
      planned: 'Kế hoạch', running: 'Đang diễn ra', cancelled: 'Đã hủy', videos: 'Video đào tạo',
      allVideos: 'Tất cả video', technical: 'Kỹ thuật', softSkills: 'Kỹ năng mềm', compliance: 'Quy định',
      expenses: 'Chi phí', allExpenses: 'Tất cả chi phí', paid: 'Đã thanh toán', assets: 'Tài sản',
      sales: 'Bán hàng', overview: 'Tổng quan', products: 'Sản phẩm', customers: 'Khách hàng',
      orders: 'Đơn hàng', announcements: 'Thông báo', reports: 'Báo cáo', recruitment: 'Tuyển dụng',
      allJobs: 'Tất cả tin tuyển', addJob: 'Đăng tin mới', official: 'Chính thức', security: 'Bảo vệ',
      courses: 'LMS - Khóa học', survey: 'Khảo sát', minigame: 'Minigame', minigamePlay: 'Chơi minigame',
      minigameTop: 'Top user/hiring', recognition: 'Vinh danh', allRecognition: 'Tất cả vinh danh',
      thanks: 'Cảm ơn', greatJob: 'Làm tốt', teamPlayer: 'Đồng đội', leadership: 'Lãnh đạo',
      onboarding: 'Onboarding', onboardingAll: 'Tất cả checklist', waiting: 'Đang chờ', paperwork: 'Hồ sơ', trainingCat: 'Đào tạo',
      analytics: 'Phân tích', system: 'Hệ thống', audit: 'Nhật ký', settings: 'Cài đặt', logout: 'Đăng xuất'
    },
    en: {
      dashboard: 'Dashboard', profile: 'Profile', employees: 'Employees', managerUsers: 'Managers',
      hiringUsers: 'Hiring users', normalUsers: 'Employee users', groups: 'Groups', passwordResets: 'Password resets',
      departments: 'Departments', allDepartments: 'All departments', hrDept: 'HR Department',
      techDept: 'Engineering Department', salesDept: 'Sales Department', positions: 'Positions',
      contracts: 'Contracts', allContracts: 'All contracts', fullTimeStaff: 'Full-time staff',
      partTimeStaff: 'Part-time staff', internStaff: 'Interns', payroll: 'Payroll',
      payments: 'Payments', allPayments: 'All payments', pendingProcess: 'Pending',
      completed: 'Completed', failed: 'Failed', documents: 'Documents', leaves: 'Leave requests',
      allLeaves: 'All leave requests', pendingApproval: 'Pending approval', approved: 'Approved',
      rejected: 'Rejected', attendance: 'Attendance', allAttendance: 'All attendance',
      present: 'Present', late: 'Late', earlyLeave: 'Early leave', absent: 'Absent',
      tasks: 'Tasks', viewAll: 'View all', overtime: 'Overtime', noOvertime: 'No overtime',
      assignments: 'Assignments', pending: 'Pending', inProgress: 'In progress', shifts: 'Work shifts',
      allShifts: 'All shifts', morning: 'Morning shift', afternoon: 'Afternoon shift', evening: 'Evening shift',
      night: 'Night shift', kpiReviews: 'KPI Reviews', kpiGoals: 'KPI Goals', skills: 'Employee skills',
      allSkills: 'All skills', training: 'Training', allTraining: 'All programs',
      planned: 'Planned', running: 'In progress', cancelled: 'Cancelled', videos: 'Training videos',
      allVideos: 'All videos', technical: 'Technical', softSkills: 'Soft skills', compliance: 'Compliance',
      expenses: 'Expenses', allExpenses: 'All expenses', paid: 'Paid', assets: 'Assets',
      sales: 'Sales', overview: 'Overview', products: 'Products', customers: 'Customers',
      orders: 'Orders', announcements: 'Announcements', reports: 'Reports', recruitment: 'Recruitment',
      allJobs: 'All job posts', addJob: 'Create job post', official: 'Full-time', security: 'Security',
      courses: 'LMS - Courses', survey: 'Surveys', minigame: 'Minigame', minigamePlay: 'Play minigame',
      minigameTop: 'Top user/hiring', recognition: 'Recognition', allRecognition: 'All recognition',
      thanks: 'Thank you', greatJob: 'Great job', teamPlayer: 'Team player', leadership: 'Leadership',
      onboarding: 'Onboarding', onboardingAll: 'All checklists', waiting: 'Pending', paperwork: 'Paperwork', trainingCat: 'Training',
      analytics: 'Analytics', system: 'System', audit: 'Audit log', settings: 'Settings', logout: 'Logout'
    },
    zh: {
      dashboard: '仪表盘', profile: '个人资料', employees: '员工', managerUsers: '经理用户',
      hiringUsers: '招聘用户', normalUsers: '普通员工', groups: '群组', passwordResets: '密码重置',
      departments: '部门', allDepartments: '所有部门', hrDept: '人力资源部',
      techDept: '技术部', salesDept: '销售部', positions: '职位', contracts: '合同',
      allContracts: '所有合同', fullTimeStaff: '正式员工', partTimeStaff: '兼职员工',
      internStaff: '实习生', payroll: '薪资', payments: '付款', allPayments: '所有付款',
      pendingProcess: '待处理', completed: '已完成', failed: '失败', documents: '文档',
      leaves: '请假', allLeaves: '所有请假单', pendingApproval: '待审批', approved: '已批准',
      rejected: '已拒绝', attendance: '考勤', allAttendance: '所有考勤', present: '出勤',
      late: '迟到', earlyLeave: '早退', absent: '缺勤', tasks: '任务', viewAll: '查看全部',
      overtime: '加班', noOvertime: '非加班', assignments: '分配', pending: '待处理',
      inProgress: '进行中', shifts: '班次', allShifts: '所有班次', morning: '早班',
      afternoon: '午班', evening: '晚班', night: '夜班', kpiReviews: 'KPI 评估',
      kpiGoals: 'KPI 目标', skills: '员工技能', allSkills: '所有技能', training: '培训',
      allTraining: '所有课程', planned: '计划中', running: '进行中', cancelled: '已取消',
      videos: '培训视频', allVideos: '所有视频', technical: '技术', softSkills: '软技能',
      compliance: '合规', expenses: '费用', allExpenses: '所有费用', paid: '已付款',
      assets: '资产', sales: '销售', overview: '总览', products: '产品', customers: '客户',
      orders: '订单', announcements: '公告', reports: '报告', recruitment: '招聘',
      allJobs: '所有招聘信息', addJob: '发布招聘', official: '正式', security: '安保',
      courses: 'LMS - 课程', survey: '调查', minigame: '小游戏', minigamePlay: '玩小游戏',
      minigameTop: '用户/招聘排名', recognition: '表彰', allRecognition: '所有表彰',
      thanks: '感谢', greatJob: '表现优秀', teamPlayer: '团队成员', leadership: '领导力',
      onboarding: '入职', onboardingAll: '所有清单', waiting: '等待中', paperwork: '资料', trainingCat: '培训',
      analytics: '分析', system: '系统', audit: '日志', settings: '设置', logout: '退出'
    },
    ja: {
      dashboard: 'ダッシュボード', profile: 'プロフィール', employees: '従業員', managerUsers: 'マネージャー',
      hiringUsers: '採用ユーザー', normalUsers: '一般ユーザー', groups: 'グループ', passwordResets: 'パスワード再設定',
      departments: '部署', allDepartments: 'すべての部署', hrDept: '人事部', techDept: '技術部',
      salesDept: '営業部', positions: '役職', contracts: '契約', allContracts: 'すべての契約',
      fullTimeStaff: '正社員', partTimeStaff: 'パートタイム', internStaff: 'インターン',
      payroll: '給与', payments: '支払い', allPayments: 'すべての支払い', pendingProcess: '処理待ち',
      completed: '完了', failed: '失敗', documents: '書類', leaves: '休暇', allLeaves: 'すべての休暇申請',
      pendingApproval: '承認待ち', approved: '承認済み', rejected: '却下', attendance: '勤怠',
      allAttendance: 'すべての勤怠', present: '出勤', late: '遅刻', earlyLeave: '早退',
      absent: '欠勤', tasks: 'タスク', viewAll: 'すべて表示', overtime: '残業', noOvertime: '残業なし',
      assignments: '割り当て', pending: '保留', inProgress: '進行中', shifts: 'シフト',
      allShifts: 'すべてのシフト', morning: '朝シフト', afternoon: '午後シフト', evening: '夕方シフト',
      night: '夜勤', kpiReviews: 'KPI評価', kpiGoals: 'KPI目標', skills: '従業員スキル',
      allSkills: 'すべてのスキル', training: '研修', allTraining: 'すべてのプログラム',
      planned: '計画中', running: '進行中', cancelled: 'キャンセル済み', videos: '研修動画',
      allVideos: 'すべての動画', technical: '技術', softSkills: 'ソフトスキル', compliance: '規定',
      expenses: '経費', allExpenses: 'すべての経費', paid: '支払い済み', assets: '資産',
      sales: '販売', overview: '概要', products: '商品', customers: '顧客', orders: '注文',
      announcements: 'お知らせ', reports: 'レポート', recruitment: '採用', allJobs: 'すべての求人',
      addJob: '求人を作成', official: '正社員', security: '警備', courses: 'LMS - コース',
      survey: 'アンケート', minigame: 'ミニゲーム', minigamePlay: 'ミニゲームをプレイ', minigameTop: 'ユーザー/採用ランキング',
      recognition: '表彰', allRecognition: 'すべての表彰', thanks: '感謝', greatJob: 'よくできました',
      teamPlayer: 'チームプレイヤー', leadership: 'リーダーシップ', onboarding: 'オンボーディング', onboardingAll: 'すべてのチェックリスト',
      waiting: '待機中', paperwork: '書類', trainingCat: '研修', analytics: '分析', system: 'システム',
      audit: 'ログ', settings: '設定', logout: 'ログアウト'
    },
    ko: {
      dashboard: '대시보드', profile: '프로필', employees: '직원', managerUsers: '매니저 사용자',
      hiringUsers: '채용 사용자', normalUsers: '일반 직원', groups: '그룹', passwordResets: '비밀번호 재설정',
      departments: '부서', allDepartments: '전체 부서', hrDept: '인사부', techDept: '기술부',
      salesDept: '영업부', positions: '직책', contracts: '계약', allContracts: '전체 계약',
      fullTimeStaff: '정규직', partTimeStaff: '파트타임', internStaff: '인턴', payroll: '급여',
      payments: '결제', allPayments: '전체 결제', pendingProcess: '처리 대기', completed: '완료',
      failed: '실패', documents: '문서', leaves: '휴가', allLeaves: '전체 휴가 신청',
      pendingApproval: '승인 대기', approved: '승인됨', rejected: '거절됨', attendance: '근태',
      allAttendance: '전체 근태', present: '출근', late: '지각', earlyLeave: '조퇴', absent: '결근',
      tasks: '업무', viewAll: '전체 보기', overtime: '초과근무', noOvertime: '초과근무 없음',
      assignments: '배정', pending: '대기', inProgress: '진행 중', shifts: '근무조',
      allShifts: '전체 근무조', morning: '오전 근무', afternoon: '오후 근무', evening: '저녁 근무',
      night: '야간 근무', kpiReviews: 'KPI 평가', kpiGoals: 'KPI 목표', skills: '직원 스킬',
      allSkills: '전체 스킬', training: '교육', allTraining: '전체 프로그램', planned: '계획됨',
      running: '진행 중', cancelled: '취소됨', videos: '교육 영상', allVideos: '전체 영상',
      technical: '기술', softSkills: '소프트 스킬', compliance: '규정', expenses: '비용',
      allExpenses: '전체 비용', paid: '지급 완료', assets: '자산', sales: '판매', overview: '개요',
      products: '제품', customers: '고객', orders: '주문', announcements: '공지', reports: '보고서',
      recruitment: '채용', allJobs: '전체 채용공고', addJob: '채용공고 등록', official: '정규직',
      security: '보안', courses: 'LMS - 과정', survey: '설문', minigame: '미니게임', minigamePlay: '미니게임 플레이',
      minigameTop: '사용자/채용 순위', recognition: '칭찬', allRecognition: '전체 칭찬',
      thanks: '감사', greatJob: '훌륭한 업무', teamPlayer: '팀 플레이어', leadership: '리더십',
      onboarding: '온보딩', onboardingAll: '전체 체크리스트', waiting: '대기 중', paperwork: '서류', trainingCat: '교육',
      analytics: '분석', system: '시스템', audit: '로그', settings: '설정', logout: '로그아웃'
    }
  };

  var ADMIN_LABEL_BY_MENU = {
    users: 'employees', departments: 'departments', contracts: 'contracts', payroll: 'payroll',
    payments: 'payments', leaves: 'leaves', attendance: 'attendance', tasks: 'tasks',
    assignments: 'assignments', shifts: 'shifts', skills: 'skills', training: 'training',
    videos: 'videos', expenses: 'expenses', sales: 'sales', hiring: 'recruitment',
    recruitment: 'recruitment', minigame: 'minigame', recognition: 'recognition',
    onboarding: 'onboarding', system: 'system'
  };

  var ADMIN_LABEL_BY_HREF = {
    '/admin/dashboard': 'dashboard', '/profile': 'profile', '/admin/users?role=MANAGER': 'managerUsers',
    '/hiring/users': 'hiringUsers', '/admin/users?role=USER': 'normalUsers', '/admin/groups': 'groups',
    '/admin/password-reset-requests': 'passwordResets', '/admin/departments': 'allDepartments',
    '/admin/users?departmentId=1': 'hrDept', '/admin/users?departmentId=2': 'techDept',
    '/admin/users?departmentId=3': 'salesDept', '/admin/positions': 'positions',
    '/admin/contracts': 'allContracts', '/admin/contracts?type=FULL_TIME': 'fullTimeStaff',
    '/admin/contracts?contractType=Nhân viên chính thức': 'fullTimeStaff', '/admin/contracts?type=PART_TIME': 'partTimeStaff',
    '/admin/contracts?contractType=Nhân viên part-time': 'partTimeStaff', '/admin/contracts?type=INTERN': 'internStaff',
    '/admin/contracts?contractType=Nhân viên thực tập': 'internStaff', '/admin/payroll': 'allDepartments',
    '/admin/payroll?departmentId=1': 'hrDept', '/admin/payroll?departmentId=2': 'techDept',
    '/admin/payroll?departmentId=3': 'salesDept', '/admin/payments': 'allPayments',
    '/admin/payments?status=PENDING': 'pendingProcess', '/admin/payments?status=PROCESSING': 'pendingProcess',
    '/admin/payments?status=COMPLETED': 'completed', '/admin/payments?status=FAILED': 'failed',
    '/admin/documents': 'documents', '/admin/leaves': 'allLeaves', '/admin/leaves?status=PENDING': 'pendingApproval',
    '/admin/leaves?status=APPROVED': 'approved', '/admin/leaves?status=REJECTED': 'rejected',
    '/admin/attendance': 'allAttendance', '/admin/attendance?status=PRESENT': 'present',
    '/admin/attendance?status=LATE': 'late', '/admin/attendance?status=EARLY_LEAVE': 'earlyLeave',
    '/admin/attendance?status=ABSENT': 'absent', '/admin/tasks': 'viewAll',
    '/admin/tasks?extraShift=true': 'overtime', '/admin/tasks?extraShift=false': 'noOvertime',
    '/admin/assignments': 'viewAll', '/admin/assignments?status=PENDING': 'pending',
    '/admin/assignments?status=IN_PROGRESS': 'inProgress', '/admin/assignments?status=COMPLETED': 'completed',
    '/admin/shifts': 'allShifts', '/admin/shifts?period=morning': 'morning',
    '/admin/shifts?period=afternoon': 'afternoon', '/admin/shifts?period=evening': 'evening',
    '/admin/shifts?period=night': 'night', '/admin/reviews': 'kpiReviews', '/admin/kpi': 'kpiGoals',
    '/admin/skills': 'allSkills', '/admin/skills?category=TECHNICAL': 'technical',
    '/admin/skills?category=LANGUAGE': 'Language', '/admin/skills?level=BEGINNER': 'Beginner',
    '/admin/skills?level=INTERMEDIATE': 'Intermediate', '/admin/skills?level=EXPERT': 'Expert',
    '/admin/training': 'allTraining', '/admin/training?status=PLANNED': 'planned',
    '/admin/training?status=IN_PROGRESS': 'running', '/admin/training?status=COMPLETED': 'completed',
    '/admin/training?status=CANCELLED': 'cancelled', '/admin/videos': 'allVideos',
    '/admin/videos?category=TECHNICAL': 'technical', '/admin/videos?category=SOFT_SKILL': 'softSkills',
    '/admin/videos?category=COMPLIANCE': 'compliance', '/admin/expenses': 'allExpenses',
    '/admin/expenses?status=PENDING': 'pendingApproval', '/admin/expenses?status=APPROVED': 'approved',
    '/admin/expenses?status=REJECTED': 'rejected', '/admin/expenses?status=PAID': 'paid',
    '/admin/assets': 'assets', '/sales': 'overview', '/sales/products': 'products',
    '/sales/customers': 'customers', '/sales/orders': 'orders', '/admin/announcements': 'announcements',
    '/admin/reports': 'reports', '/hiring/postings': 'allJobs', '/hiring/postings/add': 'addJob',
    '/hiring/postings?category=FULL_TIME': 'official', '/hiring/postings?category=PART_TIME': 'partTimeStaff',
    '/hiring/postings?category=INTERN': 'internStaff', '/hiring/postings?category=SECURITY': 'security',
    '/admin/courses': 'courses', '/admin/qrcode/list': 'QR Code', '/admin/engagement/surveys': 'survey',
    '/admin/minigame': 'minigamePlay', '/admin/minigame/top': 'minigameTop',
    '/admin/engagement/recognition': 'allRecognition', '/admin/engagement/recognition?type=THANK_YOU': 'thanks',
    '/admin/engagement/recognition?type=GREAT_JOB': 'greatJob', '/admin/engagement/recognition?type=TEAM_PLAYER': 'teamPlayer',
    '/admin/engagement/recognition?type=LEADERSHIP': 'leadership', '/admin/onboarding/checklists': 'onboardingAll',
    '/admin/onboarding/checklists?status=pending': 'waiting', '/admin/onboarding/checklists?status=completed': 'completed',
    '/admin/onboarding/checklists?category=PAPERWORK': 'paperwork', '/admin/onboarding/checklists?category=IT_SETUP': 'IT setup',
    '/admin/onboarding/checklists?category=TRAINING': 'trainingCat', '/admin/okr/objectives': 'OKR',
    '/admin/analytics/dashboard': 'analytics', '/admin/system-monitor': 'System Monitor',
    '/admin/cache': 'Cache & Email', '/admin/cloud': 'Cloud Storage', '/admin/audit-log': 'audit',
    '/admin/settings': 'settings'
  };

  var ADMIN_MAIN_LABEL_BY_HREF = {
    '/admin/dashboard': 'dashboard', '/profile': 'profile', '/admin/users': 'employees',
    '/admin/groups': 'groups', '/admin/password-reset-requests': 'passwordResets',
    '/admin/departments': 'departments', '/admin/positions': 'positions', '/admin/contracts': 'contracts',
    '/admin/payroll': 'payroll', '/admin/payments': 'payments', '/admin/documents': 'documents',
    '/admin/leaves': 'leaves', '/admin/attendance': 'attendance', '/admin/tasks': 'tasks',
    '/admin/assignments': 'assignments', '/admin/shifts': 'shifts', '/admin/reviews': 'kpiReviews',
    '/admin/kpi': 'kpiGoals', '/admin/skills': 'skills', '/admin/training': 'training',
    '/admin/videos': 'videos', '/admin/expenses': 'expenses', '/admin/assets': 'assets',
    '/sales': 'sales', '/admin/announcements': 'announcements', '/admin/reports': 'reports',
    '/hiring/postings': 'recruitment', '/admin/courses': 'courses', '/admin/qrcode/list': 'QR Code',
    '/admin/engagement/surveys': 'survey', '/admin/minigame': 'minigame',
    '/admin/engagement/recognition': 'recognition', '/admin/onboarding/checklists': 'onboarding',
    '/admin/okr/objectives': 'OKR', '/admin/analytics/dashboard': 'analytics',
    '/admin/system-monitor': 'System Monitor', '/admin/cache': 'Cache & Email',
    '/admin/cloud': 'Cloud Storage', '/admin/audit-log': 'audit', '/admin/settings': 'settings'
  };

  function currentLang() {
    var queryLang = new URLSearchParams(window.location.search).get('lang');
    var cookieMatch = document.cookie.match(/(?:^|;\s*)HRMS_LANG=([^;]+)/);
    var cookieLang = cookieMatch ? decodeURIComponent(cookieMatch[1]) : null;
    var storedLang = null;
    try { storedLang = localStorage.getItem('hrms_lang'); } catch (e) {}
    return (queryLang || cookieLang || storedLang || 'vi').split('_')[0].toLowerCase();
  }

  function adminT(key) {
    if (!key) return '';
    var lang = currentLang();
    var bundle = ADMIN_MENU_I18N[lang] || ADMIN_MENU_I18N.vi;
    return bundle[key] || ADMIN_MENU_I18N.en[key] || ADMIN_MENU_I18N.vi[key] || key;
  }

  function setMenuLabel(el, label) {
    if (!el || !label) return;
    var icons = Array.prototype.filter.call(el.children, function (child) {
      return child.tagName === 'I';
    });
    Array.prototype.slice.call(el.childNodes).forEach(function (node) {
      if (node.nodeType === 3) node.remove();
    });
    var span = Array.prototype.find.call(el.children, function (child) {
      return child.tagName === 'SPAN' && !child.classList.contains('caret') && !child.classList.contains('admin-caret');
    });
    if (!span) {
      span = document.createElement('span');
      if (icons.length) {
        el.insertBefore(span, icons[1] || null);
      } else {
        el.insertBefore(span, el.firstChild || null);
      }
    }
    span.textContent = label;
  }

  function translateAdminSidebar() {
    var sidebar = document.querySelector('.admin-sidebar, nav.sidebar, .sidebar');
    if (!sidebar) return;
    sidebar.querySelectorAll('.admin-parent').forEach(function (btn) {
      var id = btn.dataset.menu || btn.dataset.adminMenu;
      setMenuLabel(btn, adminT(ADMIN_LABEL_BY_MENU[id]));
    });
    sidebar.querySelectorAll('a[href]').forEach(function (a) {
      var href = a.getAttribute('href') || '';
      var isSubmenu = !!a.closest('.admin-submenu');
      var key = isSubmenu
        ? (ADMIN_LABEL_BY_HREF[href] || ADMIN_LABEL_BY_HREF[decodeURIComponent(href)] || ADMIN_LABEL_BY_HREF[href.split('?')[0]])
        : (ADMIN_MAIN_LABEL_BY_HREF[href.split('?')[0]] || ADMIN_LABEL_BY_HREF[href] || ADMIN_LABEL_BY_HREF[decodeURIComponent(href)]);
      if (key) setMenuLabel(a, adminT(key));
    });
    var logout = sidebar.querySelector('.admin-logout button');
    if (logout) setMenuLabel(logout, adminT('logout'));
  }

  function bindAdminLangButtons() {
    document.querySelectorAll('.admin-sidebar .lang-btn[data-lang], .admin-sidebar [data-lang]').forEach(function (btn) {
      if (btn.dataset.hrmsLangBound === 'true') return;
      btn.dataset.hrmsLangBound = 'true';
      var lang = btn.getAttribute('data-lang');
      var active = lang === currentLang();
      btn.classList.toggle('active', active);
      btn.addEventListener('click', function (e) {
        e.preventDefault();
        e.stopPropagation();
        var url = new URL(window.location.href);
        url.searchParams.set('lang', lang);
        try { localStorage.setItem('hrms_lang', lang); } catch (err) {}
        window.location.href = url.toString();
      });
    });
  }

  function injectAdminSidebarSync() {
    var path = window.location.pathname || '';
    if (path.indexOf('/admin') !== 0) return;

    var sidebar = document.querySelector('.admin-sidebar, nav.sidebar, .sidebar');
    if (!sidebar || sidebar.dataset.syncedAdminMenu === 'true') return;

    var menu = [
      ['link', '/admin/dashboard', 'bi-speedometer2', 'Dashboard'],
      ['link', '/profile', 'bi-person-circle', 'Profile'],
      ['parent', 'users', 'bi-people', 'Nhân viên',
        [['/admin/users?role=MANAGER', 'Quản lý'], ['/hiring/users', 'Hiring User'], ['/admin/users?role=USER', 'Nhân viên User']]],
      ['link', '/admin/groups', 'bi-diagram-3', 'Groups'],
      ['link', '/admin/password-reset-requests', 'bi-shield-lock', 'Password resets'],
      ['parent', 'departments', 'bi-building', 'Phòng ban',
        [['/admin/departments', 'Tất cả phòng ban'], ['/admin/users?departmentId=1', 'Phòng Nhân Sự'], ['/admin/users?departmentId=2', 'Phòng Kỹ Thuật'], ['/admin/users?departmentId=3', 'Phòng Kinh Doanh']]],
      ['link', '/admin/positions', 'bi-briefcase', 'Chức vụ'],
      ['hr'],
      ['parent', 'contracts', 'bi-file-text', 'Hợp đồng',
        [['/admin/contracts', 'Tất cả hợp đồng'], ['/admin/contracts?contractType=Nhân viên chính thức', 'Nhân viên chính thức'], ['/admin/contracts?contractType=Nhân viên part-time', 'Nhân viên part-time'], ['/admin/contracts?contractType=Nhân viên thực tập', 'Nhân viên thực tập']]],
      ['parent', 'payroll', 'bi-cash-coin', 'Bảng lương',
        [['/admin/payroll', 'Tất cả phòng ban'], ['/admin/payroll?departmentId=1', 'Phòng Nhân Sự'], ['/admin/payroll?departmentId=2', 'Phòng Kỹ Thuật'], ['/admin/payroll?departmentId=3', 'Phòng Kinh Doanh']]],
      ['parent', 'payments', 'bi-bank', 'Thanh toán',
        [['/admin/payments', 'Tất cả thanh toán'], ['/admin/payments?status=PENDING', 'Chờ xử lý'], ['/admin/payments?status=PROCESSING', 'Đang xử lý'], ['/admin/payments?status=COMPLETED', 'Hoàn thành'], ['/admin/payments?status=FAILED', 'Thất bại']]],
      ['link', '/admin/documents', 'bi-folder2-open', 'Tài liệu'],
      ['parent', 'leaves', 'bi-calendar-x', 'Nghỉ phép',
        [['/admin/leaves', 'Tất cả đơn nghỉ'], ['/admin/leaves?status=PENDING', 'Chờ duyệt'], ['/admin/leaves?status=APPROVED', 'Đã duyệt'], ['/admin/leaves?status=REJECTED', 'Bị từ chối']]],
      ['parent', 'attendance', 'bi-clipboard-check', 'Chấm công',
        [['/admin/attendance', 'Tất cả chấm công'], ['/admin/attendance?status=PRESENT', 'Có mặt'], ['/admin/attendance?status=LATE', 'Đi muộn'], ['/admin/attendance?status=EARLY_LEAVE', 'Về sớm'], ['/admin/attendance?status=ABSENT', 'Vắng mặt']]],
      ['parent', 'tasks', 'bi-list-task', 'Công việc',
        [['/admin/tasks', 'Xem all'], ['/admin/tasks?extraShift=true', 'Tăng ca'], ['/admin/tasks?extraShift=false', 'Không tăng ca']]],
      ['parent', 'assignments', 'bi-person-check', 'Phân công',
        [['/admin/assignments', 'Xem all'], ['/admin/assignments?status=IN_PROGRESS', 'Đang làm'], ['/admin/assignments?status=PENDING', 'Chờ'], ['/admin/assignments?status=COMPLETED', 'Hoàn thành']]],
      ['parent', 'shifts', 'bi-clock-history', 'Ca làm việc',
        [['/admin/shifts', 'Tất cả ca'], ['/admin/shifts?period=morning', 'Ca sáng'], ['/admin/shifts?period=afternoon', 'Ca chiều'], ['/admin/shifts?period=evening', 'Ca tối'], ['/admin/shifts?period=night', 'Ca đêm']]],
      ['hr'],
      ['link', '/admin/reviews', 'bi-star-half', 'Đánh giá KPI'],
      ['link', '/admin/kpi', 'bi-bullseye', 'Mục tiêu KPI'],
      ['link', '/admin/skills', 'bi-tools', 'Kỹ năng NV'],
      ['parent', 'training', 'bi-mortarboard', 'Đào tạo',
        [['/admin/training', 'Tất cả chương trình'], ['/admin/training?status=PLANNED', 'Kế hoạch'], ['/admin/training?status=IN_PROGRESS', 'Đang diễn ra'], ['/admin/training?status=COMPLETED', 'Hoàn thành'], ['/admin/training?status=CANCELLED', 'Đã hủy'], ['/admin/training?trainingType=INTERNAL', 'Internal'], ['/admin/training?trainingType=EXTERNAL', 'External'], ['/admin/training?trainingType=ONLINE', 'Online'], ['/admin/training?trainingType=WORKSHOP', 'Workshop']]],
      ['parent', 'videos', 'bi-collection-play', 'Video đào tạo',
        [['/admin/videos', 'Tất cả video'], ['/admin/videos?category=Onboarding', 'Onboarding'], ['/admin/videos?category=Kỹ năng mềm', 'Kỹ năng mềm'], ['/admin/videos?category=Kỹ thuật', 'Kỹ thuật'], ['/admin/videos?category=An toàn lao động', 'An toàn lao động']]],
      ['hr'],
      ['parent', 'expenses', 'bi-receipt', 'Chi phí',
        [['/admin/expenses', 'Tất cả chi phí'], ['/admin/expenses?status=PENDING', 'Chờ duyệt'], ['/admin/expenses?status=APPROVED', 'Đã duyệt'], ['/admin/expenses?status=REJECTED', 'Từ chối'], ['/admin/expenses?status=PAID', 'Đã thanh toán']]],
      ['link', '/admin/assets', 'bi-box-seam', 'Tài sản'],
      ['link', '/admin/announcements', 'bi-megaphone', 'Thông báo'],
      ['link', '/admin/reports', 'bi-bar-chart-line', 'Báo cáo'],
      ['link', '/admin/audit-log', 'bi-journal-text', 'Nhật ký'],
      ['parent', 'recruitment', 'bi-person-plus', 'Tuyển dụng',
        [['/hiring/postings', 'Tất cả tin tuyển'], ['/hiring/postings/add', 'Đăng tin mới'], ['/hiring/postings?category=FULL_TIME', 'Chính thức'], ['/hiring/postings?category=PART_TIME', 'Part-time'], ['/hiring/postings?category=INTERN', 'Intern'], ['/hiring/postings?category=JUNIOR', 'Junior'], ['/hiring/postings?category=SECURITY', 'Bảo vệ']]],
      ['hr'],
      ['link', '/admin/courses', 'bi-book', 'LMS - Khóa học'],
      ['link', '/admin/qrcode/list', 'bi-qr-code', 'QR Code'],
      ['link', '/admin/engagement/surveys', 'bi-clipboard-data', 'Khảo sát'],
      ['parent', 'recognition', 'bi-award', 'Vinh danh',
        [['/admin/engagement/recognition', 'Tất cả vinh danh'], ['/admin/engagement/recognition?type=THANK_YOU', 'Cảm ơn'], ['/admin/engagement/recognition?type=GREAT_JOB', 'Làm tốt'], ['/admin/engagement/recognition?type=TEAM_PLAYER', 'Đồng đội'], ['/admin/engagement/recognition?type=LEADERSHIP', 'Lãnh đạo']]],
      ['parent', 'onboarding', 'bi-list-check', 'Onboarding',
        [['/admin/onboarding/checklists', 'Tất cả checklist'], ['/admin/onboarding/checklists?status=pending', 'Đang chờ'], ['/admin/onboarding/checklists?status=completed', 'Hoàn thành'], ['/admin/onboarding/checklists?category=PAPERWORK', 'Hồ sơ'], ['/admin/onboarding/checklists?category=IT_SETUP', 'IT setup'], ['/admin/onboarding/checklists?category=TRAINING', 'Đào tạo']]],
      ['link', '/admin/okr/objectives', 'bi-graph-up', 'OKR'],
      ['link', '/admin/settings', 'bi-gear', 'Cài đặt'],
      ['link', '/admin/analytics/dashboard', 'bi-graph-up-arrow', 'Phân tích'],
      ['hr'],
      ['link', '/admin/cache', 'bi-lightning-charge', 'Cache & Email'],
      ['link', '/admin/cloud', 'bi-cloud', 'Cloud Storage']
    ];

    function repairText(value) {
      var text = String(value);
      try {
        return decodeURIComponent(escape(text));
      } catch (e) {
        return text;
      }
    }

    function esc(value) {
      return repairText(value).replace(/[&<>"']/g, function(ch) {
        return ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' })[ch];
      });
    }

    function linkHtml(href, icon, label) {
      return '<a href="' + esc(href) + '"><i class="bi ' + esc(icon) + '"></i><span>' + esc(label) + '</span></a>';
    }

    var csrfInput = sidebar.querySelector('form input[type="hidden"]');
    var csrfHtml = csrfInput
      ? '<input type="hidden" name="' + esc(csrfInput.getAttribute('name') || '') + '" value="' + esc(csrfInput.getAttribute('value') || '') + '">'
      : '';
    var html = '<div class="admin-brand"><span class="admin-brand-bolt">&#9889;</span> HRMS ADMIN</div><div class="admin-nav">';
    menu.forEach(function(item) {
      if (item[0] === 'hr') {
        html += '<hr>';
      } else if (item[0] === 'link') {
        html += linkHtml(item[1], item[2], item[3]);
      } else if (item[0] === 'parent') {
        html += '<button type="button" class="admin-parent" data-admin-menu="' + esc(item[1]) + '"><i class="bi ' + esc(item[2]) + '"></i><span>' + esc(item[3]) + '</span><i class="bi bi-caret-down-fill admin-caret"></i></button>';
        html += '<div class="admin-submenu" id="admin-menu-' + esc(item[1]) + '">';
        item[4].forEach(function(child) { html += '<a href="' + esc(child[0]) + '">' + esc(child[1]) + '</a>'; });
        html += '</div>';
      }
    });
    html += '</div><div class="admin-lang"><button type="button" class="lang-btn" data-lang="vi">VI</button><span>|</span><button type="button" class="lang-btn" data-lang="en">EN</button><span>|</span><button type="button" class="lang-btn" data-lang="zh">ZH</button><span>|</span><button type="button" class="lang-btn" data-lang="ja">JA</button><span>|</span><button type="button" class="lang-btn" data-lang="ko">KO</button></div>';
    html += '<form action="/logout" method="post" class="admin-logout">' + csrfHtml + '<button type="submit"><i class="bi bi-box-arrow-right me-1"></i>Đăng xuất</button></form>';

    sidebar.className = 'admin-sidebar';
    sidebar.dataset.syncedAdminMenu = 'true';
    sidebar.innerHTML = html;
    document.body.classList.add('admin-menu-synced');

    if (!document.getElementById('admin-menu-sync-style')) {
      var style = document.createElement('style');
      style.id = 'admin-menu-sync-style';
      style.textContent = [
        '.admin-sidebar{width:300px;min-height:100vh;position:fixed;inset:0 auto 0 0;z-index:1000;overflow-y:auto;background:linear-gradient(180deg,#0f172a 0%,#111827 52%,#172033 100%);border-right:1px solid rgba(148,163,184,.16);box-shadow:10px 0 28px rgba(15,23,42,.18);padding:18px;color:#dbe7f5}',
        '.admin-brand{min-height:54px;display:flex;align-items:center;justify-content:center;gap:10px;color:#fff;font-weight:800;letter-spacing:.08em;background:rgba(255,255,255,.04);border:1px solid rgba(255,255,255,.06);margin-bottom:18px}.admin-brand-bolt{color:#fb923c}',
        '.admin-nav{display:flex;flex-direction:column;gap:2px}.admin-nav a,.admin-nav button{width:100%;min-height:42px;border:0;background:transparent;color:#aab7c8;text-decoration:none;display:flex;align-items:center;gap:12px;padding:9px 14px;font-size:.95rem;font-weight:600;text-align:left;border-left:3px solid transparent}.admin-nav a:hover,.admin-nav button:hover,.admin-nav a.active,.admin-nav button.active{color:#fff;background:rgba(255,255,255,.08);border-left-color:#818cf8}.admin-nav i{width:18px;text-align:center;color:inherit}.admin-nav hr{border-color:rgba(148,163,184,.16);margin:10px 18px}',
        '.admin-parent{cursor:pointer}.admin-caret{margin-left:auto;width:auto!important;font-size:.68rem;transition:transform .18s}.admin-parent.open .admin-caret{transform:rotate(180deg)}.admin-submenu{display:none;padding:4px 0 8px 34px;background:rgba(15,23,42,.28)}.admin-submenu.open{display:block}.admin-submenu a{min-height:34px;padding:7px 12px;font-size:.84rem;border-left:0;color:#c7d2e1}',
        '.admin-lang{display:flex;align-items:center;justify-content:center;gap:8px;padding:12px 0}.admin-lang button{border:1px solid rgba(255,255,255,.16);background:rgba(255,255,255,.08);color:#cbd5e1;border-radius:8px;padding:5px 12px;font-size:.75rem;font-weight:800}.admin-lang span{opacity:.25}.admin-logout{padding:12px 6px 0}.admin-logout button{width:100%;min-height:36px;border:1px solid rgba(255,255,255,.75);color:#fff;background:transparent;border-radius:6px;font-weight:700}',
        'body.admin-menu-synced main:not(.admin-main){margin-left:300px!important;width:calc(100% - 300px)!important;max-width:none!important}.admin-main{margin-left:300px!important}',
        '@media(max-width:991px){.admin-sidebar{position:relative;width:100%;min-height:auto;max-height:460px}body.admin-menu-synced main:not(.admin-main),.admin-main{margin-left:0!important;width:100%!important}}'
      ].join('');
      document.head.appendChild(style);
    }

    sidebar.querySelectorAll('a[href]').forEach(function(a) {
      var target = a.getAttribute('href').split('?')[0];
      if (target && target !== '/' && path.indexOf(target) === 0) a.classList.add('active');
    });
    sidebar.querySelectorAll('.admin-parent').forEach(function(btn) {
      var sub = document.getElementById('admin-menu-' + btn.dataset.adminMenu);
      var open = sub && Array.prototype.some.call(sub.querySelectorAll('a[href]'), function(a) {
        return path.indexOf(a.getAttribute('href').split('?')[0]) === 0;
      });
      if (open) {
        btn.classList.add('open', 'active');
        sub.classList.add('open');
      }
      btn.addEventListener('click', function() {
        btn.classList.toggle('open');
        if (sub) sub.classList.toggle('open');
      });
    });
  }

  // ==================== BREADCRUMB ====================
  var BREADCRUMB_MAP = {
    '/admin/dashboard': [{ label: 'Dashboard', url: '/admin/dashboard' }],
    '/admin/users': [{ label: 'Dashboard', url: '/admin/dashboard' }, { label: 'Employees' }],
    '/admin/users/add': [{ label: 'Dashboard', url: '/admin/dashboard' }, { label: 'Employees', url: '/admin/users' }, { label: 'Add Employee' }],
    '/admin/departments': [{ label: 'Dashboard', url: '/admin/dashboard' }, { label: 'Departments' }],
    '/admin/positions': [{ label: 'Dashboard', url: '/admin/dashboard' }, { label: 'Positions' }],
    '/admin/contracts': [{ label: 'Dashboard', url: '/admin/dashboard' }, { label: 'Contracts' }],
    '/admin/payroll': [{ label: 'Dashboard', url: '/admin/dashboard' }, { label: 'Payroll' }],
    '/admin/payments': [{ label: 'Dashboard', url: '/admin/dashboard' }, { label: 'Payments' }],
    '/admin/leaves': [{ label: 'Dashboard', url: '/admin/dashboard' }, { label: 'Leave Requests' }],
    '/admin/attendance': [{ label: 'Dashboard', url: '/admin/dashboard' }, { label: 'Attendance' }],
    '/admin/tasks': [{ label: 'Dashboard', url: '/admin/dashboard' }, { label: 'Tasks' }],
    '/admin/reviews': [{ label: 'Dashboard', url: '/admin/dashboard' }, { label: 'KPI Reviews' }],
    '/admin/reports': [{ label: 'Dashboard', url: '/admin/dashboard' }, { label: 'Reports' }],
    '/admin/kpi': [{ label: 'Dashboard', url: '/admin/dashboard' }, { label: 'KPI Goals' }],
    '/admin/expenses': [{ label: 'Dashboard', url: '/admin/dashboard' }, { label: 'Expenses' }],
    '/admin/skills': [{ label: 'Dashboard', url: '/admin/dashboard' }, { label: 'Skills' }],
    '/admin/documents': [{ label: 'Dashboard', url: '/admin/dashboard' }, { label: 'Documents' }],
    '/admin/shifts': [{ label: 'Dashboard', url: '/admin/dashboard' }, { label: 'Work Shifts' }],
    '/admin/videos': [{ label: 'Dashboard', url: '/admin/dashboard' }, { label: 'Training Videos' }],
    '/admin/announcements': [{ label: 'Dashboard', url: '/admin/dashboard' }, { label: 'Announcements' }],
    '/admin/cache': [{ label: 'Dashboard', url: '/admin/dashboard' }, { label: 'System', url: null }, { label: 'Cache & Email' }],
    '/admin/cloud': [{ label: 'Dashboard', url: '/admin/dashboard' }, { label: 'System', url: null }, { label: 'Cloud Storage' }],
    '/admin/audit-log': [{ label: 'Dashboard', url: '/admin/dashboard' }, { label: 'System', url: null }, { label: 'Audit Log' }],
    '/manager/dashboard': [{ label: 'Manager Dashboard' }],
    '/manager/team': [{ label: 'Manager Dashboard', url: '/manager/dashboard' }, { label: 'My Team' }],
    '/manager/overtime': [{ label: 'Manager Dashboard', url: '/manager/dashboard' }, { label: 'Overtime Approval' }],
    '/user1/dashboard': [{ label: 'Dashboard' }],
    '/user1/profile': [{ label: 'Dashboard', url: '/user1/dashboard' }, { label: 'My Profile' }],
    '/user1/payroll': [{ label: 'Dashboard', url: '/user1/dashboard' }, { label: 'Payslip' }],
    '/user1/kpi': [{ label: 'Dashboard', url: '/user1/dashboard' }, { label: 'KPI Goals' }],
    '/user1/expenses': [{ label: 'Dashboard', url: '/user1/dashboard' }, { label: 'Expenses' }],
    '/user1/skills': [{ label: 'Dashboard', url: '/user1/dashboard' }, { label: 'Skills' }],
    '/user1/documents': [{ label: 'Dashboard', url: '/user1/dashboard' }, { label: 'Documents' }],
    '/user1/overtime': [{ label: 'Dashboard', url: '/user1/dashboard' }, { label: 'Overtime' }],
    '/user1/my-shifts': [{ label: 'Dashboard', url: '/user1/dashboard' }, { label: 'My Shifts' }],
    '/hiring': [{ label: 'Hiring Dashboard' }],
    '/hiring/postings': [{ label: 'Hiring Dashboard', url: '/hiring' }, { label: 'Job Postings' }],
    '/hiring/candidates': [{ label: 'Hiring Dashboard', url: '/hiring' }, { label: 'Candidates' }],
  };

  function injectBreadcrumb() {
    var path = window.location.pathname;
    // Match exact or prefix
    var crumbs = BREADCRUMB_MAP[path];
    if (!crumbs) {
      // Try prefix match
      var keys = Object.keys(BREADCRUMB_MAP).sort(function(a,b){ return b.length - a.length; });
      for (var i = 0; i < keys.length; i++) {
        if (path.startsWith(keys[i]) && keys[i] !== '/') {
          crumbs = BREADCRUMB_MAP[keys[i]];
          break;
        }
      }
    }
    if (!crumbs || crumbs.length <= 1) return;

    var nav = document.createElement('nav');
    nav.setAttribute('aria-label', 'breadcrumb');
    nav.style.cssText = 'padding:8px 0 0;margin-bottom:-8px;';
    nav.innerHTML = '<ol style="display:flex;align-items:center;gap:6px;list-style:none;margin:0;padding:0;font-size:0.78rem;">' +
      crumbs.map(function(c, i) {
        var isLast = i === crumbs.length - 1;
        return '<li style="display:flex;align-items:center;gap:6px;">' +
          (i > 0 ? '<span style="color:#475569;">›</span>' : '') +
          (isLast || !c.url
            ? '<span style="color:' + (isLast ? '#94a3b8' : '#64748b') + ';font-weight:' + (isLast ? '600' : '400') + ';">' + c.label + '</span>'
            : '<a href="' + c.url + '" style="color:#6366f1;text-decoration:none;font-weight:500;" onmouseover="this.style.textDecoration=\'underline\'" onmouseout="this.style.textDecoration=\'none\'">' + c.label + '</a>') +
          '</li>';
      }).join('') +
      '</ol>';

    // Insert after page header h1
    var h1 = document.querySelector('main h1, .main h1, .main-content h1, .page-header h1');
    if (h1 && h1.parentNode) {
      h1.parentNode.insertBefore(nav, h1);
    }
  }

  // ==================== TOAST ====================
  function showToast(message, type, duration) {
    type = type || 'info';
    duration = duration || 3000;
    var colors = { success: '#10b981', error: '#ef4444', warning: '#f59e0b', info: '#6366f1' };
    var icons = { success: '✅', error: '❌', warning: '⚠️', info: 'ℹ️' };

    var toast = document.createElement('div');
    toast.style.cssText = [
      'position:fixed', 'bottom:' + (80 + document.querySelectorAll('.hrms-toast').length * 60) + 'px',
      'left:50%', 'transform:translateX(-50%)', 'z-index:99999',
      'background:#1e293b', 'border:1px solid ' + colors[type],
      'border-left:4px solid ' + colors[type],
      'color:white', 'padding:12px 20px', 'border-radius:10px',
      'box-shadow:0 8px 24px rgba(0,0,0,0.4)',
      'display:flex', 'align-items:center', 'gap:10px',
      'font-size:0.88rem', 'font-weight:500',
      'animation:hrmsToastIn 0.3s ease',
      'max-width:400px', 'min-width:200px',
    ].join(';');
    toast.className = 'hrms-toast';
    toast.innerHTML = '<span>' + icons[type] + '</span><span>' + message + '</span>';

    if (!document.getElementById('hrms-toast-style')) {
      var s = document.createElement('style');
      s.id = 'hrms-toast-style';
      s.textContent = '@keyframes hrmsToastIn{from{opacity:0;transform:translateX(-50%) translateY(20px)}to{opacity:1;transform:translateX(-50%) translateY(0)}}';
      document.head.appendChild(s);
    }

    document.body.appendChild(toast);
    setTimeout(function() {
      toast.style.opacity = '0';
      toast.style.transition = 'opacity 0.3s';
      setTimeout(function() { toast.remove(); }, 300);
    }, duration);
  }

  // ==================== BACK TO TOP ====================
  function injectBackToTop() {
    if (document.getElementById('hrms-back-top')) return;
    var btn = document.createElement('button');
    btn.id = 'hrms-back-top';
    btn.innerHTML = '↑';
    btn.title = 'Back to top';
    btn.style.cssText = 'position:fixed;bottom:252px;right:30px;z-index:8998;width:36px;height:36px;border-radius:50%;background:rgba(99,102,241,0.3);border:1px solid rgba(99,102,241,0.5);color:#6366f1;font-size:1rem;cursor:pointer;display:none;align-items:center;justify-content:center;transition:all 0.2s;font-weight:700;';
    btn.addEventListener('click', function() { window.scrollTo({ top: 0, behavior: 'smooth' }); });
    btn.addEventListener('mouseenter', function() { this.style.background = '#6366f1'; this.style.color = 'white'; });
    btn.addEventListener('mouseleave', function() { this.style.background = 'rgba(99,102,241,0.3)'; this.style.color = '#6366f1'; });
    document.body.appendChild(btn);

    window.addEventListener('scroll', function() {
      btn.style.display = window.scrollY > 300 ? 'flex' : 'none';
    });
  }

  function injectChatbotWidget() {
    var path = window.location.pathname || '';
    var isRolePage = path.indexOf('/admin') === 0 ||
      path.indexOf('/manager') === 0 ||
      path.indexOf('/hiring') === 0 ||
      path.indexOf('/user1') === 0 ||
      path.indexOf('/user/') === 0;

    if (!isRolePage || path.indexOf('/user1/chatbot') === 0) return;

    if (!document.getElementById('hrms-chatbot-css')) {
      var link = document.createElement('link');
      link.id = 'hrms-chatbot-css';
      link.rel = 'stylesheet';
      link.href = '/css/chatbot-widget.css';
      document.head.appendChild(link);
    }

    if (!document.getElementById('hrms-chatbot-js')) {
      var script = document.createElement('script');
      script.id = 'hrms-chatbot-js';
      script.src = '/js/chatbot-widget.js';
      script.defer = true;
      document.body.appendChild(script);
    }
  }

  function injectReactIslandLoader() {
    if (document.getElementById('hrms-react-islands-js')) return;
    var hasReactRoot = document.querySelector('[data-hrms-react]');
    if (!hasReactRoot) return;

    var script = document.createElement('script');
    script.id = 'hrms-react-islands-js';
    script.src = '/js/hrms-react-islands.js';
    script.defer = true;
    document.body.appendChild(script);
  }

  function injectSettingsWidget() {
    var path = window.location.pathname || '';
    var isRolePage = path.indexOf('/admin') === 0 ||
      path.indexOf('/manager') === 0 ||
      path.indexOf('/hiring') === 0 ||
      path.indexOf('/user1') === 0 ||
      path.indexOf('/user/') === 0;

    if (!isRolePage) return;

    if (!document.getElementById('hrms-bootstrap-icons')) {
      var icons = document.createElement('link');
      icons.id = 'hrms-bootstrap-icons';
      icons.rel = 'stylesheet';
      icons.href = 'https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css';
      document.head.appendChild(icons);
    }

    if (!document.getElementById('hrms-settings-js') && !(window.HRMS && window.HRMS.settings)) {
      var script = document.createElement('script');
      script.id = 'hrms-settings-js';
      script.src = '/js/hrms-settings.js';
      script.defer = true;
      document.body.appendChild(script);
    }
  }

  // ==================== KEYBOARD SHORTCUTS ====================
  function showShortcutsHelp() {
    if (document.getElementById('hrms-shortcuts-modal')) return;
    var modal = document.createElement('div');
    modal.id = 'hrms-shortcuts-modal';
    modal.innerHTML = [
      '<div style="position:fixed;inset:0;z-index:10000;background:rgba(0,0,0,0.6);" onclick="this.parentElement.remove()"></div>',
      '<div style="position:fixed;top:50%;left:50%;transform:translate(-50%,-50%);z-index:10001;width:min(480px,90vw);background:#1e293b;border-radius:16px;box-shadow:0 24px 64px rgba(0,0,0,0.6);border:1px solid rgba(255,255,255,0.1);overflow:hidden;">',
        '<div style="background:linear-gradient(135deg,#6366f1,#8b5cf6);padding:16px 20px;display:flex;justify-content:space-between;align-items:center;">',
          '<span style="color:white;font-weight:700;font-size:1rem;">⌨️ Keyboard Shortcuts</span>',
          '<button onclick="document.getElementById(\'hrms-shortcuts-modal\').remove()" style="background:rgba(255,255,255,0.2);border:none;color:white;width:28px;height:28px;border-radius:50%;cursor:pointer;">✕</button>',
        '</div>',
        '<div style="padding:20px;display:grid;grid-template-columns:1fr 1fr;gap:10px;">',
          [
            ['Ctrl+K', 'Global Search'],
            ['⚙️ Button', 'Settings Panel'],
            ['🔔 Button', 'Notifications'],
            ['↑ Button', 'Back to Top'],
            ['Ctrl+Shift+D', 'Go to Dashboard'],
            ['Ctrl+Shift+U', 'Go to Users'],
            ['Ctrl+Shift+L', 'Go to Leaves'],
            ['Ctrl+Shift+P', 'Go to Payroll'],
            ['?', 'Show this help'],
            ['ESC', 'Close modals'],
          ].map(function(s) {
            return '<div style="display:flex;align-items:center;gap:10px;padding:8px 12px;background:rgba(255,255,255,0.04);border-radius:8px;">' +
              '<kbd style="background:rgba(255,255,255,0.1);border:1px solid rgba(255,255,255,0.15);color:#94a3b8;padding:3px 8px;border-radius:6px;font-size:0.72rem;white-space:nowrap;">' + s[0] + '</kbd>' +
              '<span style="color:#e2e8f0;font-size:0.82rem;">' + s[1] + '</span>' +
              '</div>';
          }).join(''),
        '</div>',
      '</div>',
    ].join('');
    document.body.appendChild(modal);
  }

  // ==================== KEYBOARD SHORTCUTS HANDLER ====================
  document.addEventListener('keydown', function(e) {
    // ? = show shortcuts (when not in input)
    if (e.key === '?' && !['INPUT','TEXTAREA','SELECT'].includes(document.activeElement.tagName)) {
      showShortcutsHelp();
    }
    // Ctrl+Shift+D = Dashboard
    if (e.ctrlKey && e.shiftKey && e.key === 'D') {
      e.preventDefault();
      var dash = window.location.pathname.startsWith('/admin') ? '/admin/dashboard' :
                 window.location.pathname.startsWith('/manager') ? '/manager/dashboard' :
                 '/user1/dashboard';
      window.location.href = dash;
    }
    // Ctrl+Shift+U = Users (admin only)
    if (e.ctrlKey && e.shiftKey && e.key === 'U' && window.location.pathname.startsWith('/admin')) {
      e.preventDefault(); window.location.href = '/admin/users';
    }
    // Ctrl+Shift+L = Leaves
    if (e.ctrlKey && e.shiftKey && e.key === 'L') {
      e.preventDefault();
      window.location.href = window.location.pathname.startsWith('/admin') ? '/admin/leaves' : '/user/leaves';
    }
    // Ctrl+Shift+P = Payroll
    if (e.ctrlKey && e.shiftKey && e.key === 'P' && window.location.pathname.startsWith('/admin')) {
      e.preventDefault(); window.location.href = '/admin/payroll';
    }
  });

  // ==================== INIT ====================
  function init() {
    injectAdminSidebarSync();
    translateAdminSidebar();
    bindAdminLangButtons();
    injectBreadcrumb();
    injectBackToTop();
    injectSettingsWidget();
    injectChatbotWidget();
    injectReactIslandLoader();

    // Intercept flash messages and show as toasts
    document.querySelectorAll('.alert-success, .alert-danger, .alert-warning').forEach(function(el) {
      var type = el.classList.contains('alert-success') ? 'success' :
                 el.classList.contains('alert-danger') ? 'error' : 'warning';
      var msg = el.textContent.trim();
      if (msg) showToast(msg, type, 4000);
    });
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }

  // Expose
  window.HRMS = window.HRMS || {};
  window.HRMS.toast = showToast;
  window.HRMS.shortcuts = showShortcutsHelp;
})();
