/*
 * HRMS DataTable
 * Adds client-side search, column filters, sorting, pagination, and CSV export
 * to tables with .hrms-dt or .data-table.
 */
(function () {
  'use strict';

  var instances = [];

  function textOf(cell) {
    return (cell && (cell.getAttribute('data-sort-value') || cell.textContent) || '').trim();
  }

  function normalize(value) {
    return (value || '').toString().toLowerCase().trim();
  }

  function button(label, title) {
    var btn = document.createElement('button');
    btn.type = 'button';
    btn.textContent = label;
    btn.title = title || label;
    btn.className = 'btn btn-sm btn-outline-secondary';
    return btn;
  }

  function DataTable(table) {
    this.table = table;
    this.tbody = table.querySelector('tbody');
    if (!this.tbody) return;
    this.allRows = Array.from(this.tbody.querySelectorAll('tr'));
    this.filteredRows = this.allRows.slice();
    this.currentPage = 1;
    this.pageSize = parseInt(table.getAttribute('data-page-size') || '10', 10);
    this.sortCol = -1;
    this.sortDir = 'asc';
    this.searchQuery = '';
    this.columnFilters = {};
    this.originalCells = new WeakMap();
    this.init();
  }

  DataTable.prototype.init = function () {
    if (!this.allRows.length) return;

    var wrapper = document.createElement('div');
    wrapper.className = 'hrms-dt-wrapper';
    this.table.parentNode.insertBefore(wrapper, this.table);
    wrapper.appendChild(this.table);
    this.wrapper = wrapper;

    this.allRows.forEach(function (row) {
      Array.from(row.children).forEach(function (cell) {
        this.originalCells.set(cell, cell.innerHTML);
      }, this);
    }, this);

    this.buildToolbar();
    this.makeSortable();
    this.applyFilters();
  };

  DataTable.prototype.buildToolbar = function () {
    var self = this;
    var toolbar = document.createElement('div');
    toolbar.className = 'hrms-dt-toolbar d-flex align-items-center gap-2 flex-wrap mb-3';

    var search = document.createElement('input');
    search.type = 'search';
    search.className = 'form-control form-control-sm';
    search.placeholder = this.table.getAttribute('data-search-placeholder') || 'Search table...';
    search.style.maxWidth = '320px';
    toolbar.appendChild(search);

    this.buildColumnFilters(toolbar);

    var pageSize = document.createElement('select');
    pageSize.className = 'form-select form-select-sm';
    pageSize.style.width = 'auto';
    [5, 10, 25, 50, 100].forEach(function (n) {
      var option = document.createElement('option');
      option.value = n;
      option.textContent = n + ' rows';
      option.selected = n === self.pageSize;
      pageSize.appendChild(option);
    });
    toolbar.appendChild(pageSize);

    var exportBtn = button('CSV', 'Export filtered rows to CSV');
    toolbar.appendChild(exportBtn);

    var resetBtn = button('Reset', 'Clear search, filters, and sorting');
    resetBtn.style.display = 'none';
    toolbar.appendChild(resetBtn);

    var info = document.createElement('span');
    info.className = 'hrms-dt-info text-muted small ms-auto';
    toolbar.appendChild(info);

    search.addEventListener('input', function () {
      self.searchQuery = normalize(this.value);
      self.currentPage = 1;
      self.applyFilters();
    });

    pageSize.addEventListener('change', function () {
      self.pageSize = parseInt(this.value, 10);
      self.currentPage = 1;
      self.render();
    });

    exportBtn.addEventListener('click', function () {
      self.exportCSV();
    });

    resetBtn.addEventListener('click', function () {
      search.value = '';
      self.searchQuery = '';
      self.columnFilters = {};
      self.sortCol = -1;
      self.sortDir = 'asc';
      toolbar.querySelectorAll('select[data-filter-column]').forEach(function (select) {
        select.value = '';
      });
      self.table.querySelectorAll('.hrms-sort-indicator').forEach(function (indicator) {
        indicator.textContent = '<>';
      });
      self.applyFilters();
    });

    this.wrapper.insertBefore(toolbar, this.table);
    this.infoEl = info;
    this.resetBtn = resetBtn;
  };

  DataTable.prototype.buildColumnFilters = function (toolbar) {
    var self = this;
    var headers = Array.from(this.table.querySelectorAll('thead th'));
    var filterNames = [
      'status', 'trang thai', 'type', 'loai', 'role', 'vai tro',
      'department', 'phong ban', 'category', 'danh muc', 'gender', 'gioi tinh'
    ];

    headers.forEach(function (th, index) {
      if (th.hasAttribute('data-no-filter')) return;
      var label = normalize(th.textContent.replace(/[<>]/g, ''));
      var explicit = th.hasAttribute('data-filter');
      if (!explicit && filterNames.indexOf(label) === -1) return;

      var values = new Set();
      self.allRows.forEach(function (row) {
        var value = textOf(row.children[index]);
        if (value && value.length <= 60) values.add(value);
      });
      if (values.size < 2 || values.size > 40) return;

      var select = document.createElement('select');
      select.className = 'form-select form-select-sm';
      select.style.width = 'auto';
      select.setAttribute('data-filter-column', index);

      var all = document.createElement('option');
      all.value = '';
      all.textContent = th.textContent.trim().replace(/[<>]/g, '') + ': All';
      select.appendChild(all);

      Array.from(values).sort(function (a, b) { return a.localeCompare(b, 'vi'); }).forEach(function (value) {
        var option = document.createElement('option');
        option.value = value;
        option.textContent = value.length > 24 ? value.slice(0, 24) + '...' : value;
        select.appendChild(option);
      });

      select.addEventListener('change', function () {
        self.columnFilters[index] = this.value;
        self.currentPage = 1;
        self.applyFilters();
      });

      toolbar.appendChild(select);
    });
  };

  DataTable.prototype.makeSortable = function () {
    var self = this;
    Array.from(this.table.querySelectorAll('thead th')).forEach(function (th, index) {
      var text = normalize(th.textContent);
      if (!text || text === 'actions' || text === 'action' || text === 'thao tac' || th.hasAttribute('data-no-sort')) return;

      th.style.cursor = 'pointer';
      th.style.userSelect = 'none';
      var indicator = document.createElement('span');
      indicator.className = 'hrms-sort-indicator ms-1 text-muted';
      indicator.textContent = '<>';
      th.appendChild(indicator);

      th.addEventListener('click', function () {
        self.sortDir = self.sortCol === index && self.sortDir === 'asc' ? 'desc' : 'asc';
        self.sortCol = index;
        self.table.querySelectorAll('.hrms-sort-indicator').forEach(function (el) {
          el.textContent = '<>';
        });
        indicator.textContent = self.sortDir === 'asc' ? '^' : 'v';
        self.applyFilters();
      });
    });
  };

  DataTable.prototype.restoreCells = function () {
    this.allRows.forEach(function (row) {
      Array.from(row.children).forEach(function (cell) {
        if (this.originalCells.has(cell)) cell.innerHTML = this.originalCells.get(cell);
      }, this);
    }, this);
  };

  DataTable.prototype.applyFilters = function () {
    var self = this;
    var q = this.searchQuery;
    this.filteredRows = this.allRows.filter(function (row) {
      if (q && normalize(row.textContent).indexOf(q) === -1) return false;
      return Object.keys(self.columnFilters).every(function (key) {
        var value = self.columnFilters[key];
        if (!value) return true;
        return normalize(textOf(row.children[parseInt(key, 10)])).indexOf(normalize(value)) !== -1;
      });
    });

    if (this.sortCol >= 0) {
      var col = this.sortCol;
      var dir = this.sortDir === 'asc' ? 1 : -1;
      this.filteredRows.sort(function (a, b) {
        var av = textOf(a.children[col]);
        var bv = textOf(b.children[col]);
        var an = Number(av.replace(/[^0-9.-]/g, ''));
        var bn = Number(bv.replace(/[^0-9.-]/g, ''));
        var cmp = !isNaN(an) && !isNaN(bn) && av.match(/\d/) && bv.match(/\d/)
          ? an - bn
          : av.localeCompare(bv, 'vi');
        return cmp * dir;
      });
    }

    if (this.resetBtn) {
      var hasFilters = q || Object.keys(this.columnFilters).some(function (key) { return self.columnFilters[key]; }) || this.sortCol >= 0;
      this.resetBtn.style.display = hasFilters ? '' : 'none';
    }
    this.render();
  };

  DataTable.prototype.render = function () {
    this.restoreCells();
    var total = this.filteredRows.length;
    var pages = Math.max(1, Math.ceil(total / this.pageSize));
    this.currentPage = Math.min(this.currentPage, pages);
    var start = (this.currentPage - 1) * this.pageSize;
    var end = Math.min(start + this.pageSize, total);
    var shown = this.filteredRows.slice(start, end);

    this.allRows.forEach(function (row) { row.style.display = 'none'; });
    shown.forEach(function (row) { row.style.display = ''; });

    var empty = this.tbody.querySelector('.hrms-dt-empty');
    if (empty) empty.remove();
    if (total === 0) {
      empty = document.createElement('tr');
      empty.className = 'hrms-dt-empty';
      empty.innerHTML = '<td colspan="' + this.table.querySelectorAll('thead th').length + '" class="text-center text-muted py-4">No results found</td>';
      this.tbody.appendChild(empty);
    }

    if (this.infoEl) {
      this.infoEl.textContent = total === 0 ? 'No rows' : 'Showing ' + (start + 1) + '-' + end + ' of ' + total;
    }
    this.renderPagination(pages);
  };

  DataTable.prototype.renderPagination = function (pages) {
    var self = this;
    var old = this.wrapper.querySelector('.hrms-dt-pagination');
    if (old) old.remove();
    if (pages <= 1) return;

    var nav = document.createElement('div');
    nav.className = 'hrms-dt-pagination d-flex justify-content-center gap-1 flex-wrap mt-3';
    var add = function (label, page, disabled, active) {
      var btn = button(label);
      if (active) btn.className = 'btn btn-sm btn-primary';
      btn.disabled = disabled;
      btn.addEventListener('click', function () {
        self.currentPage = page;
        self.render();
      });
      nav.appendChild(btn);
    };

    add('Prev', Math.max(1, this.currentPage - 1), this.currentPage === 1, false);
    for (var i = Math.max(1, this.currentPage - 2); i <= Math.min(pages, this.currentPage + 2); i++) {
      add(String(i), i, false, i === this.currentPage);
    }
    add('Next', Math.min(pages, this.currentPage + 1), this.currentPage === pages, false);
    this.wrapper.appendChild(nav);
  };

  DataTable.prototype.exportCSV = function () {
    var headers = Array.from(this.table.querySelectorAll('thead th'))
      .map(function (th) { return '"' + th.textContent.replace(/[<>^v]/g, '').trim().replace(/"/g, '""') + '"'; });
    var rows = this.filteredRows.map(function (row) {
      return Array.from(row.children).map(function (td) {
        return '"' + textOf(td).replace(/"/g, '""') + '"';
      }).join(',');
    });
    var blob = new Blob(['\uFEFF' + [headers.join(',')].concat(rows).join('\n')], { type: 'text/csv;charset=utf-8;' });
    var url = URL.createObjectURL(blob);
    var link = document.createElement('a');
    link.href = url;
    link.download = 'hrms-export-' + new Date().toISOString().slice(0, 10) + '.csv';
    link.click();
    URL.revokeObjectURL(url);
  };

  function initAll() {
    document.querySelectorAll('table.hrms-dt, table.data-table').forEach(function (table) {
      if (!table.getAttribute('data-dt-init')) {
        table.setAttribute('data-dt-init', '1');
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
  window.HRMS.dataTableInstances = instances;
})();
