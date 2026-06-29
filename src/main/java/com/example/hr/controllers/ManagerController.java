package com.example.hr.controllers;







import com.example.hr.department.entity.Department;
import com.example.hr.user.repository.UserRepository;
import com.example.hr.leave.entity.LeaveRequest;
import com.example.hr.leave.repository.LeaveRequestRepository;
import com.example.hr.attendance.entity.Attendance;
import com.example.hr.attendance.repository.AttendanceRepository;
import com.example.hr.enums.*;
import com.example.hr.models.*;
import com.example.hr.repository.*;
import com.example.hr.task.entity.Task;
import com.example.hr.task.repository.TaskAssignmentRepository;
import com.example.hr.task.repository.TaskRepository;
import com.example.hr.service.AuthUserHelper;
import com.example.hr.service.BulkOperationService;
import com.example.hr.service.EmailFacade;
import com.example.hr.service.NotificationService;
import com.example.hr.service.NewOvertimeService;
import com.example.hr.service.TeamBudgetService;
import com.example.hr.sales.entity.SalesOrder;
import com.example.hr.sales.entity.SalesProduct;
import com.example.hr.sales.service.SalesService;
import com.example.hr.payment.dto.CartDto;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.io.ByteArrayOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.example.hr.recruitment.repository.CandidateRepository;
import com.example.hr.recruitment.repository.JobPostingRepository;
import com.example.hr.recruitment.entity.JobPosting;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/manager")
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
public class ManagerController {

    @Autowired private LeaveRequestRepository leaveRepository;
    @Autowired private TaskRepository taskRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private AttendanceRepository attendanceRepository;
    @Autowired private TaskAssignmentRepository taskAssignmentRepository;
    @Autowired private PerformanceReviewRepository reviewRepository;
    @Autowired private OvertimeRequestRepository overtimeRepository;
    @Autowired private NewOvertimeService overtimeService;
    @Autowired private NotificationService notificationService;
    @Autowired private EmailFacade emailFacade;
    @Autowired private AuthUserHelper authUserHelper;
    @Autowired private MeetingRepository meetingRepository;
    @Autowired private TeamBudgetService teamBudgetService;
    @Autowired private SalesService salesService;
    @Autowired private BulkOperationService bulkOperationService;
    @Autowired private CandidateRepository candidateRepository;
    @Autowired private JobPostingRepository jobPostingRepository;

