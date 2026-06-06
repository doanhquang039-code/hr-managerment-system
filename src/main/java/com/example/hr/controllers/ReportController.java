package com.example.hr.controllers;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.hr.enums.AttendanceStatus;
import com.example.hr.enums.UserStatus;
import com.example.hr.attendance.entity.Attendance;
import com.example.hr.models.Department;
import com.example.hr.payroll.entity.Payroll;
import com.example.hr.models.PerformanceReview;
import com.example.hr.models.User;
import com.example.hr.attendance.repository.AttendanceRepository;
import com.example.hr.repository.DepartmentRepository;
import com.example.hr.leave.repository.LeaveRequestRepository;
import com.example.hr.payroll.repository.PayrollRepository;
import com.example.hr.repository.PerformanceReviewRepository;
import com.example.hr.user.repository.UserRepository;

@Controller
@RequestMapping("/admin/reports")
public class ReportController {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private PayrollRepository payrollRepository;

    @Autowired
    private PerformanceReviewRepository reviewRepository;

    @Autowired
    private LeaveRequestRepository leaveRepository;

    @GetMapping
    public String reportDashboard(Model model) {
        LocalDate today = LocalDate.now();
        YearMonth currentPeriod = YearMonth.from(today);
        int currentMonth = currentPeriod.getMonthValue();
        int currentYear = currentPeriod.getYear();

        List<User> activeUsers = userRepository.findByStatus(UserStatus.ACTIVE);
        long totalEmployees = activeUsers.size();
        List<Attendance> allAttendance = attendanceRepository.findAll();
        List<Payroll> allPayrolls = payrollRepository.findAll();
        List<PerformanceReview> allReviews = reviewRepository.findAll();

        List<String> monthLabels = new ArrayList<>();
        List<Integer> monthPresent = new ArrayList<>();
        List<Integer> monthLate = new ArrayList<>();
        List<Integer> monthAbsent = new ArrayList<>();

        for (int i = 5; i >= 0; i--) {
            YearMonth period = currentPeriod.minusMonths(i);
            LocalDate monthStart = period.atDay(1);
            LocalDate monthEnd = period.atEndOfMonth();

            List<Attendance> monthAttendance = allAttendance.stream()
                    .filter(attendance -> isWithinMonth(attendance.getAttendanceDate(), period))
                    .toList();

            long present = monthAttendance.stream()
                    .filter(attendance -> attendance.getStatus() == AttendanceStatus.PRESENT)
                    .count();
            long late = monthAttendance.stream()
                    .filter(attendance -> attendance.getStatus() == AttendanceStatus.LATE)
                    .count();
            long expectedAttendances = totalEmployees * countWeekdays(monthStart, monthEnd);
            long absent = Math.max(0, expectedAttendances - present - late);

            monthLabels.add(period.format(DateTimeFormatter.ofPattern("MM/yyyy")));
            monthPresent.add((int) present);
            monthLate.add((int) late);
            monthAbsent.add((int) Math.min(absent, expectedAttendances));
        }

        model.addAttribute("monthLabels", monthLabels);
        model.addAttribute("monthPresent", monthPresent);
        model.addAttribute("monthLate", monthLate);
        model.addAttribute("monthAbsent", monthAbsent);

        Map<Integer, Payroll> payrollByUserThisMonth = allPayrolls.stream()
                .filter(payroll -> payroll.getUser() != null && payroll.getUser().getId() != null)
                .filter(payroll -> payroll.getMonth() != null && payroll.getYear() != null)
                .filter(payroll -> payroll.getMonth() == currentMonth && payroll.getYear() == currentYear)
                .collect(Collectors.toMap(
                        payroll -> payroll.getUser().getId(),
                        payroll -> payroll,
                        this::pickPreferredPayroll));

        List<String> deptSalaryNames = new ArrayList<>();
        List<Double> deptAvgSalary = new ArrayList<>();
        List<Long> deptHeadcount = new ArrayList<>();

        for (Department department : departmentRepository.findAll()) {
            List<User> deptUsers = activeUsers.stream()
                    .filter(user -> user.getDepartment() != null && Objects.equals(user.getDepartment().getId(), department.getId()))
                    .toList();
            if (deptUsers.isEmpty()) {
                continue;
            }

            double avgSalary = deptUsers.stream()
                    .map(User::getId)
                    .map(payrollByUserThisMonth::get)
                    .filter(Objects::nonNull)
                    .map(this::calculateNetSalary)
                    .mapToDouble(BigDecimal::doubleValue)
                    .average()
                    .orElse(0.0);

            deptSalaryNames.add(department.getDepartmentName());
            deptAvgSalary.add(avgSalary / 1_000_000);
            deptHeadcount.add((long) deptUsers.size());
        }

        model.addAttribute("deptSalaryNames", deptSalaryNames);
        model.addAttribute("deptAvgSalary", deptAvgSalary);
        model.addAttribute("deptHeadcount", deptHeadcount);

        int[] kpiDistribution = new int[5];
        for (PerformanceReview review : allReviews) {
            if (review.getOverallScore() == null) {
                continue;
            }
            double score = review.getOverallScore().doubleValue();
            if (score < 50) {
                kpiDistribution[0]++;
            } else if (score < 60) {
                kpiDistribution[1]++;
            } else if (score < 75) {
                kpiDistribution[2]++;
            } else if (score < 90) {
                kpiDistribution[3]++;
            } else {
                kpiDistribution[4]++;
            }
        }
        model.addAttribute("kpiDistribution",
                Arrays.asList(kpiDistribution[0], kpiDistribution[1], kpiDistribution[2], kpiDistribution[3], kpiDistribution[4]));

        long leaveThisMonth = leaveRepository.findAll().stream()
                .filter(leave -> leave.getStartDate() != null)
                .filter(leave -> leave.getStartDate().getMonthValue() == currentMonth)
                .filter(leave -> leave.getStartDate().getYear() == currentYear)
                .count();

        double avgOverallScore = allReviews.stream()
                .filter(review -> review.getOverallScore() != null)
                .mapToDouble(review -> review.getOverallScore().doubleValue())
                .average()
                .orElse(0.0);

        BigDecimal totalPayrollThisMonth = payrollByUserThisMonth.values().stream()
                .map(this::calculateNetSalary)
                .reduce(ZERO, BigDecimal::add);

        List<PerformanceReview> topPerformers = allReviews.stream()
                .filter(review -> review.getUser() != null && review.getUser().getId() != null)
                .filter(review -> review.getOverallScore() != null)
                .collect(Collectors.toMap(
                        review -> review.getUser().getId(),
                        review -> review,
                        this::pickBetterReview))
                .values().stream()
                .sorted(Comparator.comparing(PerformanceReview::getOverallScore, Comparator.reverseOrder())
                        .thenComparing(PerformanceReview::getReviewDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(5)
                .toList();

        model.addAttribute("totalEmployees", totalEmployees);
        model.addAttribute("avgKpiScore", String.format("%.1f", avgOverallScore));
        model.addAttribute("totalPayrollThisMonth", totalPayrollThisMonth);
        model.addAttribute("leaveThisMonth", leaveThisMonth);
        model.addAttribute("topPerformers", topPerformers);
        model.addAttribute("today", today.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        model.addAttribute("currentMonthYear", today.format(DateTimeFormatter.ofPattern("MM/yyyy")));

        return "admin/report-dashboard";
    }

    private boolean isWithinMonth(LocalDate date, YearMonth period) {
        return date != null && YearMonth.from(date).equals(period);
    }

    private long countWeekdays(LocalDate start, LocalDate end) {
        long total = 0;
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            DayOfWeek dayOfWeek = date.getDayOfWeek();
            if (dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY) {
                total++;
            }
        }
        return total;
    }

    private BigDecimal calculateNetSalary(Payroll payroll) {
        if (payroll == null) {
            return ZERO;
        }
        if (payroll.getNetSalary() != null) {
            return payroll.getNetSalary();
        }

        BigDecimal baseSalary = payroll.getBaseSalary() != null ? payroll.getBaseSalary() : ZERO;
        BigDecimal bonus = payroll.getBonus() != null ? payroll.getBonus() : ZERO;
        BigDecimal deductions = payroll.getDeductions() != null ? payroll.getDeductions() : ZERO;
        return baseSalary.add(bonus).subtract(deductions);
    }

    private Payroll pickPreferredPayroll(Payroll left, Payroll right) {
        if (left.getPaymentStatus() == right.getPaymentStatus()) {
            return higherId(left, right);
        }
        if (left.getPaymentStatus() == null) {
            return right;
        }
        if (right.getPaymentStatus() == null) {
            return left;
        }
        if (left.getPaymentStatus().ordinal() != right.getPaymentStatus().ordinal()) {
            return left.getPaymentStatus().ordinal() > right.getPaymentStatus().ordinal() ? left : right;
        }
        return higherId(left, right);
    }

    private Payroll higherId(Payroll left, Payroll right) {
        Integer leftId = left.getId();
        Integer rightId = right.getId();
        if (leftId == null) {
            return right;
        }
        if (rightId == null) {
            return left;
        }
        return leftId >= rightId ? left : right;
    }

    private PerformanceReview pickBetterReview(PerformanceReview left, PerformanceReview right) {
        int scoreCompare = left.getOverallScore().compareTo(right.getOverallScore());
        if (scoreCompare != 0) {
            return scoreCompare > 0 ? left : right;
        }

        LocalDate leftDate = left.getReviewDate();
        LocalDate rightDate = right.getReviewDate();
        if (leftDate == null) {
            return right;
        }
        if (rightDate == null) {
            return left;
        }
        return leftDate.isAfter(rightDate) ? left : right;
    }
}
