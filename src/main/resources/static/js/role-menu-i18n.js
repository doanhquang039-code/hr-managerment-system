(function () {
  'use strict';

  var vi = {
    dashboard: 'Dashboard', mainDashboard: 'Dashboard chính', advancedDashboard: 'Dashboard nâng cao',
    teamAnalytics: 'Team Analytics', minigame: 'Minigame', playTop: 'Chơi & xem top',
    createMinigame: 'Tạo minigame có quà', aiInsights: 'AI Insights', teamInsight: 'Insight team',
    performanceAnalytics: 'Phân tích hiệu suất', summaryReport: 'Báo cáo tổng hợp',
    myGroups: 'Nhóm của tôi', allGroups: 'Tất cả nhóm', myTeam: 'Team của tôi', members: 'Thành viên',
    teamAdmin: 'Quản trị đội nhóm', overview: 'Tổng quan', teamGoals: 'Mục tiêu team',
    allGoals: 'Tất cả mục tiêu', createGoal: 'Tạo mục tiêu', inProgress: 'Đang triển khai',
    completed: 'Hoàn thành', overdue: 'Quá hạn', meetings: 'Cuộc họp', allMeetings: 'Tất cả cuộc họp',
    scheduleMeeting: 'Lên lịch họp', teamMeeting: 'Họp team', budget: 'Ngân sách',
    budgetOverview: 'Tổng quan ngân sách', createBudget: 'Tạo ngân sách', budgetAnalytics: 'Phân tích ngân sách',
    budgetReport: 'Báo cáo ngân sách', sales: 'Bán hàng', products: 'Sản phẩm', customers: 'Khách hàng',
    orders: 'Đơn hàng', teamOps: 'Vận hành team', teamPeople: 'Nhân sự team', teamList: 'Danh sách team',
    teamMembers: 'Thành viên team', performance: 'Hiệu suất', teamContracts: 'Hợp đồng team',
    attendance: 'Chấm công', attendanceTrack: 'Theo dõi chấm công', overtime: 'Tăng ca',
    overtimePending: 'Tăng ca chờ duyệt', overtimeApproved: 'Tăng ca đã duyệt', overtimeRejected: 'Tăng ca bị từ chối',
    leaves: 'Nghỉ phép', allLeaves: 'Tất cả đơn nghỉ', pendingApproval: 'Chờ duyệt',
    approved: 'Đã duyệt', rejected: 'Bị từ chối', reports: 'Báo cáo', teamReports: 'Báo cáo team',
    settings: 'Cài đặt', profile: 'Profile', dashboardSettings: 'Cài đặt dashboard', logout: 'Đăng xuất',
    hiringDashboard: 'Hiring Dashboard', jobManagement: 'Quản lý tuyển dụng', jobPostings: 'Tin tuyển dụng',
    allJobs: 'Tất cả tin tuyển', addJob: 'Đăng tin mới', fullTime: 'Chính thức', partTime: 'Part-time',
    intern: 'Intern', junior: 'Junior', security: 'Bảo vệ', createJob: 'Tạo tin tuyển',
    closingSoon: 'Sắp hết hạn', candidates: 'Ứng viên', allCandidates: 'Tất cả ứng viên',
    newApplications: 'Hồ sơ mới', screening: 'Đang sàng lọc', interviewStage: 'Vòng phỏng vấn',
    offerStage: 'Đề nghị tuyển', interviews: 'Phỏng vấn', allInterviews: 'Tất cả phỏng vấn',
    scheduleInterview: 'Lên lịch phỏng vấn', upcoming: 'Sắp diễn ra', scheduled: 'Đã lên lịch',
    analytics: 'Phân tích', pipelineAnalytics: 'Phân tích pipeline', hiringPerformance: 'Hiệu suất tuyển dụng',
    hiringSettings: 'Cài đặt Hiring', personalInfo: 'Thông tin cá nhân', updateInfo: 'Cập nhật thông tin',
    updatePassword: 'Cập nhật mật khẩu', personalProfile: 'Hồ sơ cá nhân', timekeeping: 'Điểm danh',
    weekAttendance: 'Điểm danh trong tuần', shifts: 'Ca làm việc', schedule: 'Lịch làm việc',
    utilities: 'Tiện ích', notifications: 'Thông báo', personalNotifications: 'Thông báo cá nhân',
    adminAnnouncements: 'Thông báo từ Admin', weeklyBoard: 'Bảng tin trong tuần', hrAssistant: 'Trợ lý HR',
    sharing: 'Chia sẻ thông tin', exchange: 'Trao đổi', talkManager: 'Trao đổi với Manager',
    talkHiring: 'Trao đổi với Hiring', internalSocial: 'Mạng xã hội nội bộ', recognition: 'Vinh danh',
    work: 'Công việc', myTasks: 'Công việc của tôi', okr: 'OKR', reviews: 'Đánh giá',
    skills: 'Kỹ năng', learning: 'Học tập & Phát triển', training: 'Đào tạo', myCourses: 'Khóa học của tôi',
    courseCatalog: 'Danh mục khóa học', trainingVideos: 'Video đào tạo', financeAssets: 'Tài chính & Tài sản',
    finance: 'Tài chính', payslip: 'Phiếu lương', expenses: 'Chi phí', reimbursement: 'Hoàn tiền',
    documents: 'Tài liệu', assets: 'Tài sản', engagement: 'Tương tác', referrals: 'Giới thiệu ứng viên',
    surveys: 'Khảo sát', myChecklist: 'Checklist của tôi', quickSupport: 'Hỗ trợ nhanh'
  };

  var en = {
    dashboard: 'Dashboard', mainDashboard: 'Main Dashboard', advancedDashboard: 'Advanced Dashboard',
    teamAnalytics: 'Team Analytics', minigame: 'Minigame', playTop: 'Play & leaderboard',
    createMinigame: 'Create rewarded minigame', aiInsights: 'AI Insights', teamInsight: 'Team insights',
    performanceAnalytics: 'Performance analytics', summaryReport: 'Summary report',
    myGroups: 'My Groups', allGroups: 'All groups', myTeam: 'My team', members: 'Members',
    teamAdmin: 'Team Management', overview: 'Overview', teamGoals: 'Team goals',
    allGoals: 'All goals', createGoal: 'Create goal', inProgress: 'In progress',
    completed: 'Completed', overdue: 'Overdue', meetings: 'Meetings', allMeetings: 'All meetings',
    scheduleMeeting: 'Schedule meeting', teamMeeting: 'Team meeting', budget: 'Budget',
    budgetOverview: 'Budget overview', createBudget: 'Create budget', budgetAnalytics: 'Budget analytics',
    budgetReport: 'Budget report', sales: 'Sales', products: 'Products', customers: 'Customers',
    orders: 'Orders', teamOps: 'Team Operations', teamPeople: 'Team people', teamList: 'Team list',
    teamMembers: 'Team members', performance: 'Performance', teamContracts: 'Team contracts',
    attendance: 'Attendance', attendanceTrack: 'Attendance tracking', overtime: 'Overtime',
    overtimePending: 'Pending overtime', overtimeApproved: 'Approved overtime', overtimeRejected: 'Rejected overtime',
    leaves: 'Leave', allLeaves: 'All leave requests', pendingApproval: 'Pending approval',
    approved: 'Approved', rejected: 'Rejected', reports: 'Reports', teamReports: 'Team reports',
    settings: 'Settings', profile: 'Profile', dashboardSettings: 'Dashboard settings', logout: 'Logout',
    hiringDashboard: 'Hiring Dashboard', jobManagement: 'Job Management', jobPostings: 'Job Postings',
    allJobs: 'All job posts', addJob: 'Create job post', fullTime: 'Full-time', partTime: 'Part-time',
    intern: 'Intern', junior: 'Junior', security: 'Security', createJob: 'Create Job',
    closingSoon: 'Closing Soon', candidates: 'Candidates', allCandidates: 'All Candidates',
    newApplications: 'New Applications', screening: 'In Screening', interviewStage: 'Interview Stage',
    offerStage: 'Offer Stage', interviews: 'Interviews', allInterviews: 'All Interviews',
    scheduleInterview: 'Schedule Interview', upcoming: 'Upcoming', scheduled: 'Scheduled',
    analytics: 'Analytics', pipelineAnalytics: 'Pipeline Analytics', hiringPerformance: 'Hiring Performance',
    hiringSettings: 'Hiring Settings', personalInfo: 'Personal Information', updateInfo: 'Update information',
    updatePassword: 'Update password', personalProfile: 'Personal profile', timekeeping: 'Timekeeping',
    weekAttendance: 'Weekly attendance', shifts: 'Work shifts', schedule: 'Work schedule',
    utilities: 'Utilities', notifications: 'Notifications', personalNotifications: 'Personal notifications',
    adminAnnouncements: 'Admin announcements', weeklyBoard: 'Weekly bulletin', hrAssistant: 'HR Assistant',
    sharing: 'Information Sharing', exchange: 'Exchange', talkManager: 'Talk with Manager',
    talkHiring: 'Talk with Hiring', internalSocial: 'Internal social network', recognition: 'Recognition',
    work: 'Work', myTasks: 'My tasks', okr: 'OKR', reviews: 'Reviews',
    skills: 'Skills', learning: 'Learning & Development', training: 'Training', myCourses: 'My courses',
    courseCatalog: 'Course catalog', trainingVideos: 'Training videos', financeAssets: 'Finance & Assets',
    finance: 'Finance', payslip: 'Payslip', expenses: 'Expenses', reimbursement: 'Reimbursement',
    documents: 'Documents', assets: 'Assets', engagement: 'Engagement', referrals: 'Candidate referrals',
    surveys: 'Surveys', myChecklist: 'My checklist', quickSupport: 'Quick support'
  };

  var zh = Object.assign({}, en, {
    dashboard: '仪表盘', mainDashboard: '主仪表盘', advancedDashboard: '高级仪表盘', teamAnalytics: '团队分析',
    minigame: '小游戏', playTop: '游玩与排行', createMinigame: '创建有奖小游戏', aiInsights: 'AI 洞察',
    myGroups: '我的群组', teamAdmin: '团队管理', overview: '总览', teamGoals: '团队目标',
    meetings: '会议', budget: '预算', sales: '销售', products: '产品', customers: '客户', orders: '订单',
    teamOps: '团队运营', teamPeople: '团队人员', attendance: '考勤', overtime: '加班',
    leaves: '请假', reports: '报告', settings: '设置', profile: '个人资料', dashboardSettings: '仪表盘设置',
    logout: '退出', hiringDashboard: '招聘仪表盘', jobManagement: '招聘管理', jobPostings: '招聘信息',
    candidates: '候选人', interviews: '面试', analytics: '分析', personalInfo: '个人信息',
    timekeeping: '打卡', utilities: '工具', notifications: '通知', sharing: '信息共享',
    exchange: '交流', work: '工作', learning: '学习与发展', training: '培训', financeAssets: '财务与资产',
    finance: '财务', engagement: '互动'
  });
  var ja = Object.assign({}, en, {
    dashboard: 'ダッシュボード', mainDashboard: 'メインダッシュボード', advancedDashboard: '高度なダッシュボード',
    teamAnalytics: 'チーム分析', minigame: 'ミニゲーム', playTop: 'プレイとランキング',
    createMinigame: '報酬付きミニゲーム作成', aiInsights: 'AIインサイト', myGroups: 'マイグループ',
    teamAdmin: 'チーム管理', overview: '概要', teamGoals: 'チーム目標', meetings: '会議',
    budget: '予算', sales: '販売', products: '商品', customers: '顧客', orders: '注文',
    teamOps: 'チーム運用', teamPeople: 'チームメンバー', attendance: '勤怠', overtime: '残業',
    leaves: '休暇', reports: 'レポート', settings: '設定', profile: 'プロフィール',
    dashboardSettings: 'ダッシュボード設定', logout: 'ログアウト', hiringDashboard: '採用ダッシュボード',
    jobManagement: '求人管理', jobPostings: '求人情報', candidates: '候補者', interviews: '面接',
    analytics: '分析', personalInfo: '個人情報', timekeeping: '出勤管理', utilities: 'ユーティリティ',
    notifications: '通知', sharing: '情報共有', exchange: '交流', work: '仕事',
    learning: '学習と成長', training: '研修', financeAssets: '財務と資産', finance: '財務', engagement: 'エンゲージメント'
  });
  var ko = Object.assign({}, en, {
    dashboard: '대시보드', mainDashboard: '메인 대시보드', advancedDashboard: '고급 대시보드',
    teamAnalytics: '팀 분석', minigame: '미니게임', playTop: '플레이 및 순위',
    createMinigame: '보상 미니게임 만들기', aiInsights: 'AI 인사이트', myGroups: '내 그룹',
    teamAdmin: '팀 관리', overview: '개요', teamGoals: '팀 목표', meetings: '회의',
    budget: '예산', sales: '판매', products: '제품', customers: '고객', orders: '주문',
    teamOps: '팀 운영', teamPeople: '팀 인원', attendance: '근태', overtime: '초과근무',
    leaves: '휴가', reports: '보고서', settings: '설정', profile: '프로필',
    dashboardSettings: '대시보드 설정', logout: '로그아웃', hiringDashboard: '채용 대시보드',
    jobManagement: '채용 관리', jobPostings: '채용 공고', candidates: '지원자', interviews: '면접',
    analytics: '분석', personalInfo: '개인 정보', timekeeping: '출퇴근', utilities: '유틸리티',
    notifications: '알림', sharing: '정보 공유', exchange: '소통', work: '업무',
    learning: '학습 및 개발', training: '교육', financeAssets: '재무 및 자산', finance: '재무', engagement: '참여'
  });

  var bundles = { vi: vi, en: en, zh: zh, ja: ja, ko: ko };

  var hrefKeys = {
    '/manager/dashboard': 'mainDashboard', '/manager/dashboard-advanced': 'advancedDashboard', '/manager/analytics': 'teamAnalytics',
    '/manager/minigame': 'playTop', '/manager/minigames/create': 'createMinigame', '/manager/ai-insights': 'teamInsight',
    '/manager/reports/team': 'teamReports', '/groups': 'allGroups', '/manager/team': 'myTeam', '/manager/team-members': 'members',
    '/manager/goals': 'allGoals', '/manager/goals/create': 'createGoal', '/manager/goals?status=IN_PROGRESS': 'inProgress',
    '/manager/goals?status=COMPLETED': 'completed', '/manager/goals?status=OVERDUE': 'overdue', '/manager/meetings': 'allMeetings',
    '/manager/meetings/create': 'scheduleMeeting', '/manager/meetings/type/TEAM': 'teamMeeting', '/manager/budget': 'budgetOverview',
    '/manager/budget/create': 'createBudget', '/manager/budget/analytics': 'budgetAnalytics', '/manager/reports/budget': 'budgetReport',
    '/sales': 'overview', '/sales/products': 'products', '/sales/customers': 'customers', '/sales/orders': 'orders',
    '/manager/performance': 'performance', '/manager/contracts': 'teamContracts', '/manager/attendance': 'attendanceTrack',
    '/manager/overtime': 'overtime', '/manager/overtime?status=PENDING': 'overtimePending', '/manager/overtime?status=APPROVED': 'overtimeApproved',
    '/manager/overtime?status=REJECTED': 'overtimeRejected', '/manager/leave-requests': 'allLeaves',
    '/manager/leave-requests?status=PENDING': 'pendingApproval', '/manager/leave-requests?status=APPROVED': 'approved',
    '/manager/leave-requests?status=REJECTED': 'rejected', '/profile': 'profile',
    '/hiring/dashboard': 'hiringDashboard', '/hiring/minigame': 'minigame', '/hiring/jobs': 'allJobs',
    '/hiring/postings/add': 'addJob', '/hiring/postings?category=FULL_TIME': 'fullTime', '/hiring/postings?category=PART_TIME': 'partTime',
    '/hiring/postings?category=INTERN': 'intern', '/hiring/postings?category=JUNIOR': 'junior', '/hiring/postings?category=SECURITY': 'security',
    '/hiring/jobs/create': 'createJob', '/hiring/jobs/closing-soon': 'closingSoon', '/hiring/candidates': 'allCandidates',
    '/hiring/candidates?stage=APPLIED': 'newApplications', '/hiring/candidates?stage=SCREENING': 'screening',
    '/hiring/candidates?stage=INTERVIEW': 'interviewStage', '/hiring/candidates?stage=OFFER': 'offerStage',
    '/hiring/interviews': 'allInterviews', '/hiring/interviews/create': 'scheduleInterview', '/hiring/interviews/upcoming': 'upcoming',
    '/hiring/interviews?status=SCHEDULED': 'scheduled', '/hiring/interviews?status=COMPLETED': 'completed',
    '/hiring/analytics/pipeline': 'pipelineAnalytics', '/hiring/analytics/performance': 'hiringPerformance',
    '/hiring/reports': 'reports', '/hiring/settings': 'hiringSettings',
    '/user1/dashboard': 'dashboard', '/user1/groups': 'myGroups', '/user1/minigame': 'minigame',
    '/user1/profile#update-info': 'updateInfo', '/user1/profile#change-password': 'updatePassword', '/user1/profile': 'personalProfile',
    '/user1/attendance': 'attendance', '/user1/my-shifts': 'shifts', '/user1/my-scans': 'QR Check-in',
    '/user1/notifications': 'personalNotifications', '/user1/announcements': 'adminAnnouncements', '/user1/chatbot': 'hrAssistant',
    '/user1/groups?target=manager': 'talkManager', '/user1/groups?target=hiring': 'talkHiring', '/user1/tasks': 'myTasks',
    '/user1/leaves': 'leaves', '/user1/overtime': 'overtime', '/user1/kpi': 'KPI Goals', '/user1/okr-list': 'okr',
    '/user1/reviews': 'reviews', '/user1/skills': 'skills', '/lms/my-courses': 'myCourses', '/lms/course-catalog': 'courseCatalog',
    '/videos': 'trainingVideos', '/user1/payroll': 'payslip', '/user1/expenses': 'expenses', '/user1/documents': 'documents',
    '/user1/my-assets': 'assets', '/user1/referrals': 'referrals', '/user1/surveys': 'surveys', '/user1/my-checklist': 'myChecklist'
  };

  var parentKeys = {
    'manager-dashboard-options': 'dashboard', 'manager-minigame-options': 'minigame', 'manager-ai-options': 'aiInsights',
    'manager-group-options': 'myGroups', 'manager-goals-options': 'teamGoals', 'manager-meeting-options': 'meetings',
    'manager-budget-options': 'budget', 'manager-sales-options': 'sales', 'manager-team-options': 'teamPeople',
    'manager-attendance-options': 'attendance', 'manager-leave-options': 'leaves', 'manager-report-options': 'teamReports',
    'hiring-job-options': 'jobPostings', 'profile-options': 'personalProfile', 'attendance-options': 'attendance',
    'utility-options': 'notifications', 'social-options': 'exchange', 'work-options': 'work',
    'learning-options': 'training', 'finance-options': 'finance', 'engagement-options': 'engagement'
  };

  var sectionKeys = {
    manager: ['overview', 'teamAdmin', 'teamOps', 'reports', 'settings'],
    hiring: ['jobManagement', 'candidates', 'interviews', 'analytics', 'settings'],
    user: ['overview', 'personalInfo', 'timekeeping', 'utilities', 'sharing', 'work', 'learning', 'financeAssets', 'engagement']
  };

  function lang() {
    var q = new URLSearchParams(location.search).get('lang');
    var m = document.cookie.match(/(?:^|;\s*)HRMS_LANG=([^;]+)/);
    var c = m ? decodeURIComponent(m[1]) : null;
    var l = q || c || localStorage.getItem('hrms_lang') || 'vi';
    return String(l).split('_')[0].toLowerCase();
  }

  function t(key) {
    if (!key) return '';
    var b = bundles[lang()] || bundles.vi;
    return b[key] || bundles.en[key] || bundles.vi[key] || key;
  }

  function setLabel(el, text) {
    if (!el || !text) return;
    var holder = el.querySelector(':scope > .left');
    if (holder) {
      Array.from(holder.childNodes).forEach(function (node) { if (node.nodeType === 3) node.remove(); });
      var span = holder.querySelector('span[data-role-label]');
      if (!span) {
        span = document.createElement('span');
        span.dataset.roleLabel = 'true';
        holder.appendChild(span);
      }
      span.textContent = text;
      return;
    }
    var directSpan = el.querySelector(':scope > span:not(.left)');
    if (directSpan) {
      directSpan.textContent = text;
      return;
    }
    Array.from(el.childNodes).forEach(function (node) { if (node.nodeType === 3) node.remove(); });
    el.appendChild(document.createTextNode(' ' + text));
  }

  function translateLinks(root) {
    root.querySelectorAll('a[href]').forEach(function (a) {
      var href = a.getAttribute('href') || '';
      var key = hrefKeys[href] || hrefKeys[decodeURIComponent(href)] || hrefKeys[href.split('?')[0].split('#')[0]];
      if (key) setLabel(a, t(key));
    });
  }

  function translateParents(root) {
    root.querySelectorAll('[data-manager-submenu-toggle], [data-submenu-toggle], [data-user-submenu-toggle]').forEach(function (el) {
      var id = el.dataset.managerSubmenuToggle || el.dataset.submenuToggle || el.dataset.userSubmenuToggle;
      setLabel(el, t(parentKeys[id]));
    });
  }

  function translateSections(root, role) {
    var selector = role === 'manager' ? '.manager-section' : role === 'user' ? '.sidebar-section-label' : '.sidebar-heading span';
    root.querySelectorAll(selector).forEach(function (el, index) {
      var key = sectionKeys[role] && sectionKeys[role][index];
      if (key) el.textContent = t(key);
    });
  }

  function bindLang(root) {
    root.querySelectorAll('.lang-btn[data-lang]').forEach(function (btn) {
      var active = btn.dataset.lang === lang();
      btn.classList.toggle('active', active);
      if (btn.dataset.roleLangBound === 'true') return;
      btn.dataset.roleLangBound = 'true';
      btn.addEventListener('click', function (e) {
        e.preventDefault();
        var selected = btn.dataset.lang;
        localStorage.setItem('hrms_lang', selected);
        var url = new URL(location.href);
        url.searchParams.set('lang', selected);
        location.href = url.toString();
      });
    });
  }

  function init() {
    var manager = document.querySelector('.manager-sidebar');
    var hiring = document.querySelector('.sidebar:not(.manager-sidebar)');
    var user = document.querySelector('.sidebar .sidebar-brand') ? document.querySelector('.sidebar') : null;

    if (manager) {
      translateSections(manager, 'manager');
      translateParents(manager);
      translateLinks(manager);
      bindLang(manager);
      var managerSettings = manager.querySelector('a[onclick*="settings"]');
      if (managerSettings) setLabel(managerSettings, t('dashboardSettings'));
      var managerLogout = manager.querySelector('.sidebar-logout button[type="submit"]');
      if (managerLogout) setLabel(managerLogout, t('logout'));
    }
    if (hiring && !user && location.pathname.indexOf('/hiring') === 0) {
      translateSections(hiring, 'hiring');
      translateParents(hiring);
      translateLinks(hiring);
      bindLang(hiring);
      var logout = hiring.querySelector('button[type="submit"]');
      if (logout) setLabel(logout, t('logout'));
    }
    if (user) {
      translateSections(user, 'user');
      translateParents(user);
      translateLinks(user);
      bindLang(user);
      var settings = user.querySelector('[data-user-settings-label="true"] span');
      if (settings) settings.textContent = t('dashboardSettings');
      var logoutBtn = user.querySelector('.sidebar-logout button[type="submit"]');
      if (logoutBtn) setLabel(logoutBtn, t('logout'));
    }
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();