    // ==================== DASHBOARD ====================

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        try {
            LocalDate today = LocalDate.now();

            long totalEmployees = userRepository.findByStatus(UserStatus.ACTIVE).size();
            long pendingLeaves  = leaveRepository.countByStatus(LeaveStatus.PENDING);
            long pendingOT      = overtimeRepository.countByStatus("PENDING");

            var allAssignments = taskAssignmentRepository.findAllWithUser();
            long activeTasks    = allAssignments.stream().filter(a -> a.getStatus() == TaskStatus.IN_PROGRESS).count();
            long completedTasks = allAssignments.stream().filter(a -> a.getStatus() == TaskStatus.COMPLETED).count();
            long pendingTasks   = allAssignments.stream().filter(a -> a.getStatus() == TaskStatus.PENDING).count();

            long checkedInToday = attendanceRepository.findByAttendanceDateBetween(today, today, org.springframework.data.domain.Pageable.unpaged()).getContent().size();
            long absentToday    = Math.max(0, totalEmployees - checkedInToday);

            List<String> attLabels   = new ArrayList<>();
            List<Integer> attPresent = new ArrayList<>();
            List<Integer> attLate    = new ArrayList<>();
            List<Integer> attAbsent  = new ArrayList<>();

            for (int i = 6; i >= 0; i--) {
                LocalDate date = today.minusDays(i);
                attLabels.add(date.format(DateTimeFormatter.ofPattern("dd/MM")));
                List<Attendance> dayAtt = attendanceRepository.findByAttendanceDateBetween(date, date, org.springframework.data.domain.Pageable.unpaged()).getContent();
                long present = dayAtt.stream().filter(a -> a.getStatus() == AttendanceStatus.PRESENT).count();
                long late    = dayAtt.stream().filter(a -> a.getStatus() == AttendanceStatus.LATE).count();
                long absent  = Math.max(0, totalEmployees - dayAtt.size());
                attPresent.add((int) present);
                attLate.add((int) late);
                attAbsent.add((int) absent);
            }

            var pendingFirst = leaveRepository.findAllWithUser(null).stream()
                    .sorted((a, b) -> {
                        if (a.getStatus() == LeaveStatus.PENDING && b.getStatus() != LeaveStatus.PENDING) return -1;
                        if (a.getStatus() != LeaveStatus.PENDING && b.getStatus() == LeaveStatus.PENDING) return 1;
                        return 0;
                    }).collect(Collectors.toList());

            List<PerformanceReview> topPerformers = reviewRepository.findAllWithUsers().stream()
                    .filter(r -> r.getOverallScore() != null)
                    .sorted((a, b) -> b.getOverallScore().compareTo(a.getOverallScore()))
                    .limit(5)
                    .collect(Collectors.toList());

            // Load tasks with error handling for invalid enum values
            List<Task> recentTasks = new ArrayList<>();
            try {
                recentTasks = taskRepository.findAll();
            } catch (Exception e) {
                // Log error but continue - tasks with invalid enum will be skipped
                System.err.println("Warning: Some tasks have invalid enum values: " + e.getMessage());
            }

            model.addAttribute("totalEmployees", totalEmployees);
            model.addAttribute("pendingLeaves",  pendingLeaves);
            model.addAttribute("pendingOT",      pendingOT);
            model.addAttribute("activeTasks",    activeTasks);
            model.addAttribute("absentToday",    absentToday);
            model.addAttribute("completedTasks", completedTasks);
            model.addAttribute("pendingTasks",   pendingTasks);
            model.addAttribute("recentLeaves",   pendingFirst);
            model.addAttribute("recentTasks",    recentTasks);
            model.addAttribute("teamMembers",    userRepository.findByStatus(UserStatus.ACTIVE));
            model.addAttribute("topPerformers",  topPerformers);
            model.addAttribute("attLabels",      attLabels);
            model.addAttribute("attPresent",     attPresent);
            model.addAttribute("attLate",        attLate);
            model.addAttribute("attAbsent",      attAbsent);
            model.addAttribute("today", today.format(DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy")));
            model.addAttribute("cartCount", getCartCount(session));

            // === Recruitment stats ===
            List<JobPosting> activePostings = jobPostingRepository.findAll();
            List<String> recruitPositions = new ArrayList<>();
            List<Long> recruitApplicants = new ArrayList<>();
            List<JobPosting> sortedPostings = activePostings.stream()
                    .sorted((a, b) -> Integer.compare(
                            candidateRepository.findByJobPostingOrderByAppliedAtDesc(b).size(),
                            candidateRepository.findByJobPostingOrderByAppliedAtDesc(a).size()
                    ))
                    .limit(8)
                    .collect(Collectors.toList());
            for (JobPosting jp : sortedPostings) {
                long count = candidateRepository.findByJobPostingOrderByAppliedAtDesc(jp).size();
                recruitPositions.add(jp.getTitle());
                recruitApplicants.add(count);
            }
            if (recruitPositions.isEmpty()) {
                recruitPositions = List.of("Java Dev", "Frontend", "Marketing", "HR", "Sales", "DevOps");
                recruitApplicants = List.of(0L, 0L, 0L, 0L, 0L, 0L);
            }
            model.addAttribute("recruitPositions", recruitPositions);
            model.addAttribute("recruitApplicants", recruitApplicants);

            return "manager/dashboard-simple";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi tải dashboard: " + e.getMessage());
            model.addAttribute("errorDetails", e.getClass().getSimpleName());
            return "error/500";
        }
    }

    private int getCartCount(HttpSession session) {
        CartDto cart = (CartDto) session.getAttribute("salesCart");
        return cart == null ? 0 : cart.getTotalItems();
    }

    // ==================== TEAM MANAGEMENT ====================

    @GetMapping("/team")
    public String team(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) Integer deptId,
                       Model model) {
        List<User> members = userRepository.findByStatus(UserStatus.ACTIVE);
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.toLowerCase();
            members = members.stream()
                    .filter(u -> u.getFullName().toLowerCase().contains(kw)
                            || (u.getEmail() != null && u.getEmail().toLowerCase().contains(kw)))
                    .collect(Collectors.toList());
        }
        if (deptId != null) {
            members = members.stream()
                    .filter(u -> u.getDepartment() != null && u.getDepartment().getId().equals(deptId))
                    .collect(Collectors.toList());
        }
        model.addAttribute("members", members);
        // Use LinkedHashMap deduplication by ID to avoid StackOverflow from
        // Lombok @Data hashCode() on bidirectional Department <-> User entities
        java.util.Map<Integer, com.example.hr.department.entity.Department> deptMap = new java.util.LinkedHashMap<>();
        for (User u : members) {
            if (u.getDepartment() != null) {
                deptMap.putIfAbsent(u.getDepartment().getId(), u.getDepartment());
            }
        }
        model.addAttribute("departments", new java.util.ArrayList<>(deptMap.values()));
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedDeptId", deptId);
        return "manager/team";
    }

    // ==================== OVERTIME APPROVAL ====================

    @GetMapping("/overtime")
    public String overtimeList(@RequestParam(required = false) String status,
                               @RequestParam(required = false) String keyword,
                               Model model) {
        String selectedStatus = normalizeOvertimeStatus(status);
        List<OvertimeRequest> requests = selectedStatus != null
                ? overtimeRepository.findByStatusOrderByCreatedAtDesc(selectedStatus)
                : overtimeRepository.findAll().stream()
                        .sorted(Comparator.comparing(OvertimeRequest::getCreatedAt,
                                Comparator.nullsLast(Comparator.reverseOrder())))
                        .collect(Collectors.toList());

        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim().toLowerCase();
            requests = requests.stream()
                    .filter(r -> containsIgnoreCase(r.getReason(), kw)
                            || (r.getUser() != null && (
                                    containsIgnoreCase(r.getUser().getFullName(), kw)
                                    || containsIgnoreCase(r.getUser().getEmail(), kw))))
                    .collect(Collectors.toList());
        }
        
        long countPending  = overtimeRepository.countByStatus("PENDING");
        long countApproved = overtimeRepository.countByStatus("APPROVED");
        long countRejected = overtimeRepository.countByStatus("REJECTED");

        model.addAttribute("requests", requests);
        model.addAttribute("selectedStatus", selectedStatus);
        model.addAttribute("keyword", keyword);
        model.addAttribute("countPending",  countPending);
        model.addAttribute("countApproved", countApproved);
        model.addAttribute("countRejected", countRejected);
        return "manager/overtime";
    }

    @GetMapping("/overtime/approve/{id}")
    public String approveOT(@PathVariable Integer id, Authentication auth, RedirectAttributes ra) {
        User approver = authUserHelper.getCurrentUser(auth);
        if (approver == null) {
            ra.addFlashAttribute("error", "Khong tim thay nguoi duyet");
            return "redirect:/manager/overtime";
        }
        try {
            OvertimeRequest request = overtimeService.approveRequest(id, approver);
            ra.addFlashAttribute("success", "Da duyet don OT cua " + request.getUser().getFullName());
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/manager/overtime";
    }

    @PostMapping("/overtime/reject/{id}")
    public String rejectOT(@PathVariable Integer id,
                            @RequestParam String reason,
                            Authentication auth,
                            RedirectAttributes ra) {
        User approver = authUserHelper.getCurrentUser(auth);
        if (approver == null) {
            ra.addFlashAttribute("error", "Khong tim thay nguoi duyet");
            return "redirect:/manager/overtime";
        }
        try {
            OvertimeRequest request = overtimeService.rejectRequest(id, approver, reason);
            ra.addFlashAttribute("success", "Da tu choi don OT cua " + request.getUser().getFullName());
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/manager/overtime";
    }

    // ==================== USER OVERTIME (self-service) ====================

    @GetMapping("/user1/overtime")
    @PreAuthorize("isAuthenticated()")
    public String userOvertimeList(Authentication auth, Model model) {
        User currentUser = authUserHelper.getCurrentUser(auth);
        if (currentUser == null) return "redirect:/login";
        List<OvertimeRequest> myRequests = overtimeRepository.findByUserOrderByCreatedAtDesc(currentUser);
        model.addAttribute("myRequests", myRequests);
        model.addAttribute("currentUser", currentUser);
        return "user1/overtime";
    }

    // ==================== TEAM MEMBERS ====================

    @GetMapping("/team-members")
    public String teamMembers(Model model) {
        List<User> teamMembers = userRepository.findByStatus(UserStatus.ACTIVE);
        long totalMembers = teamMembers.size();
        long activeMembers = teamMembers.stream().filter(u -> u.getStatus() == UserStatus.ACTIVE).count();
        long onLeave = leaveRepository.findAllWithUser(null).stream()
                .filter(l -> l.getStatus() == LeaveStatus.APPROVED 
                        && !l.getStartDate().isAfter(LocalDate.now()) 
                        && !l.getEndDate().isBefore(LocalDate.now()))
                .count();
        
        double avgPerformance = reviewRepository.findAllWithUsers().stream()
                .filter(r -> r.getOverallScore() != null)
                .mapToDouble(PerformanceReview::getOverallScore)
                .average()
                .orElse(0.0);

        model.addAttribute("teamMembers", teamMembers);
        model.addAttribute("totalMembers", totalMembers);
        model.addAttribute("activeMembers", activeMembers);
        model.addAttribute("onLeave", onLeave);
        model.addAttribute("avgPerformance", String.format("%.1f", avgPerformance));
        return "manager/team-members";
    }

    // ==================== LEAVE REQUESTS ====================

    @GetMapping("/leave-requests")
    public String leaveRequests(Model model) {
        List<LeaveRequest> allLeaves = leaveRepository.findAllWithUser(null);
        
        List<LeaveRequest> pendingLeaves = allLeaves.stream()
                .filter(l -> l.getStatus() == LeaveStatus.PENDING)
                .collect(Collectors.toList());
        
        List<LeaveRequest> approvedLeaves = allLeaves.stream()
                .filter(l -> l.getStatus() == LeaveStatus.APPROVED)
                .collect(Collectors.toList());
        
        List<LeaveRequest> rejectedLeaves = allLeaves.stream()
                .filter(l -> l.getStatus() == LeaveStatus.REJECTED)
                .collect(Collectors.toList());

        model.addAttribute("pendingLeaves", pendingLeaves);
        model.addAttribute("approvedLeaves", approvedLeaves);
        model.addAttribute("rejectedLeaves", rejectedLeaves);
        model.addAttribute("allLeaves", allLeaves);
        model.addAttribute("pendingCount", pendingLeaves.size());
        model.addAttribute("approvedCount", approvedLeaves.size());
        model.addAttribute("rejectedCount", rejectedLeaves.size());
        model.addAttribute("totalCount", allLeaves.size());
        return "manager/leave-requests";
    }

    @GetMapping("/approvals")
    public String approvals(Model model, HttpSession session) {
        List<LeaveRequest> pendingLeaves = leaveRepository.findAllWithUser(null).stream()
                .filter(l -> l.getStatus() == LeaveStatus.PENDING)
                .collect(Collectors.toList());
        List<SalesProduct> pendingProducts = salesService.getPendingProducts();
        List<SalesOrder> pendingOrders = salesService.getPendingOrders();

        model.addAttribute("pendingLeaves", pendingLeaves);
        model.addAttribute("pendingProducts", pendingProducts);
        model.addAttribute("pendingOrders", pendingOrders);
        model.addAttribute("pendingLeaveCount", pendingLeaves.size());
        model.addAttribute("pendingProductCount", pendingProducts.size());
        model.addAttribute("pendingOrderCount", pendingOrders.size());
        model.addAttribute("totalApprovalCount", pendingLeaves.size() + pendingProducts.size() + pendingOrders.size());
        model.addAttribute("cartCount", getCartCount(session));
        return "manager/approvals";
    }

    @PostMapping("/approvals/products/{id}/approve")
    public String approveSalesProductFromApprovals(@PathVariable Integer id,
                                                   Authentication auth,
                                                   RedirectAttributes ra) {
        try {
            salesService.approveProduct(id, authUserHelper.getCurrentUser(auth));
            ra.addFlashAttribute("success", "Da duyet san pham");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Khong the duyet san pham: " + e.getMessage());
        }
        return "redirect:/manager/approvals";
    }

    @PostMapping("/approvals/products/{id}/reject")
    public String rejectSalesProductFromApprovals(@PathVariable Integer id,
                                                  Authentication auth,
                                                  RedirectAttributes ra) {
        try {
            salesService.rejectProduct(id, authUserHelper.getCurrentUser(auth));
            ra.addFlashAttribute("success", "Da tu choi san pham");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Khong the tu choi san pham: " + e.getMessage());
        }
        return "redirect:/manager/approvals";
    }

    @PostMapping("/approvals/orders/{id}/approve")
    public String approveSalesOrderFromApprovals(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            salesService.approveOrder(id);
            ra.addFlashAttribute("success", "Da duyet don hang");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Khong the duyet don hang: " + e.getMessage());
        }
        return "redirect:/manager/approvals";
    }

    @PostMapping("/approvals/orders/{id}/reject")
    public String rejectSalesOrderFromApprovals(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            salesService.rejectOrder(id);
            ra.addFlashAttribute("success", "Da tu choi don hang");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Khong the tu choi don hang: " + e.getMessage());
        }
        return "redirect:/manager/approvals";
    }

    @PostMapping("/approvals/leaves/{id}/approve")
    public String approveLeaveFromApprovals(@PathVariable Integer id, RedirectAttributes ra) {
        return processLeaveFromApprovals(id, "APPROVE", null, ra);
    }

    @PostMapping("/approvals/leaves/{id}/reject")
    public String rejectLeaveFromApprovals(@PathVariable Integer id,
                                           @RequestParam(required = false) String rejectionReason,
                                           RedirectAttributes ra) {
        return processLeaveFromApprovals(id, "REJECT", rejectionReason, ra);
    }

    private String processLeaveFromApprovals(Integer id,
                                             String action,
                                             String rejectionReason,
                                             RedirectAttributes ra) {
        try {
            LeaveRequest leave = leaveRepository.findById(id).orElse(null);
            if (leave == null) {
                ra.addFlashAttribute("error", "Khong tim thay don nghi");
                return "redirect:/manager/approvals";
            }
            if ("APPROVE".equals(action)) {
                leave.setStatus(LeaveStatus.APPROVED);
                ra.addFlashAttribute("success", "Da duyet don nghi");
            } else {
                leave.setStatus(LeaveStatus.REJECTED);
                ra.addFlashAttribute("success", "Da tu choi don nghi");
            }
            leaveRepository.save(leave);
            try {
                notificationService.createNotification(
                        leave.getUser(),
                        "Don nghi cua ban da duoc xu ly: " + ("APPROVE".equals(action) ? "APPROVED" : "REJECTED"),
                        NotificationType.LEAVE_REQUEST,
                        "/user/leaves"
                );
            } catch (Exception e) {
                System.err.println("Failed to send leave approval notification: " + e.getMessage());
            }
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Khong the xu ly don nghi: " + e.getMessage());
        }
        return "redirect:/manager/approvals";
    }

    @PostMapping("/leave-approve/{id}")
    public String approveLeave(@PathVariable Integer id, 
                               @RequestParam String action,
                               @RequestParam(required = false) String rejectionReason,
                               RedirectAttributes ra) {
        try {
            LeaveRequest leave = leaveRepository.findById(id).orElse(null);
            if (leave == null) {
                ra.addFlashAttribute("error", "Leave request not found");
                return "redirect:/manager/leave-requests";
            }

            if ("APPROVE".equals(action)) {
                leave.setStatus(LeaveStatus.APPROVED);
                ra.addFlashAttribute("success", "Leave request approved successfully");
            } else if ("REJECT".equals(action)) {
                leave.setStatus(LeaveStatus.REJECTED);
                ra.addFlashAttribute("success", "Leave request rejected");
            }
            
            leaveRepository.save(leave);
            
            // Send notification
            try {
                notificationService.createNotification(
                    leave.getUser(),
                    "Your leave request has been " + action.toLowerCase(),
                    NotificationType.LEAVE_REQUEST,
                    "/user/leaves"
                );
            } catch (Exception e) {
                System.err.println("Failed to send notification: " + e.getMessage());
            }
            
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error processing leave request: " + e.getMessage());
        }
        return "redirect:/manager/leave-requests";
    }

    // ==================== GOALS MANAGEMENT ====================
    // Note: Goals management is handled by TeamGoalController at /manager/goals
    // Removed duplicate routes to avoid ambiguous mapping errors

    // ==================== MEETINGS MANAGEMENT ====================
    // Note: Meetings management is handled by ManagerMeetingController at /manager/meetings
    // Removed duplicate routes to avoid ambiguous mapping errors

    // ==================== ANALYTICS ====================
    // Note: Analytics is handled by ManagerDashboardController at /manager/analytics
    // Removed duplicate route to avoid ambiguous mapping errors

    // ==================== ATTENDANCE ====================

    @GetMapping("/attendance")
    public String attendance(@RequestParam(required = false) String date, Model model) {
        try {
            LocalDate selectedDate = (date != null && !date.isBlank()) 
                    ? LocalDate.parse(date) 
                    : LocalDate.now();
            
            List<Attendance> attendances = attendanceRepository.findByAttendanceDateBetween(
                    selectedDate, 
                    selectedDate,
                    org.springframework.data.domain.Pageable.unpaged()
            ).getContent();
            
            List<User> allMembers = userRepository.findByStatus(UserStatus.ACTIVE);
            long presentCount = attendances.stream()
                    .filter(a -> a.getStatus() == AttendanceStatus.PRESENT)
                    .count();
            long lateCount = attendances.stream()
                    .filter(a -> a.getStatus() == AttendanceStatus.LATE)
                    .count();
            long absentCount = allMembers.size() - attendances.size();
            
            model.addAttribute("attendances", attendances);
            model.addAttribute("selectedDate", selectedDate);
            model.addAttribute("presentCount", presentCount);
            model.addAttribute("lateCount", lateCount);
            model.addAttribute("absentCount", absentCount);
            model.addAttribute("totalMembers", allMembers.size());
            
            return "manager/attendance";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "LÃ¡Â»â€”i tÃ¡ÂºÂ£i attendance: " + e.getMessage());
            return "error/500";
        }
    }

    // ==================== PERFORMANCE ====================

    @GetMapping("/performance")
    public String performance(Model model) {
        try {
            List<PerformanceReview> reviews = reviewRepository.findAllWithUsers();
            
            List<PerformanceReview> pendingReviews = reviews.stream()
                    .filter(r -> r.getStatus() == com.example.hr.enums.ReviewStatus.DRAFT 
                            || r.getStatus() == com.example.hr.enums.ReviewStatus.SUBMITTED)
                    .collect(Collectors.toList());
            
            List<PerformanceReview> completedReviews = reviews.stream()
                    .filter(r -> r.getStatus() == com.example.hr.enums.ReviewStatus.COMPLETED)
                    .collect(Collectors.toList());
            
            double avgScore = reviews.stream()
                    .filter(r -> r.getOverallScore() != null)
                    .mapToDouble(PerformanceReview::getOverallScore)
                    .average()
                    .orElse(0.0);
            
            model.addAttribute("reviews", reviews);
            model.addAttribute("pendingReviews", pendingReviews);
            model.addAttribute("completedReviews", completedReviews);
            model.addAttribute("avgScore", String.format("%.1f", avgScore));
            model.addAttribute("totalReviews", reviews.size());
            model.addAttribute("pendingCount", pendingReviews.size());
            model.addAttribute("completedCount", completedReviews.size());
            
            return "manager/performance";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "LÃ¡Â»â€”i tÃ¡ÂºÂ£i performance: " + e.getMessage());
            return "error/500";
        }
    }

    // ==================== BUDGET ====================
    // Note: Budget management is handled by TeamBudgetController at /manager/budget
    // Removed duplicate route to avoid ambiguous mapping errors

    // ==================== REPORTS ====================

    @GetMapping("/reports/team")
    public String teamReports(Model model) {
        try {
            LocalDate today = LocalDate.now();
            LocalDate startOfMonth = today.withDayOfMonth(1);
            
            List<User> teamMembers = userRepository.findByStatus(UserStatus.ACTIVE);
            List<Attendance> monthAttendance = attendanceRepository.findByAttendanceDateBetween(
                    startOfMonth, 
                    today,
                    org.springframework.data.domain.Pageable.unpaged()
            ).getContent();
            List<PerformanceReview> reviews = reviewRepository.findAllWithUsers();
            
            model.addAttribute("teamMembers", teamMembers);
            model.addAttribute("monthAttendance", monthAttendance);
            model.addAttribute("reviews", reviews);
            model.addAttribute("reportMonth", today.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
            
            return "manager/reports/team";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "LÃ¡Â»â€”i tÃ¡ÂºÂ£i team reports: " + e.getMessage());
            return "error/500";
        }
    }

    @GetMapping("/reports/budget")
    public String budgetReports(Authentication auth, Model model) {
        try {
            User manager = authUserHelper.getCurrentUser(auth);
            if (manager == null) return "redirect:/login";

            int currentYear = LocalDate.now().getYear();
            var budgets = manager.getDepartment() == null
                    ? teamBudgetService.getAllBudgets()
                    : teamBudgetService.getBudgetsByDepartment(manager.getDepartment());
            var budgetStats = manager.getDepartment() == null
                    ? teamBudgetService.getBudgetStatistics(currentYear)
                    : teamBudgetService.getBudgetStatistics(manager.getDepartment(), currentYear);

            var overBudget = budgets.stream()
                    .filter(TeamBudget::isOverBudget)
                    .collect(Collectors.toList());
            var activeBudgets = budgets.stream()
                    .filter(b -> "ACTIVE".equalsIgnoreCase(b.getStatus()))
                    .collect(Collectors.toList());

            model.addAttribute("budgets", budgets);
            model.addAttribute("budgetStats", budgetStats);
            model.addAttribute("overBudget", overBudget);
            model.addAttribute("activeBudgets", activeBudgets);
            model.addAttribute("currentYear", currentYear);
            
            return "manager/reports/budget";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi tải budget reports: " + e.getMessage());
            return "error/500";
        }
    }

    // ==================== MY DEPARTMENT ====================

    @GetMapping("/my-department")
    public String myDepartment(Authentication authentication, Model model) {
        User manager = authUserHelper.getCurrentUser(authentication);
        if (manager == null) return "redirect:/login";

        Department department = manager.getDepartment();
        if (department == null) {
            model.addAttribute("errorMessage", "Bạn chưa được gán vào phòng ban nào.");
            return "error/403";
        }

        List<User> members = userRepository.findByDepartment(department);
        // Department managers: users in the same department with role/groupRole MANAGER
        List<User> managers = members.stream()
                .filter(u -> u.getRole() == Role.MANAGER || (u.getGroupRole() != null && "MANAGER".equalsIgnoreCase(u.getGroupRole().getName())))
                .collect(Collectors.toList());
        List<User> hirings = members.stream()
                .filter(u -> u.getRole() == Role.HIRING || (u.getGroupRole() != null && "HIRING".equalsIgnoreCase(u.getGroupRole().getName())))
                .collect(Collectors.toList());

        model.addAttribute("department", department);
        model.addAttribute("members", members);
        model.addAttribute("managers", managers);
        model.addAttribute("hirings", hirings);
        model.addAttribute("user", manager);
        return "manager/my-department";
    }

    @PostMapping("/my-department/import")
    public String importDepartmentUsers(@RequestParam("file") MultipartFile file,
                                        Authentication authentication,
                                        RedirectAttributes redirectAttributes) {
        User manager = authUserHelper.getCurrentUser(authentication);
        if (manager == null) return "redirect:/login";

        Department department = manager.getDepartment();
        if (department == null) {
            redirectAttributes.addFlashAttribute("error", "Bạn chưa được gán vào phòng ban nào.");
            return "redirect:/manager/my-department";
        }

        try {
            var result = bulkOperationService.importUsersToDepartmentFromExcel(file, department);
            redirectAttributes.addFlashAttribute("success",
                    String.format("Đã nhập thành công %d nhân viên. Có %d lỗi.",
                            result.getSuccessCount(), result.getErrorCount()));
            if (result.getErrorCount() > 0) {
                redirectAttributes.addFlashAttribute("errors", result.getErrors().values());
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi nhập file Excel: " + e.getMessage());
        }

        return "redirect:/manager/my-department";
    }

    @GetMapping("/my-department/export")
    public ResponseEntity<ByteArrayResource> exportDepartmentUsers(Authentication authentication) {
        User manager = authUserHelper.getCurrentUser(authentication);
        if (manager == null) return ResponseEntity.status(401).build();

        Department department = manager.getDepartment();
        if (department == null) return ResponseEntity.status(403).build();

        try {
            List<User> members = userRepository.findByDepartment(department);
            ByteArrayOutputStream out = bulkOperationService.exportUsersToExcel(members);
            ByteArrayResource resource = new ByteArrayResource(out.toByteArray());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=department_members.xlsx")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(resource.contentLength())
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private String normalizeOvertimeStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        String normalized = status.trim().toUpperCase();
        return switch (normalized) {
            case "PENDING", "APPROVED", "REJECTED" -> normalized;
            default -> null;
        };
    }

    private boolean containsIgnoreCase(String value, String lowerKeyword) {
        return value != null && value.toLowerCase().contains(lowerKeyword);
    }

}
