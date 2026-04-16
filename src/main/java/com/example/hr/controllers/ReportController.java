package com.example.hr.controllers;

import com.example.hr.enums.AttendanceStatus;
import com.example.hr.enums.UserStatus;
import com.example.hr.models.Attendance;
import com.example.hr.models.Department;
import com.example.hr.models.PerformanceReview;
import com.example.hr.models.Payroll;
import com.example.hr.models.User;
import com.example.hr.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/reports")
public class ReportController {

    @Autowired private UserRepository userRepository;
    @Autowired private DepartmentRepository departmentRepository;
    @Autowired private AttendanceRepository attendanceRepository;
    @Autowired private PayrollRepository payrollRepository;
    @Autowired private PerformanceReviewRepository reviewRepository;
    @Autowired private LeaveRequestRepository leaveRepository;

    @GetMapping
    public String reportDashboard(Model model) {
        LocalDate today = LocalDate.now();
        List<User> activeUsers = userRepository.findByStatus(UserStatus.ACTIVE);
        long totalEmployees = activeUsers.size();

        // ===== CHART 1: Chấm công 12 tháng gần nhất (Theo tháng) =====
        List<String> monthLabels = new ArrayList<>();
        List<Integer> monthPresent = new ArrayList<>();
        List<Integer> monthLate = new ArrayList<>();
        List<Integer> monthAbsent = new ArrayList<>();

        for (int i = 5; i >= 0; i--) {
            LocalDate monthStart = today.minusMonths(i).withDayOfMonth(1);
            LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());
            monthLabels.add(monthStart.format(DateTimeFormatter.ofPattern("MM/yyyy")));

            List<Attendance> monthAtt = attendanceRepository.findByAttendanceDateBetween(monthStart, monthEnd);
            long present = monthAtt.stream().filter(a -> a.getStatus() == AttendanceStatus.PRESENT).count();
            long late = monthAtt.stream().filter(a -> a.getStatus() == AttendanceStatus.LATE).count();
            // Ước tính số ngày vắng = (nhân viên * ngày làm việc trong tháng) - check in
            long workDays = monthStart.until(monthEnd).getDays() * 5 / 7 + 1;
            long absent = Math.max(0, (totalEmployees * workDays) - monthAtt.size());

            monthPresent.add((int) present);
            monthLate.add((int) late);
            monthAbsent.add((int) Math.min(absent, totalEmployees * workDays));
        }

        model.addAttribute("monthLabels", monthLabels);
        model.addAttribute("monthPresent", monthPresent);
        model.addAttribute("monthLate", monthLate);
        model.addAttribute("monthAbsent", monthAbsent);

        // ===== CHART 2: Phân bổ lương theo phòng ban =====
        List<Department> depts = departmentRepository.findAll();
        List<String> deptSalaryNames = new ArrayList<>();
        List<Double> deptAvgSalary = new ArrayList<>();
        List<Long> deptHeadcount = new ArrayList<>();

        List<Payroll> allPayrolls = payrollRepository.findAll();
        int currentMonth = today.getMonthValue();
        int currentYear = today.getYear();

        for (Department d : depts) {
            List<User> deptUsers = activeUsers.stream()
                    .filter(u -> u.getDepartment() != null && u.getDepartment().getId().equals(d.getId()))
                    .collect(Collectors.toList());
            if (deptUsers.isEmpty()) continue;

            // Lấy bảng lương tháng hiện tại của phòng ban
            double avgSalary = deptUsers.stream()
                    .flatMap(u -> allPayrolls.stream()
                            .filter(p -> p.getUser() != null && p.getUser().getId().equals(u.getId())
                                    && p.getMonth() == currentMonth && p.getYear() == currentYear))
                    .mapToDouble(p -> {
                        if (p.getBaseSalary() != null) {
                            BigDecimal net = p.getBaseSalary()
                                    .add(p.getBonus() != null ? p.getBonus() : BigDecimal.ZERO)
                                    .subtract(p.getDeductions() != null ? p.getDeductions() : BigDecimal.ZERO);
                            return net.doubleValue();
                        }
                        return 0.0;
                    })
                    .average().orElse(0.0);

            deptSalaryNames.add(d.getDepartmentName());
            deptAvgSalary.add(avgSalary / 1_000_000); // đơn vị triệu
            deptHeadcount.add((long) deptUsers.size());
        }

        model.addAttribute("deptSalaryNames", deptSalaryNames);
        model.addAttribute("deptAvgSalary", deptAvgSalary);
        model.addAttribute("deptHeadcount", deptHeadcount);

        // ===== CHART 3: Phân phối điểm KPI (Histogram) =====
        List<PerformanceReview> allReviews = reviewRepository.findAll();
        int[] kpiDistribution = new int[5]; // [Yếu, TB, Khá, Tốt, Xuất sắc]
        for (PerformanceReview r : allReviews) {
            if (r.getOverallScore() == null) continue;
            double score = r.getOverallScore().doubleValue();
            if (score < 50) kpiDistribution[0]++;
            else if (score < 60) kpiDistribution[1]++;
            else if (score < 75) kpiDistribution[2]++;
            else if (score < 90) kpiDistribution[3]++;
            else kpiDistribution[4]++;
        }
        model.addAttribute("kpiDistribution", Arrays.asList(
                kpiDistribution[0], kpiDistribution[1], kpiDistribution[2], kpiDistribution[3], kpiDistribution[4]));

        // ===== CHART 4: Task completion rate =====
        // (dữ liệu đơn giản - sẽ hiển thị dạng doughnut)
        // leave request này tháng
        long leaveThisMonth = leaveRepository.findAll().stream()
                .filter(l -> l.getStartDate() != null
                        && l.getStartDate().getMonthValue() == currentMonth
                        && l.getStartDate().getYear() == currentYear)
                .count();

        // ===== SUMMARY KPIs =====
        double avgOverallScore = allReviews.stream()
                .filter(r -> r.getOverallScore() != null)
                .mapToDouble(r -> r.getOverallScore().doubleValue())
                .average().orElse(0.0);

        BigDecimal totalPayrollThisMonth = allPayrolls.stream()
                .filter(p -> p.getMonth() == currentMonth && p.getYear() == currentYear)
                .map(p -> {
                    if (p.getBaseSalary() == null) return BigDecimal.ZERO;
                    return p.getBaseSalary()
                            .add(p.getBonus() != null ? p.getBonus() : BigDecimal.ZERO)
                            .subtract(p.getDeductions() != null ? p.getDeductions() : BigDecimal.ZERO);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Top performers
        List<PerformanceReview> topPerformers = allReviews.stream()
                .filter(r -> r.getOverallScore() != null)
                .sorted((a, b) -> b.getOverallScore().compareTo(a.getOverallScore()))
                .limit(5)
                .collect(Collectors.toList());

        model.addAttribute("totalEmployees", totalEmployees);
        model.addAttribute("avgKpiScore", String.format("%.1f", avgOverallScore));
        model.addAttribute("totalPayrollThisMonth", totalPayrollThisMonth);
        model.addAttribute("leaveThisMonth", leaveThisMonth);
        model.addAttribute("topPerformers", topPerformers);
        model.addAttribute("today", today.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        model.addAttribute("currentMonthYear", today.format(DateTimeFormatter.ofPattern("MM/yyyy")));

        return "admin/report-dashboard";
    }
}
