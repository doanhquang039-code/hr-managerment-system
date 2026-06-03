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
    html += '</div><div class="admin-lang"><button type="button" data-lang="vi">NV VI</button><span>|</span><button type="button" data-lang="en">GB EN</button></div>';
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
