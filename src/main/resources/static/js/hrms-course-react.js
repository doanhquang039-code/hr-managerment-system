(function () {
  "use strict";

  window.HRMSReactIslands = window.HRMSReactIslands || {};

  var categoryLabels = {
    TECHNICAL: "Kỹ thuật",
    SOFT_SKILLS: "Kỹ năng mềm",
    COMPLIANCE: "Tuân thủ",
    LEADERSHIP: "Lãnh đạo",
    SALES: "Bán hàng",
    MANAGEMENT: "Quản lý"
  };

  var levelLabels = {
    BEGINNER: "Cơ bản",
    INTERMEDIATE: "Trung cấp",
    ADVANCED: "Nâng cao"
  };

  function labelOf(map, value) {
    return map[value] || value || "-";
  }

  function formatMinutes(value) {
    var minutes = Number(value || 0);
    if (!minutes) return "-";
    var hours = Math.floor(minutes / 60);
    var rest = minutes % 60;
    if (!hours) return minutes + " phút";
    return rest ? hours + " giờ " + rest + " phút" : hours + " giờ";
  }

  function CourseAdminList() {
    var React = window.React;
    var e = React.createElement;
    var cfg = window.HRMS_COURSE_REACT_CONFIG || { courses: [] };
    var courses = cfg.courses || [];

    var state = React.useState({ keyword: "", category: "ALL", level: "ALL" });
    var filters = state[0];
    var setFilters = state[1];

    var categories = Array.from(new Set(courses.map(function (c) { return c.category; }).filter(Boolean)));
    var levels = Array.from(new Set(courses.map(function (c) { return c.level; }).filter(Boolean)));
    var mandatoryCount = courses.filter(function (c) { return c.mandatory; }).length;
    var activeCount = courses.filter(function (c) { return c.active; }).length;

    var filtered = courses.filter(function (course) {
      var keyword = filters.keyword.trim().toLowerCase();
      var matchesKeyword = !keyword ||
        String(course.title || "").toLowerCase().indexOf(keyword) >= 0 ||
        String(course.description || "").toLowerCase().indexOf(keyword) >= 0;
      var matchesCategory = filters.category === "ALL" || course.category === filters.category;
      var matchesLevel = filters.level === "ALL" || course.level === filters.level;
      return matchesKeyword && matchesCategory && matchesLevel;
    });

    function update(name, value) {
      var next = Object.assign({}, filters);
      next[name] = value;
      setFilters(next);
    }

    function StatCard(props) {
      return e("div", { className: "course-react-stat " + (props.tone || "") },
        e("div", { className: "course-react-stat-icon" }, e("i", { className: "bi " + props.icon })),
        e("div", null,
          e("div", { className: "course-react-stat-label" }, props.label),
          e("div", { className: "course-react-stat-value" }, props.value)
        )
      );
    }

    function CourseCard(props) {
      var course = props.course;
      return e("article", { className: "course-react-card" },
        e("div", { className: "course-react-thumb" },
          course.thumbnailUrl
            ? e("img", { src: course.thumbnailUrl, alt: course.title || "Course" })
            : e("i", { className: "bi bi-book" })
        ),
        e("div", { className: "course-react-body" },
          e("div", { className: "course-react-tags" },
            e("span", { className: "course-react-tag primary" }, labelOf(categoryLabels, course.category)),
            e("span", { className: "course-react-tag" }, labelOf(levelLabels, course.level)),
            course.mandatory ? e("span", { className: "course-react-tag danger" }, "Bắt buộc") : null,
            course.active ? e("span", { className: "course-react-tag success" }, "Đang mở") : e("span", { className: "course-react-tag muted" }, "Tạm dừng")
          ),
          e("h3", null, course.title || "Khóa học"),
          e("p", null, course.description || "Chưa có mô tả cho khóa học này."),
        e("div", { className: "course-react-meta" },
            e("span", null, e("i", { className: "bi bi-clock" }), formatMinutes(course.durationMinutes)),
            e("span", null, e("i", { className: "bi bi-patch-check" }), course.passingScore ? course.passingScore + "% đạt" : "Chưa đặt điểm"),
            e("span", null, e("i", { className: course.videoUrl ? "bi bi-play-circle-fill" : "bi bi-play-circle" }), course.videoUrl ? "Có video" : "Chưa có video")
          )
        ),
        e("div", { className: "course-react-actions" },
          course.videoUrl ? e("a", { className: "btn btn-sm btn-outline-success", href: course.videoUrl, target: "_blank", rel: "noreferrer" }, e("i", { className: "bi bi-play-fill me-1" }), "Video") : null,
          e("a", { className: "btn btn-sm btn-outline-primary", href: "/lms/course/" + course.id }, e("i", { className: "bi bi-eye me-1" }), "Xem"),
          e("a", { className: "btn btn-sm btn-primary", href: "/admin/course/new" }, e("i", { className: "bi bi-plus-lg me-1" }), "Tạo mới")
        )
      );
    }

    return e("div", { className: "course-react-shell" },
      e("div", { className: "course-react-hero" },
        e("div", null,
          e("span", { className: "course-react-kicker" }, "LMS COURSE HUB"),
          e("h1", null, "Quản lý khóa học"),
          e("p", null, "Theo dõi danh mục, cấp độ, khóa bắt buộc và nội dung đào tạo trong một màn hình.")
        ),
        e("a", { href: "/admin/course/new", className: "course-react-new" }, e("i", { className: "bi bi-plus-lg" }), "Tạo khóa học mới")
      ),
      e("section", { className: "course-react-stats" },
        e(StatCard, { label: "Tổng khóa học", value: courses.length, icon: "bi-collection-play", tone: "blue" }),
        e(StatCard, { label: "Đang hoạt động", value: activeCount, icon: "bi-play-circle", tone: "green" }),
        e(StatCard, { label: "Bắt buộc", value: mandatoryCount, icon: "bi-exclamation-circle", tone: "red" }),
        e(StatCard, { label: "Danh mục", value: categories.length, icon: "bi-grid", tone: "purple" })
      ),
      e("section", { className: "course-react-filters" },
        e("div", { className: "course-react-search" },
          e("i", { className: "bi bi-search" }),
          e("input", {
            value: filters.keyword,
            onChange: function (ev) { update("keyword", ev.target.value); },
            placeholder: "Tìm khóa học..."
          })
        ),
        e("select", { value: filters.category, onChange: function (ev) { update("category", ev.target.value); } },
          e("option", { value: "ALL" }, "Tất cả danh mục"),
          categories.map(function (category) {
            return e("option", { key: category, value: category }, labelOf(categoryLabels, category));
          })
        ),
        e("select", { value: filters.level, onChange: function (ev) { update("level", ev.target.value); } },
          e("option", { value: "ALL" }, "Tất cả cấp độ"),
          levels.map(function (level) {
            return e("option", { key: level, value: level }, labelOf(levelLabels, level));
          })
        )
      ),
      e("section", { className: "course-react-list" },
        filtered.length
          ? filtered.map(function (course) { return e(CourseCard, { key: course.id, course: course }); })
          : e("div", { className: "course-react-empty" },
              e("i", { className: "bi bi-folder2-open" }),
              e("strong", null, "Không có khóa học phù hợp"),
              e("span", null, "Thử đổi bộ lọc hoặc tạo khóa học mới.")
            )
      )
    );
  }

  window.HRMSReactIslands.CourseAdminList = CourseAdminList;
})();
