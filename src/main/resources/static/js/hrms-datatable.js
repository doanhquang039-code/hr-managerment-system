/**
 * HRMS DataTable v1
 * Client-side: search, filter, sort, pagination cho tất cả bảng
 * Dùng: <table class="hrms-dt" data-page-size="10">
 */
(function () {
  'use strict';

  var instances = [];

  // ==================== CORE CLASS ====================
  function DataTable(table) {
    this.table = table;
    this.tbody = table.querySelector('tbody');
    this.allRows = [];
    this.filteredRows = [];
    this.currentPage = 1;
    this.pageSize = parseInt(table.getAttribute('data-page-size') || '10');
    this.sortCol = -1;
    this.sortDir = 'asc';
    this.searchQuery = '';
    this.columnFilters = {};
    this.wrapper = null;
    this.init();
  }

  DataTable.prototype.init = function () {
    // Wrap table
    var wrapper = document.createElement('div');
    wrapper.className = 'hrms-dt-wrapper';
    wrapper.style.cssText = 'position:relative;';
    this.table.parentNode.insertBefore(wrapper, this.table);
    wrapper.appendChild(this.table);
    this.wrapper = wrapper;

    // Capture all rows
    this.allRows = Array.from(this.tbody.querySelectorAll('tr'));
    this.filteredRows = this.allRows.slice();

    // Build toolbar
    this.buildToolbar();

    // Make headers sortable
    this.makeSortable();

    // Initial render
    this.render();
  };

  DataTable.prototype.buildToolbar = function () {
    var self = this;
    var toolbar = document.createElement('div');
    toolbar.className = 'hrms-dt-toolbar';
    toolbar.style.cssText = 'display:flex;align-items:center;gap:10px;margin-bottom:12px;flex-wrap:wrap;';

    // Search input
    var searchWrap = document.createElement('div');
    searchWrap.style.cssText = 'position:relative;flex:1;min-width:200px;';
    searchWrap.innerHTML = [
      '<span style="position:absolute;left:10px;top:50%;transform:translateY(-50%);color:#64748b;font-size:0.85rem;">🔍</span>',
      '<input type="text" placeholder="Search all columns..." ',
        'style="width:100%;padding:8px 10px 8px 32px;background:#2a2a3e;border:1px solid rgba(255,255,255,0.1);',
        'border-radius:10px;color:white;font-size:0.85rem;outline:none;box-sizing:border-box;" ',
        'class="hrms-dt-search">',
    ].join('');
    toolbar.appendChild(searchWrap);

    // Column filter dropdowns — auto-detect columns with few unique values
    var headers = Array.from(this.table.querySelectorAll('thead th'));
    headers.forEach(function(th, i) {
      var text = th.textContent.trim().replace(/[⇅↑↓]/g,'').trim().toLowerCase();
      // Only add filter for status/type/role columns
      var filterCols = ['trạng thái','status','loại','type','vai trò','role','phòng ban','department','danh mục','category','giới tính','gender'];
      if (!filterCols.includes(text)) return;

      // Collect unique values
      var values = new Set();
      self.allRows.forEach(function(row) {
        var cell = row.querySelectorAll('td')[i];
        if (cell) {
          var v = cell.textContent.trim();
          if (v && v.length < 50) values.add(v);
        }
      });
      if (values.size < 2 || values.size > 20) return;

      var sel = document.createElement('select');
      sel.style.cssText = 'padding:8px 12px;background:#2a2a3e;border:1px solid rgba(255,255,255,0.1);border-radius:10px;color:#94a3b8;font-size:0.82rem;cursor:pointer;max-width:150px;';
      var defaultOpt = document.createElement('option');
      defaultOpt.value = '';
      defaultOpt.textContent = th.textContent.trim().replace(/[⇅↑↓]/g,'').trim() + ': All';
      sel.appendChild(defaultOpt);
      Array.from(values).sort().forEach(function(v) {
        var opt = document.createElement('option');
        opt.value = v;
        opt.textContent = v.length > 20 ? v.substring(0,20)+'...' : v;
        sel.appendChild(opt);
      });
      sel.addEventListener('change', function() {
        self.columnFilters[i] = this.value;
        self.currentPage = 1;
        self.applyFilters();
      });
      toolbar.appendChild(sel);
    });

    // Page size selector
    var pageSel = document.createElement('select');
    pageSel.style.cssText = 'padding:8px 12px;background:#2a2a3e;border:1px solid rgba(255,255,255,0.1);border-radius:10px;color:#94a3b8;font-size:0.82rem;cursor:pointer;';
    [5, 10, 25, 50, 100].forEach(function (n) {
      var opt = document.createElement('option');
      opt.value = n;
      opt.textContent = n + ' rows';
      if (n === self.pageSize) opt.selected = true;
      pageSel.appendChild(opt);
    });
    toolbar.appendChild(pageSel);

    // Export CSV button
    var exportBtn = document.createElement('button');
    exportBtn.innerHTML = '📥 CSV';
    exportBtn.title = 'Export to CSV';
    exportBtn.style.cssText = 'padding:8px 14px;background:rgba(99,102,241,0.15);border:1px solid rgba(99,102,241,0.3);border-radius:10px;color:#6366f1;font-size:0.82rem;cursor:pointer;font-weight:600;white-space:nowrap;';
    toolbar.appendChild(exportBtn);

    // Reset filters button
    var resetBtn = document.createElement('button');
    resetBtn.innerHTML = '✕ Reset';
    resetBtn.title = 'Reset all filters';
    resetBtn.style.cssText = 'padding:8px 14px;background:rgba(239,68,68,0.1);border:1px solid rgba(239,68,68,0.2);border-radius:10px;color:#ef4444;font-size:0.82rem;cursor:pointer;font-weight:600;white-space:nowrap;display:none;';
    toolbar.appendChild(resetBtn);

    // Info text
    var info = document.createElement('span');
    info.className = 'hrms-dt-info';
    info.style.cssText = 'color:#64748b;font-size:0.78rem;white-space:nowrap;margin-left:auto;';
    toolbar.appendChild(info);

    this.wrapper.insertBefore(toolbar, this.table);

    // Events
    var searchInput = toolbar.querySelector('.hrms-dt-search');

    searchInput.addEventListener('input', function () {
      self.searchQuery = this.value.toLowerCase().trim();
      self.currentPage = 1;
      resetBtn.style.display = self.searchQuery ? 'block' : 'none';
      self.applyFilters();
    });

    pageSel.addEventListener('change', function () {
      self.pageSize = parseInt(this.value);
      self.currentPage = 1;
      self.render();
    });

    exportBtn.addEventListener('click', function () { self.exportCSV(); });

    resetBtn.addEventListener('click', function() {
      searchInput.value = '';
      self.searchQuery = '';
      self.columnFilters = {};
      self.sortCol = -1;
      self.sortDir = 'asc';
      toolbar.querySelectorAll('select').forEach(function(s) { s.value = ''; });
      self.table.querySelectorAll('.hrms-sort-indicator').forEach(function(s) { s.textContent = '⇅'; s.style.color = '#475569'; });
      resetBtn.style.display = 'none';
      self.applyFilters();
    });

    this.infoEl = info;
    this.toolbar = toolbar;
    this.resetBtn = resetBtn;
  };

  DataTable.prototype.makeSortable = function () {
    var self = this;
    var headers = this.table.querySelectorAll('thead th');
    headers.forEach(function (th, i) {
      // Skip action columns
      var text = th.textContent.trim().toLowerCase();
      if (text === 'thao tác' || text === 'action' || text === '') return;

      th.style.cursor = 'pointer';
      th.style.userSelect = 'none';
      th.style.position = 'relative';

      var indicator = document.createElement('span');
      indicator.className = 'hrms-sort-indicator';
      indicator.style.cssText = 'margin-left:6px;color:#475569;font-size:0.7rem;';
      indicator.textContent = '⇅';
      th.appendChild(indicator);

      th.addEventListener('click', function () {
        if (self.sortCol === i) {
          self.sortDir = self.sortDir === 'asc' ? 'desc' : 'asc';
        } else {
          self.sortCol = i;
          self.sortDir = 'asc';
        }
        // Update indicators
        self.table.querySelectorAll('.hrms-sort-indicator').forEach(function (s) {
          s.textContent = '⇅';
          s.style.color = '#475569';
        });
        indicator.textContent = self.sortDir === 'asc' ? '↑' : '↓';
        indicator.style.color = '#6366f1';
        self.applyFilters();
      });
    });
  };

  DataTable.prototype.applyFilters = function () {
    var self = this;
    var q = this.searchQuery;

    this.filteredRows = this.allRows.filter(function (row) {
      // Global search
      if (q) {
        var text = row.textContent.toLowerCase();
        if (!text.includes(q)) return false;
      }
      // Column filters
      var cells = row.querySelectorAll('td');
      for (var col in self.columnFilters) {
        var val = self.columnFilters[col].toLowerCase();
        if (!val) continue;
        var cell = cells[parseInt(col)];
        if (!cell) continue;
        if (!cell.textContent.toLowerCase().includes(val)) return false;
      }
      return true;
    });

    // Sort
    if (this.sortCol >= 0) {
      var col = this.sortCol;
      var dir = this.sortDir;
      this.filteredRows.sort(function (a, b) {
        var aText = (a.querySelectorAll('td')[col] || {}).textContent || '';
        var bText = (b.querySelectorAll('td')[col] || {}).textContent || '';
        // Try numeric
        var aNum = parseFloat(aText.replace(/[^0-9.-]/g, ''));
        var bNum = parseFloat(bText.replace(/[^0-9.-]/g, ''));
        var cmp;
        if (!isNaN(aNum) && !isNaN(bNum)) {
          cmp = aNum - bNum;
        } else {
          cmp = aText.localeCompare(bText, 'vi');
        }
        return dir === 'asc' ? cmp : -cmp;
      });
    }

    this.currentPage = 1;
    this.render();
  };

  DataTable.prototype.render = function () {
    var total = this.filteredRows.length;
    var pages = Math.max(1, Math.ceil(total / this.pageSize));
    this.currentPage = Math.min(this.currentPage, pages);

    var start = (this.currentPage - 1) * this.pageSize;
    var end = Math.min(start + this.pageSize, total);
    var pageRows = this.filteredRows.slice(start, end);

    // Hide all rows
    this.allRows.forEach(function (r) { r.style.display = 'none'; });
    // Show page rows
    pageRows.forEach(function (r) {
      r.style.display = '';
      // Highlight search matches
      if (self.searchQuery) {
        r.querySelectorAll('td').forEach(function(td) {
          var text = td.textContent;
          var lower = text.toLowerCase();
          if (lower.includes(self.searchQuery)) {
            var regex = new RegExp('(' + self.searchQuery.replace(/[.*+?^${}()|[\]\\]/g,'\\$&') + ')', 'gi');
            // Only highlight text nodes, not HTML
            if (!td.querySelector('a,button,span.status-badge,span.badge')) {
              td.innerHTML = td.textContent.replace(regex, '<mark style="background:rgba(99,102,241,0.3);color:white;border-radius:3px;padding:0 2px;">$1</mark>');
            }
          }
        });
      }
    });

    // Empty state
    var emptyRow = this.tbody.querySelector('.hrms-dt-empty');
    if (emptyRow) emptyRow.remove();
    if (total === 0) {
      var cols = (this.table.querySelector('thead tr') || {}).children;
      var colCount = cols ? cols.length : 5;
      var empty = document.createElement('tr');
      empty.className = 'hrms-dt-empty';
      empty.innerHTML = '<td colspan="' + colCount + '" style="text-align:center;padding:40px;color:#475569;">' +
        '<div style="font-size:2rem;margin-bottom:8px;">🔍</div>' +
        '<div style="font-weight:600;">No results found</div>' +
        '<div style="font-size:0.82rem;margin-top:4px;">Try adjusting your search or filters</div>' +
        '</td>';
      this.tbody.appendChild(empty);
    }

    // Update info
    if (this.infoEl) {
      this.infoEl.textContent = total === 0
        ? 'No results'
        : 'Showing ' + (start + 1) + '-' + end + ' of ' + total + ' rows';
    }

    // Render pagination
    this.renderPagination(pages);
  };

  DataTable.prototype.renderPagination = function (pages) {
    var self = this;
    var existing = this.wrapper.querySelector('.hrms-dt-pagination');
    if (existing) existing.remove();
    if (pages <= 1) return;

    var pag = document.createElement('div');
    pag.className = 'hrms-dt-pagination';
    pag.style.cssText = 'display:flex;align-items:center;justify-content:center;gap:6px;margin-top:14px;flex-wrap:wrap;';

    var btnStyle = 'padding:6px 12px;border-radius:8px;border:1px solid rgba(255,255,255,0.1);cursor:pointer;font-size:0.82rem;font-weight:600;transition:all 0.15s;';

    // Prev
    var prev = document.createElement('button');
    prev.textContent = '← Prev';
    prev.style.cssText = btnStyle + 'background:rgba(255,255,255,0.06);color:#94a3b8;';
    prev.disabled = this.currentPage === 1;
    if (this.currentPage === 1) prev.style.opacity = '0.4';
    prev.addEventListener('click', function () {
      if (self.currentPage > 1) { self.currentPage--; self.render(); }
    });
    pag.appendChild(prev);

    // Page numbers
    var startPage = Math.max(1, this.currentPage - 2);
    var endPage = Math.min(pages, startPage + 4);
    if (endPage - startPage < 4) startPage = Math.max(1, endPage - 4);

    if (startPage > 1) {
      pag.appendChild(this._pageBtn(1, pages));
      if (startPage > 2) {
        var dots = document.createElement('span');
        dots.textContent = '...';
        dots.style.color = '#475569';
        pag.appendChild(dots);
      }
    }

    for (var i = startPage; i <= endPage; i++) {
      pag.appendChild(this._pageBtn(i, pages));
    }

    if (endPage < pages) {
      if (endPage < pages - 1) {
        var dots2 = document.createElement('span');
        dots2.textContent = '...';
        dots2.style.color = '#475569';
        pag.appendChild(dots2);
      }
      pag.appendChild(this._pageBtn(pages, pages));
    }

    // Next
    var next = document.createElement('button');
    next.textContent = 'Next →';
    next.style.cssText = btnStyle + 'background:rgba(255,255,255,0.06);color:#94a3b8;';
    next.disabled = this.currentPage === pages;
    if (this.currentPage === pages) next.style.opacity = '0.4';
    next.addEventListener('click', function () {
      if (self.currentPage < pages) { self.currentPage++; self.render(); }
    });
    pag.appendChild(next);

    this.wrapper.appendChild(pag);
  };

  DataTable.prototype._pageBtn = function (n, pages) {
    var self = this;
    var btn = document.createElement('button');
    btn.textContent = n;
    var isActive = n === this.currentPage;
    btn.style.cssText = 'padding:6px 12px;border-radius:8px;border:1px solid ' +
      (isActive ? '#6366f1' : 'rgba(255,255,255,0.1)') + ';cursor:pointer;font-size:0.82rem;font-weight:' +
      (isActive ? '700' : '500') + ';background:' +
      (isActive ? 'linear-gradient(135deg,#6366f1,#8b5cf6)' : 'rgba(255,255,255,0.06)') +
      ';color:' + (isActive ? 'white' : '#94a3b8') + ';transition:all 0.15s;';
    btn.addEventListener('click', function () {
      self.currentPage = n;
      self.render();
    });
    return btn;
  };

  DataTable.prototype.exportCSV = function () {
    var headers = Array.from(this.table.querySelectorAll('thead th'))
      .map(function (th) { return '"' + th.textContent.trim().replace(/[⇅↑↓]/g, '').trim() + '"'; })
      .join(',');

    var rows = this.filteredRows.map(function (row) {
      return Array.from(row.querySelectorAll('td'))
        .map(function (td) { return '"' + td.textContent.trim().replace(/"/g, '""') + '"'; })
        .join(',');
    });

    var csv = [headers].concat(rows).join('\n');
    var blob = new Blob(['\uFEFF' + csv], { type: 'text/csv;charset=utf-8;' });
    var url = URL.createObjectURL(blob);
    var a = document.createElement('a');
    a.href = url;
    a.download = 'hrms-export-' + new Date().toISOString().slice(0, 10) + '.csv';
    a.click();
    URL.revokeObjectURL(url);

    if (window.HRMS && window.HRMS.toast) {
      window.HRMS.toast('Exported ' + this.filteredRows.length + ' rows to CSV', 'success');
    }
  };

  // ==================== AUTO INIT ====================
  function initAll() {
    document.querySelectorAll('table.hrms-dt, table.data-table').forEach(function (table) {
      if (!table.getAttribute('data-dt-init')) {
        table.setAttribute('data-dt-init', '1');
        // Add hrms-dt class if missing
        table.classList.add('hrms-dt');
        instances.push(new DataTable(table));
      }
    });
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initAll);
  } else {
    initAll();
  }

  window.HRMS = window.HRMS || {};
  window.HRMS.DataTable = DataTable;
  window.HRMS.initDataTables = initAll;
})();
