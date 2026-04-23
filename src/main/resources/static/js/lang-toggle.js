/**
 * HRMS Language Toggle v5 — Simple & Reliable
 * Dùng innerHTML string replacement thay vì TreeWalker
 */
(function () {
  'use strict';

  // ==================== DICTIONARY ====================
  // Mảng [tiếng Việt, tiếng Anh] — thứ tự quan trọng (dài trước)
  var DICT = [
    // === PAGE TITLES & HEADINGS ===
    ['Bảng Điều Khiển', 'Dashboard'],
    ['Quản Lý Nhân Viên', 'Employee Management'],
    ['Quản Lý Phòng Ban', 'Department Management'],
    ['Quản Lý Chức Vụ', 'Position Management'],
    ['Quản Lý Hợp Đồng', 'Contract Management'],
    ['Quản Lý Bảng Lương', 'Payroll Management'],
    ['Quản Lý Thanh Toán', 'Payment Management'],
    ['Quản Lý Nghỉ Phép', 'Leave Management'],
    ['Quản Lý Chấm Công', 'Attendance Management'],
    ['Quản Lý Công Việc', 'Task Management'],
    ['Quản Lý KPI', 'KPI Management'],
    ['Quản Lý Chi Phí', 'Expense Management'],
    ['Quản Lý Kỹ Năng', 'Skill Management'],
    ['Quản Lý Tài Liệu', 'Document Management'],
    ['Quản Lý Ca Làm Việc', 'Shift Management'],
    ['Quản Lý Video', 'Video Management'],
    ['Thư Viện Video Đào Tạo', 'Training Video Library'],
    ['Phân Ca Nhân Viên', 'Employee Shift Assignment'],
    ['Duyệt Đơn Làm Thêm Giờ', 'Overtime Approval'],
    ['Nhóm Của Tôi', 'My Team'],
    ['Tổng Quan', 'Overview'],

    // === SIDEBAR NAVIGATION ===
    ['Nhân viên', 'Employees'],
    ['Phòng ban', 'Departments'],
    ['Chức vụ', 'Positions'],
    ['Hợp đồng', 'Contracts'],
    ['Bảng lương', 'Payroll'],
    ['Thanh toán', 'Payments'],
    ['Tài liệu', 'Documents'],
    ['Nghỉ phép', 'Leave Requests'],
    ['Chấm công', 'Attendance'],
    ['Công việc', 'Tasks'],
    ['Phân công', 'Assignments'],
    ['Đánh giá KPI', 'KPI Reviews'],
    ['Báo cáo', 'Reports'],
    ['Thông báo công ty', 'Announcements'],
    ['Tuyển dụng', 'Recruitment'],
    ['Video đào tạo', 'Training Videos'],
    ['Thêm video mới', 'Upload Video'],
    ['Cache &amp; Email', 'Cache &amp; Email'],
    ['Cloud Storage', 'Cloud Storage'],
    ['Nhật ký hệ thống', 'Audit Log'],
    ['KPI Goals', 'KPI Goals'],
    ['Chi phí', 'Expenses'],
    ['Kỹ năng', 'Skills'],
    ['Ca làm việc', 'Work Shifts'],
    ['Phân ca', 'Shift Assignments'],
    ['Đăng xuất', 'Logout'],
    ['Hồ sơ cá nhân', 'My Profile'],
    ['Làm thêm giờ', 'Overtime'],
    ['Phiếu lương', 'Payslip'],
    ['Công việc của tôi', 'My Tasks'],
    ['Thông báo', 'Notifications'],
    ['Duyệt nghỉ phép', 'Approve Leaves'],
    ['Duyệt OT', 'Approve Overtime'],
    ['Tin tuyển dụng', 'Job Postings'],
    ['Ứng viên', 'Candidates'],

    // === SECTION LABELS ===
    ['Cá nhân', 'Personal'],
    ['Hỗ trợ', 'Support'],
    ['Đào tạo', 'Training'],
    ['Quản lý', 'Management'],
    ['Hệ thống', 'System'],

    // === STAT CARDS ===
    ['NHÂN VIÊN', 'EMPLOYEES'],
    ['PHÒNG BAN', 'DEPARTMENTS'],
    ['CHỨC VỤ', 'POSITIONS'],
    ['CHỜ DUYỆT NGHỈ', 'PENDING LEAVES'],
    ['CÔNG VIỆC', 'TASKS'],
    ['KPI REVIEWS', 'KPI REVIEWS'],
    ['Tổng quan đi làm theo ngày', 'Daily attendance overview'],
    ['Phân bổ nhân lực', 'Workforce distribution'],
    ['Nhân Viên Theo Phòng Ban', 'Employees by Department'],
    ['Chấm Công 7 Ngày Qua', 'Attendance Last 7 Days'],
    ['Hôm nay:', 'Today:'],
    ['Xem chi tiết', 'View Details'],

    // === TABLE HEADERS ===
    ['Họ và tên', 'Full Name'],
    ['Họ tên', 'Full Name'],
    ['Email', 'Email'],
    ['Số điện thoại', 'Phone'],
    ['Phòng ban', 'Department'],
    ['Chức vụ', 'Position'],
    ['Vai trò', 'Role'],
    ['Ngày tạo', 'Created Date'],
    ['Ngày vào làm', 'Hire Date'],
    ['Mã nhân viên', 'Employee Code'],
    ['Loại', 'Type'],
    ['Số tiền', 'Amount'],
    ['Ngày', 'Date'],
    ['Ghi chú', 'Note'],
    ['Mô tả', 'Description'],
    ['Tên', 'Name'],
    ['Danh mục', 'Category'],
    ['Từ ngày', 'From Date'],
    ['Đến ngày', 'To Date'],
    ['Số ngày', 'Days'],
    ['Lý do', 'Reason'],
    ['Người duyệt', 'Approver'],
    ['Giờ vào', 'Check In'],
    ['Giờ ra', 'Check Out'],
    ['Tháng', 'Month'],
    ['Năm', 'Year'],
    ['Lương cơ bản', 'Base Salary'],
    ['Thưởng', 'Bonus'],
    ['Khấu trừ', 'Deductions'],
    ['Lương thực nhận', 'Net Salary'],
    ['Tiêu đề', 'Title'],
    ['Nội dung', 'Content'],
    ['Ưu tiên', 'Priority'],
    ['Hạn chót', 'Deadline'],
    ['Tiến độ', 'Progress'],
    ['Mức độ', 'Level'],
    ['Kinh nghiệm', 'Experience'],
    ['Chứng chỉ', 'Certificate'],
    ['Loại tài liệu', 'Document Type'],
    ['Kích thước', 'Size'],
    ['Ngày hết hạn', 'Expiry Date'],
    ['Xác minh', 'Verified'],
    ['Phương thức', 'Method'],
    ['Mã giao dịch', 'Transaction ID'],
    ['Ngày thanh toán', 'Payment Date'],
    ['Thao tác', 'Action'],
    ['Trạng thái', 'Status'],
    ['Tất cả', 'All'],

    // === BUTTONS ===
    ['Thêm nhân viên', 'Add Employee'],
    ['Tạo ngân sách', 'Create Budget'],
    ['Tạo đơn nghỉ', 'Create Leave'],
    ['Tạo thanh toán', 'Create Payment'],
    ['Tạo hợp đồng', 'Create Contract'],
    ['Tạo công việc', 'Create Task'],
    ['Tạo thông báo', 'Create Announcement'],
    ['Lưu thay đổi', 'Save Changes'],
    ['Lưu', 'Save'],
    ['Hủy', 'Cancel'],
    ['Xóa', 'Delete'],
    ['Sửa', 'Edit'],
    ['Thêm', 'Add'],
    ['Tìm kiếm', 'Search'],
    ['Quay lại', 'Back'],
    ['Xác nhận', 'Confirm'],
    ['Lọc', 'Filter'],
    ['Reset', 'Reset'],
    ['Xuất', 'Export'],
    ['Nhập', 'Import'],
    ['Cập nhật', 'Update'],
    ['Chi tiết', 'Details'],
    ['Xem', 'View'],
    ['Duyệt', 'Approve'],
    ['Từ chối', 'Reject'],
    ['Đóng', 'Close'],
    ['Gửi', 'Submit'],
    ['Tải lên', 'Upload'],
    ['Tải xuống', 'Download'],
    ['Tìm', 'Search'],

    // === STATUS ===
    ['Chờ duyệt', 'Pending'],
    ['Đã duyệt', 'Approved'],
    ['Đang thực hiện', 'In Progress'],
    ['Hoàn thành', 'Completed'],
    ['Đã hủy', 'Cancelled'],
    ['Hoạt động', 'Active'],
    ['Không hoạt động', 'Inactive'],
    ['Đã thanh toán', 'Paid'],
    ['Chưa thanh toán', 'Unpaid'],
    ['Có mặt', 'Present'],
    ['Đi muộn', 'Late'],
    ['Vắng mặt', 'Absent'],
    ['Về sớm', 'Early Leave'],

    // === FORM ===
    ['Tên đăng nhập', 'Username'],
    ['Mật khẩu', 'Password'],
    ['Giới tính', 'Gender'],
    ['Nam', 'Male'],
    ['Nữ', 'Female'],
    ['Ngày sinh', 'Date of Birth'],
    ['Địa chỉ', 'Address'],
    ['Ảnh đại diện', 'Avatar'],
    ['Đăng nhập', 'Login'],

    // === MESSAGES ===
    ['Không có dữ liệu', 'No data available'],
    ['Đang tải...', 'Loading...'],
    ['Thành công', 'Success'],
    ['Thất bại', 'Failed'],
    ['Lỗi', 'Error'],
    ['Cảnh báo', 'Warning'],
    ['Không có', 'None'],
    ['Chưa có', 'None yet'],
  ];

  // ==================== CORE ====================

  function getCookie(name) {
    var m = document.cookie.match(new RegExp('(^| )' + name + '=([^;]+)'));
    return m ? m[2] : null;
  }

  function getLang() {
    var urlParams = new URLSearchParams(window.location.search);
    return urlParams.get('lang') || getCookie('HRMS_LANG') || 'vi';
  }

  /**
   * Translate a single text node using the dictionary
   */
  function translateNode(node) {
    var text = node.nodeValue;
    if (!text || !text.trim()) return;

    for (var i = 0; i < DICT.length; i++) {
      var vi = DICT[i][0];
      var en = DICT[i][1];
      if (text.indexOf(vi) !== -1) {
        text = text.split(vi).join(en);
      }
    }
    if (text !== node.nodeValue) {
      node.nodeValue = text;
    }
  }

  /**
   * Walk all text nodes in an element
   */
  function translateElement(el) {
    if (!el) return;
    var nodes = [];
    var walker = document.createTreeWalker(el, NodeFilter.SHOW_TEXT, null, false);
    var node;
    while ((node = walker.nextNode())) {
      var parent = node.parentNode;
      if (parent) {
        var tag = parent.nodeName.toLowerCase();
        if (tag !== 'script' && tag !== 'style') {
          nodes.push(node);
        }
      }
    }
    nodes.forEach(translateNode);
  }

  /**
   * Translate placeholder attributes
   */
  function translatePlaceholders() {
    document.querySelectorAll('[placeholder]').forEach(function(el) {
      var ph = el.getAttribute('placeholder');
      for (var i = 0; i < DICT.length; i++) {
        if (ph.indexOf(DICT[i][0]) !== -1) {
          ph = ph.split(DICT[i][0]).join(DICT[i][1]);
        }
      }
      el.setAttribute('placeholder', ph);
    });
  }

  /**
   * Translate title attribute (tooltips)
   */
  function translateTitles() {
    document.querySelectorAll('[title]').forEach(function(el) {
      var t = el.getAttribute('title');
      for (var i = 0; i < DICT.length; i++) {
        if (t.indexOf(DICT[i][0]) !== -1) {
          t = t.split(DICT[i][0]).join(DICT[i][1]);
        }
      }
      el.setAttribute('title', t);
    });
  }

  function updateActiveButtons(lang) {
    document.querySelectorAll('.lang-btn').forEach(function(btn) {
      var btnLang = btn.getAttribute('data-lang');
      if (btnLang === lang) {
        btn.style.background = '#6366f1';
        btn.style.borderColor = '#6366f1';
        btn.style.color = 'white';
        btn.style.fontWeight = '700';
      } else {
        btn.style.background = 'rgba(255,255,255,0.08)';
        btn.style.border = '1px solid rgba(255,255,255,0.15)';
        btn.style.color = '#94a3b8';
        btn.style.fontWeight = '600';
      }
    });
  }

  function switchLang(lang) {
    var url = new URL(window.location.href);
    url.searchParams.set('lang', lang);
    window.location.href = url.toString();
  }

  // ==================== INIT ====================

  function init() {
    var lang = getLang();
    console.log('[HRMS Lang] Current lang:', lang);
    updateActiveButtons(lang);

    if (lang === 'en') {
      console.log('[HRMS Lang] Translating to English...');
      translateElement(document.body);
      translatePlaceholders();
      translateTitles();
      console.log('[HRMS Lang] Translation complete');
    }

    // Bind buttons
    document.querySelectorAll('.lang-btn').forEach(function(btn) {
      btn.addEventListener('click', function(e) {
        e.preventDefault();
        e.stopPropagation();
        var targetLang = this.getAttribute('data-lang');
        console.log('[HRMS Lang] Switching to:', targetLang);
        if (targetLang) switchLang(targetLang);
      });
    });
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }

  window.HRMS = window.HRMS || {};
  window.HRMS.setLang = switchLang;
  window.HRMS.getLang = getLang;
})();
