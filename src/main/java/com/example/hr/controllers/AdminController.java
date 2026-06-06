package com.example.hr.controllers;



import com.example.hr.payment.entity.Payment;
import com.example.hr.payroll.entity.Payroll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.hr.enums.AttendanceStatus;
import com.example.hr.enums.LeaveStatus;
import com.example.hr.enums.UserStatus;
import com.example.hr.attendance.entity.Attendance;
import com.example.hr.models.Department;
import com.example.hr.models.User;
import com.example.hr.attendance.repository.AttendanceRepository;
import com.example.hr.repository.DepartmentRepository;
import com.example.hr.repository.JobPositionRepository;
import com.example.hr.leave.repository.LeaveRequestRepository;
import com.example.hr.payment.repository.PaymentRepository;
import com.example.hr.payroll.repository.PayrollRepository;
import com.example.hr.repository.PerformanceReviewRepository;
import com.example.hr.repository.QuizAttemptRepository;
import com.example.hr.sales.repository.SalesOrderRepository;
import com.example.hr.sales.repository.SalesProductRepository;
import com.example.hr.repository.TaskRepository;
import com.example.hr.user.repository.UserRepository;
import com.example.hr.payment.service.PaymentService;
import com.example.hr.service.PasswordResetService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.math.BigDecimal;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired private UserRepository userRepository;
    @Autowired private DepartmentRepository departmentRepository;
    @Autowired private JobPositionRepository jobPositionRepository;
    @Autowired private LeaveRequestRepository leaveRepository;
    @Autowired private TaskRepository taskRepository;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private PaymentService paymentService;
    @Autowired private AttendanceRepository attendanceRepository;
    @Autowired private PayrollRepository payrollRepository;
    @Autowired private PerformanceReviewRepository reviewRepository;
    @Autowired private PasswordResetService passwordResetService;
    @Autowired private SalesProductRepository salesProductRepository;
    @Autowired private SalesOrderRepository salesOrderRepository;
    @Autowired private QuizAttemptRepository quizAttemptRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // === BASIC STATS ===
        long totalUsers       = userRepository.findByStatus(UserStatus.ACTIVE).size();
        long totalDepartments = departmentRepository.count();
        long totalPositions   = jobPositionRepository.findByActiveTrue().size();
        long pendingLeaves    = leaveRepository.findAllWithUser(null).stream()
                                    .filter(l -> l.getStatus() == LeaveStatus.PENDING).count();
        long totalTasks       = taskRepository.count();
        long totalReviews     = reviewRepository.count();

        model.addAttribute("totalUsers",       totalUsers);
        model.addAttribute("totalDepartments", totalDepartments);
        model.addAttribute("totalPositions",   totalPositions);
        model.addAttribute("pendingLeaves",    pendingLeaves);
        model.addAttribute("totalTasks",       totalTasks);
        model.addAttribute("totalReviews",     totalReviews);
        model.addAttribute("pendingResetRequests", passwordResetService.countPendingRequests());
        model.addAttribute("today", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        // === CHART 1: Chấm công 7 ngày gần nhất ===
        List<String> attLabels = new ArrayList<>();
        List<Integer> attPresent = new ArrayList<>();
        List<Integer> attLate    = new ArrayList<>();
        List<Integer> attAbsent  = new ArrayList<>();
        LocalDate today = LocalDate.now();
        List<User> allActiveUsers = userRepository.findByStatus(UserStatus.ACTIVE);
        long totalEmployees = allActiveUsers.size();

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            attLabels.add(date.format(DateTimeFormatter.ofPattern("dd/MM")));
            List<Attendance> dayAtt = attendanceRepository
                    .findByAttendanceDateBetween(date, date, org.springframework.data.domain.Pageable.unpaged()).getContent();
            long present = dayAtt.stream().filter(a -> a.getStatus() == AttendanceStatus.PRESENT).count();
            long late    = dayAtt.stream().filter(a -> a.getStatus() == AttendanceStatus.LATE).count();
            long absent  = Math.max(0, totalEmployees - dayAtt.size());
            attPresent.add((int) present);
            attLate.add((int) late);
            attAbsent.add((int) absent);
        }
        model.addAttribute("attLabels",  attLabels);
        model.addAttribute("attPresent", attPresent);
        model.addAttribute("attLate",    attLate);
        model.addAttribute("attAbsent",  attAbsent);

        // === CHART 2: Nhân viên theo phòng ban (Pie/Doughnut) ===
        List<Department> depts = departmentRepository.findAll();
        List<String> deptNames  = new ArrayList<>();
        List<Long> deptCounts   = new ArrayList<>();
        for (Department d : depts) {
            long cnt = allActiveUsers.stream()
                    .filter(u -> u.getDepartment() != null && u.getDepartment().getId().equals(d.getId()))
                    .count();
            if (cnt > 0) {
                deptNames.add(d.getDepartmentName());
                deptCounts.add(cnt);
            }
        }
        model.addAttribute("deptNames",  deptNames);
        model.addAttribute("deptCounts", deptCounts);
        model.addAttribute("departments", depts);
        model.addAttribute("positions", jobPositionRepository.findByActiveTrue());

        // === CHART 3: Leave statistics ===
        var allLeaves = leaveRepository.findAllWithUser(null);
        long approvedLeaves = allLeaves.stream().filter(l -> l.getStatus() == LeaveStatus.APPROVED).count();
        long rejectedLeaves = allLeaves.stream().filter(l -> l.getStatus() == LeaveStatus.REJECTED).count();
        model.addAttribute("leaveApproved", approvedLeaves);
        model.addAttribute("leaveRejected", rejectedLeaves);
        model.addAttribute("leavePending",  pendingLeaves);

        // === RECENT ACTIVITIES ===
        // Recent 5 leave requests (pending first)
        var recentLeaves = leaveRepository.findAllWithUser(null).stream()
                .filter(l -> l.getStatus() == LeaveStatus.PENDING)
                .limit(5)
                .collect(Collectors.toList());
        model.addAttribute("recentLeaves", recentLeaves);

        // Recent 5 users
        var recentUsers = allActiveUsers.stream().limit(5).collect(Collectors.toList());
        model.addAttribute("recentUsers", recentUsers);

        // === OPERATIONS & GROWTH ===
        var allProducts = salesProductRepository.findAll();
        long productApproved = allProducts.stream().filter(p -> statusIs(p.getApprovalStatus(), "APPROVED")).count();
        long productPending = allProducts.stream().filter(p -> statusIs(p.getApprovalStatus(), "PENDING")).count();
        long productRejected = allProducts.stream().filter(p -> statusIs(p.getApprovalStatus(), "REJECTED")).count();
        long activeProducts = allProducts.stream().filter(p -> Boolean.TRUE.equals(p.getActive())).count();
        model.addAttribute("productTotal", allProducts.size());
        model.addAttribute("activeProducts", activeProducts);
        model.addAttribute("productApproved", productApproved);
        model.addAttribute("productPending", productPending);
        model.addAttribute("productRejected", productRejected);

        var salesOrders = salesOrderRepository.findAllByOrderByCreatedAtDesc();
        BigDecimal salesRevenue = salesOrders.stream()
                .map(o -> o.getTotalAmount() == null ? BigDecimal.ZERO : o.getTotalAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long paidOrders = salesOrders.stream()
                .filter(o -> statusIs(o.getStatus(), "PAID") || statusIs(o.getStatus(), "SHIPPED") || statusIs(o.getStatus(), "COMPLETED"))
                .count();
        long pendingOrders = salesOrders.stream().filter(o -> statusIs(o.getStatus(), "PENDING")).count();
        long cancelledOrders = salesOrders.stream().filter(o -> statusIs(o.getStatus(), "CANCELLED")).count();
        model.addAttribute("salesRevenue", salesRevenue);
        model.addAttribute("salesOrderCount", salesOrders.size());
        model.addAttribute("paidOrders", paidOrders);
        model.addAttribute("pendingOrders", pendingOrders);
        model.addAttribute("cancelledOrders", cancelledOrders);

        var completedMinigameAttempts = quizAttemptRepository.findAll().stream()
                .filter(a -> a.getCompletedAt() != null && a.getUser() != null)
                .collect(Collectors.toList());
        var topMinigameAttempts = completedMinigameAttempts.stream()
                .sorted((a, b) -> Integer.compare(b.getScore() == null ? 0 : b.getScore(), a.getScore() == null ? 0 : a.getScore()))
                .limit(5)
                .collect(Collectors.toList());
        model.addAttribute("topMinigameAttempts", topMinigameAttempts);
        model.addAttribute("minigameAttemptCount", completedMinigameAttempts.size());

        long activeRate = totalUsers == 0 ? 0 : Math.round((double) totalUsers * 100 / Math.max(1, userRepository.count()));
        long productApprovalRate = allProducts.isEmpty() ? 0 : Math.round((double) productApproved * 100 / allProducts.size());
        long leaveResolutionRate = allLeaves.isEmpty() ? 0 : Math.round((double) (approvedLeaves + rejectedLeaves) * 100 / allLeaves.size());
        model.addAttribute("activeRate", activeRate);
        model.addAttribute("productApprovalRate", productApprovalRate);
        model.addAttribute("leaveResolutionRate", leaveResolutionRate);
        model.addAttribute("pendingSalesProducts", allProducts.stream()
                .filter(p -> "PENDING".equals(p.getApprovalStatus()))
                .limit(5)
                .collect(Collectors.toList()));
        model.addAttribute("recentSalesOrders", salesOrders.stream()
                .limit(5)
                .collect(Collectors.toList()));
        model.addAttribute("lowStockProducts", allProducts.stream()
                .filter(p -> Boolean.TRUE.equals(p.getActive()))
                .filter(p -> p.getStockQuantity() != null && p.getStockQuantity() <= 5)
                .limit(5)
                .collect(Collectors.toList()));

        return "admin/dashboard";
    }

    private boolean statusIs(String actual, String expected) {
        return actual != null && actual.trim().equalsIgnoreCase(expected);
    }
}
