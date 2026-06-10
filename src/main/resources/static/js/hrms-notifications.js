/**
 * HRMS Notification Center
 */
(function () {
  'use strict';

  var POLL_INTERVAL = 30000;
  var unreadCount = 0;

  function fetchNotifications() {
    fetch('/api/notifications/unread-count', {
      headers: { 'X-Requested-With': 'XMLHttpRequest' }
    })
      .then(function (r) { return r.ok ? r.json() : null; })
      .then(function (data) {
        if (data && data.count !== undefined) updateBadge(data.count);
      })
      .catch(function () {});
  }

  function fetchNotificationList() {
    return fetch('/notifications/api/list?limit=10', {
      headers: { 'X-Requested-With': 'XMLHttpRequest' }
    })
      .then(function (r) { return r.ok ? r.json() : []; })
      .catch(function () { return []; });
  }

  function updateBadge(count) {
    unreadCount = count;
    document.querySelectorAll('.hrms-notif-badge').forEach(function (badge) {
      badge.textContent = count > 99 ? '99+' : count;
      badge.style.display = count > 0 ? 'flex' : 'none';
    });
  }

  function createNotifButton() {
    if (document.getElementById('hrms-notif-btn')) return;

    var btn = document.createElement('button');
    btn.id = 'hrms-notif-btn';
    btn.title = 'Thông báo';
    btn.style.cssText = [
      'position:fixed',
      'bottom:180px',
      'right:24px',
      'z-index:8999',
      'width:48px',
      'height:48px',
      'border-radius:50%',
      'background:linear-gradient(135deg,#f59e0b,#f97316)',
      'border:none',
      'color:white',
      'font-size:1.1rem',
      'cursor:pointer',
      'box-shadow:0 12px 28px rgba(245,158,11,0.36)',
      'transition:all 0.2s',
      'display:flex',
      'align-items:center',
      'justify-content:center'
    ].join(';');
    btn.innerHTML = '<i class="bi bi-bell-fill" aria-hidden="true"></i>' +
      '<span class="hrms-notif-badge" style="position:absolute;top:-4px;right:-4px;background:#ef4444;color:white;border-radius:10px;font-size:0.65rem;font-weight:700;padding:2px 5px;min-width:18px;height:18px;display:none;align-items:center;justify-content:center;border:2px solid #1e293b;">0</span>';

    btn.addEventListener('mouseenter', function () { this.style.transform = 'scale(1.08)'; });
    btn.addEventListener('mouseleave', function () { this.style.transform = 'scale(1)'; });
    btn.addEventListener('click', toggleNotifPanel);

    document.body.appendChild(btn);
  }

  function toggleNotifPanel() {
    var existing = document.getElementById('hrms-notif-panel');
    if (existing) {
      existing.remove();
      return;
    }

    fetchNotificationList().then(function (notifications) {
      var panel = document.createElement('div');
      panel.id = 'hrms-notif-panel';
      panel.style.cssText = 'position:fixed;bottom:242px;right:24px;z-index:9500;width:340px;background:#1e293b;border-radius:16px;box-shadow:0 20px 60px rgba(0,0,0,0.5);border:1px solid rgba(255,255,255,0.1);overflow:hidden;';

      var notifHtml = notifications.length === 0
        ? '<div style="text-align:center;padding:32px;color:#94a3b8;"><div style="font-size:2rem;margin-bottom:8px;"><i class="bi bi-bell"></i></div><div>Chưa có thông báo</div></div>'
        : notifications.map(function (n) {
          var typeColors = { PAYROLL: '#10b981', SUCCESS: '#6366f1', DANGER: '#ef4444', WARNING: '#f59e0b', LEAVE_REQUEST: '#3b82f6', INFO: '#94a3b8' };
          var color = typeColors[n.type] || '#94a3b8';
          return '<div style="padding:12px 16px;border-bottom:1px solid rgba(255,255,255,0.05);cursor:pointer;transition:background 0.15s;" ' +
            'onmouseover="this.style.background=\'rgba(255,255,255,0.04)\'" ' +
            'onmouseout="this.style.background=\'transparent\'" ' +
            'onclick="window.location.href=\'' + (n.link || '/notifications') + '\'">' +
            '<div style="display:flex;gap:10px;align-items:flex-start;">' +
            '<div style="width:8px;height:8px;border-radius:50%;background:' + color + ';margin-top:6px;flex-shrink:0;' + (n.isRead ? 'opacity:0.3;' : '') + '"></div>' +
            '<div style="flex:1;min-width:0;">' +
            '<div style="color:' + (n.isRead ? '#64748b' : '#e2e8f0') + ';font-size:0.83rem;line-height:1.4;">' + (n.message || '') + '</div>' +
            '<div style="color:#64748b;font-size:0.72rem;margin-top:3px;">' + (n.createdAt ? new Date(n.createdAt).toLocaleString('vi-VN') : '') + '</div>' +
            '</div></div></div>';
        }).join('');

      panel.innerHTML = [
        '<div style="background:linear-gradient(135deg,#f59e0b,#f97316);padding:14px 18px;display:flex;align-items:center;justify-content:space-between;">',
        '<div style="display:flex;align-items:center;gap:8px;">',
        '<span style="font-size:1.1rem;"><i class="bi bi-bell-fill"></i></span>',
        '<span style="color:white;font-weight:700;">Thông báo</span>',
        unreadCount > 0 ? '<span style="background:rgba(255,255,255,0.25);color:white;border-radius:10px;padding:1px 8px;font-size:0.72rem;font-weight:700;">' + unreadCount + ' mới</span>' : '',
        '</div>',
        '<div style="display:flex;gap:8px;">',
        '<button onclick="HRMS.notif.markAllRead()" style="background:rgba(255,255,255,0.2);border:none;color:white;border-radius:6px;padding:4px 10px;font-size:0.72rem;cursor:pointer;">Đã đọc</button>',
        '<button onclick="document.getElementById(\'hrms-notif-panel\').remove()" style="background:rgba(255,255,255,0.2);border:none;color:white;width:24px;height:24px;border-radius:50%;cursor:pointer;font-size:0.9rem;">x</button>',
        '</div></div>',
        '<div style="max-height:360px;overflow-y:auto;">' + notifHtml + '</div>',
        '<div style="padding:10px 16px;border-top:1px solid rgba(255,255,255,0.06);text-align:center;">',
        '<a href="/notifications" style="color:#f59e0b;font-size:0.82rem;text-decoration:none;font-weight:600;">Xem tất cả thông báo →</a>',
        '</div>'
      ].join('');

      document.body.appendChild(panel);

      setTimeout(function () {
        document.addEventListener('click', function closePanel(e) {
          if (!panel.contains(e.target) && e.target.id !== 'hrms-notif-btn') {
            panel.remove();
            document.removeEventListener('click', closePanel);
          }
        });
      }, 100);
    });
  }

  function getCsrfToken() {
    var match = document.cookie.match(/XSRF-TOKEN=([^;]+)/);
    return match ? decodeURIComponent(match[1]) : '';
  }

  function init() {
    createNotifButton();
    fetchNotifications();
    setInterval(fetchNotifications, POLL_INTERVAL);
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }

  window.HRMS = window.HRMS || {};
  window.HRMS.notif = {
    markAllRead: function () {
      fetch('/api/notifications/mark-all-read', {
        method: 'PUT',
        headers: { 'X-XSRF-TOKEN': getCsrfToken() }
      })
        .then(function () {
          updateBadge(0);
          var panel = document.getElementById('hrms-notif-panel');
          if (panel) panel.remove();
        })
        .catch(function () {});
    },
    refresh: fetchNotifications
  };
})();
