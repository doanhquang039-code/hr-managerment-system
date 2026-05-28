package com.example.hr.controllers;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.hr.enums.AttendanceStatus;
import com.example.hr.models.Attendance;
import com.example.hr.models.User;
import com.example.hr.repository.AttendanceRepository;
import com.example.hr.repository.UserRepository;
import com.example.hr.service.AuthUserHelper;

@Controller
public class AttendanceController {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthUserHelper authUserHelper;

    // ==================== ADMIN VIEWS ====================

    @GetMapping("/admin/attendance")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String adminList(@RequestParam(required = false) String keyword,
                            @RequestParam(required = false) Integer month,
                            @RequestParam(required = false) Integer year,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int size,
                            Model model) {
        int currentMonth = (month != null) ? month : LocalDate.now().getMonthValue();
        int currentYear  = (year  != null) ? year  : LocalDate.now().getYear();

        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "attendanceDate"));
        org.springframework.data.domain.Page<Attendance> attendancePage;

        if (keyword != null && !keyword.isBlank()) {
            attendancePage = attendanceRepository.findAllWithUser(keyword.trim(), pageable);
        } else {
            LocalDate start = LocalDate.of(currentYear, currentMonth, 1);
            LocalDate end   = start.withDayOfMonth(start.lengthOfMonth());
            attendancePage = attendanceRepository.findByAttendanceDateBetween(start, end, pageable);
        }

        model.addAttribute("attendances", attendancePage.getContent());
        model.addAttribute("attendancePage", attendancePage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", attendancePage.getTotalPages());
        model.addAttribute("totalItems", attendancePage.getTotalElements());
        model.addAttribute("keyword", keyword);
        model.addAttribute("month", currentMonth);
        model.addAttribute("year", currentYear);
        model.addAttribute("users", userRepository.findAll());
        return "admin/attendance-list";
    }

    @GetMapping("/admin/attendance/add")
    @PreAuthorize("hasRole('ADMIN')")
    public String showAddForm(Model model) {
        model.addAttribute("attendance", new Attendance());
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("statuses", AttendanceStatus.values());
        return "admin/attendance-form";
    }

    @PostMapping("/admin/attendance/save")
    @PreAuthorize("hasRole('ADMIN')")
    public String saveAttendance(@ModelAttribute Attendance attendance) {
        attendanceRepository.save(attendance);
        return "redirect:/admin/attendance";
    }

    @GetMapping("/admin/attendance/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteAttendance(@PathVariable Integer id) {
        attendanceRepository.deleteById(id);
        return "redirect:/admin/attendance";
    }

    // ==================== USER VIEWS ====================

    @GetMapping({"/user/attendance", "/user1/attendance"})
    @PreAuthorize("isAuthenticated()")
    public String userAttendance(@RequestParam(required = false) Integer month,
                                 @RequestParam(required = false) Integer year,
                                 Authentication auth,
                                 Model model) {
        User currentUser = authUserHelper.getCurrentUser(auth);
        if (currentUser == null) throw new RuntimeException("Người dùng không tồn tại");

        int currentMonth = (month != null) ? month : LocalDate.now().getMonthValue();
        int currentYear  = (year  != null) ? year  : LocalDate.now().getYear();

        List<Attendance> myAttendances = attendanceRepository
                .findByUserAndYearAndMonth(currentUser, currentYear, currentMonth);

        // Kiểm tra hôm nay đã check-in chưa
        Optional<Attendance> todayRecord = attendanceRepository
                .findByUserAndAttendanceDate(currentUser, LocalDate.now());

        model.addAttribute("attendances", myAttendances);
        model.addAttribute("todayRecord", todayRecord.orElse(null));
        model.addAttribute("month", currentMonth);
        model.addAttribute("year", currentYear);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("today", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        return "user1/attendance";
    }

    @PostMapping({"/user/attendance/checkin", "/user1/attendance/checkin"})
    @PreAuthorize("isAuthenticated()")
    public String checkIn(Authentication auth) {
        User currentUser = authUserHelper.getCurrentUser(auth);
        if (currentUser == null) throw new RuntimeException("Người dùng không tồn tại");

        LocalDate today = LocalDate.now();
        Optional<Attendance> existing = attendanceRepository
                .findByUserAndAttendanceDate(currentUser, today);

        if (existing.isEmpty()) {
            Attendance attendance = new Attendance();
            attendance.setUser(currentUser);
            attendance.setAttendanceDate(today);
            LocalTime now = LocalTime.now();
            attendance.setCheckInTime(now);

            // Tính trạng thái: LATE nếu sau 8:30
            LocalTime lateThreshold = LocalTime.of(8, 30);
            attendance.setStatus(now.isAfter(lateThreshold)
                    ? AttendanceStatus.LATE : AttendanceStatus.PRESENT);

            attendanceRepository.save(attendance);
        }
        return "redirect:/user1/attendance";
    }

    @PostMapping({"/user/attendance/checkout", "/user1/attendance/checkout"})
    @PreAuthorize("isAuthenticated()")
    public String checkOut(Authentication auth) {
        User currentUser = authUserHelper.getCurrentUser(auth);
        if (currentUser == null) throw new RuntimeException("Người dùng không tồn tại");

        Optional<Attendance> existing = attendanceRepository
                .findByUserAndAttendanceDate(currentUser, LocalDate.now());

        if (existing.isPresent()) {
            Attendance attendance = existing.get();
            LocalTime now = LocalTime.now();
            attendance.setCheckOutTime(now);

            // Nếu chưa đánh dấu LATE: xét EARLY_LEAVE nếu về trước 17:30
            if (attendance.getStatus() == AttendanceStatus.PRESENT) {
                LocalTime earlyThreshold = LocalTime.of(17, 30);
                if (now.isBefore(earlyThreshold)) {
                    attendance.setStatus(AttendanceStatus.EARLY_LEAVE);
                }
            }
            attendanceRepository.save(attendance);
        }
        return "redirect:/user1/attendance";
    }
}
