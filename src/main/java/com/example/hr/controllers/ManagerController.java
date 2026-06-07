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
import com.example.hr.service.EmailFacade;
import com.example.hr.service.NotificationService;
import com.example.hr.service.NewOvertimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    // ==================== DASHBOARD ====================

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
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
            return "manager/dashboard-simple";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "LÃ¡Â»â€”i tÃ¡ÂºÂ£i dashboard: " + e.getMessage());
            model.addAttribute("errorDetails", e.getClass().getSimpleName());
            return "error/500";
        }
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
    public String budgetReports(Model model) {
        try {
            // TODO: Implement budget reports
            model.addAttribute("message", "Budget reports feature coming soon");
            
            return "manager/reports/budget";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "LÃ¡Â»â€”i tÃ¡ÂºÂ£i budget reports: " + e.getMessage());
            return "error/500";
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


