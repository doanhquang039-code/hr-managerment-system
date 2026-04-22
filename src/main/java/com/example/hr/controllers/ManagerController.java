package com.example.hr.controllers;

import com.example.hr.enums.*;
import com.example.hr.models.*;
import com.example.hr.repository.*;
import com.example.hr.service.AuthUserHelper;
import com.example.hr.service.EmailFacade;
import com.example.hr.service.NotificationService;
import com.example.hr.service.OvertimeService;
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
    @Autowired private OvertimeService overtimeService;
    @Autowired private NotificationService notificationService;
    @Autowired private EmailFacade emailFacade;
    @Autowired private AuthUserHelper authUserHelper;

    // ==================== DASHBOARD ====================

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        LocalDate today = LocalDate.now();

        long totalEmployees = userRepository.findByStatus(UserStatus.ACTIVE).size();
        long pendingLeaves  = leaveRepository.countByStatus(LeaveStatus.PENDING);
        long pendingOT      = overtimeService.countByStatus(OvertimeStatus.PENDING);

        var allAssignments = taskAssignmentRepository.findAllWithUser();
        long activeTasks    = allAssignments.stream().filter(a -> a.getStatus() == TaskStatus.IN_PROGRESS).count();
        long completedTasks = allAssignments.stream().filter(a -> a.getStatus() == TaskStatus.COMPLETED).count();
        long pendingTasks   = allAssignments.stream().filter(a -> a.getStatus() == TaskStatus.PENDING).count();

        long checkedInToday = attendanceRepository.findByAttendanceDateBetween(today, today).size();
        long absentToday    = Math.max(0, totalEmployees - checkedInToday);

        List<String> attLabels   = new ArrayList<>();
        List<Integer> attPresent = new ArrayList<>();
        List<Integer> attLate    = new ArrayList<>();
        List<Integer> attAbsent  = new ArrayList<>();

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            attLabels.add(date.format(DateTimeFormatter.ofPattern("dd/MM")));
            List<Attendance> dayAtt = attendanceRepository.findByAttendanceDateBetween(date, date);
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

        model.addAttribute("totalEmployees", totalEmployees);
        model.addAttribute("pendingLeaves",  pendingLeaves);
        model.addAttribute("pendingOT",      pendingOT);
        model.addAttribute("activeTasks",    activeTasks);
        model.addAttribute("absentToday",    absentToday);
        model.addAttribute("completedTasks", completedTasks);
        model.addAttribute("pendingTasks",   pendingTasks);
        model.addAttribute("recentLeaves",   pendingFirst);
        model.addAttribute("recentTasks",    taskRepository.findAll());
        model.addAttribute("teamMembers",    userRepository.findByStatus(UserStatus.ACTIVE));
        model.addAttribute("topPerformers",  topPerformers);
        model.addAttribute("attLabels",      attLabels);
        model.addAttribute("attPresent",     attPresent);
        model.addAttribute("attLate",        attLate);
        model.addAttribute("attAbsent",      attAbsent);
        model.addAttribute("today", today.format(DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy")));
        return "manager/dashboard";
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
        model.addAttribute("departments", userRepository.findAll().stream()
                .map(User::getDepartment).filter(d -> d != null).distinct().collect(Collectors.toList()));
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedDeptId", deptId);
        return "manager/team";
    }

    // ==================== OVERTIME APPROVAL ====================

    @GetMapping("/overtime")
    public String overtimeList(@RequestParam(required = false) String status, Model model) {
        List<OvertimeRequest> requests;
        if (status != null && !status.isBlank()) {
            requests = overtimeService.getAllRequests().stream()
                    .filter(r -> r.getStatus().name().equals(status))
                    .collect(Collectors.toList());
        } else {
            requests = overtimeService.getAllRequests();
        }
        long countPending  = overtimeService.countByStatus(OvertimeStatus.PENDING);
        long countApproved = overtimeService.countByStatus(OvertimeStatus.APPROVED);
        long countRejected = overtimeService.countByStatus(OvertimeStatus.REJECTED);

        model.addAttribute("requests", requests);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("countPending",  countPending);
        model.addAttribute("countApproved", countApproved);
        model.addAttribute("countRejected", countRejected);
        return "manager/overtime";
    }

    @GetMapping("/overtime/approve/{id}")
    public String approveOT(@PathVariable Integer id, Authentication auth, RedirectAttributes ra) {
        try {
            User approver = authUserHelper.getCurrentUser(auth);
            OvertimeRequest req = overtimeService.approveRequest(id, approver);
            // Notification
            if (req.getUser() != null) {
                notificationService.createNotification(req.getUser(),
                        "✅ Đơn OT ngày " + req.getOvertimeDate() + " đã được duyệt!",
                        NotificationType.SUCCESS, "/user1/overtime");
            }
            ra.addFlashAttribute("success", "Đã duyệt đơn OT!");
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
        try {
            User approver = authUserHelper.getCurrentUser(auth);
            OvertimeRequest req = overtimeService.rejectRequest(id, approver, reason);
            if (req.getUser() != null) {
                notificationService.createNotification(req.getUser(),
                        "❌ Đơn OT ngày " + req.getOvertimeDate() + " bị từ chối: " + reason,
                        NotificationType.DANGER, "/user1/overtime");
            }
            ra.addFlashAttribute("success", "Đã từ chối đơn OT!");
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
        List<OvertimeRequest> myRequests = overtimeService.getRequestsByUser(currentUser.getId());
        model.addAttribute("myRequests", myRequests);
        model.addAttribute("currentUser", currentUser);
        return "user1/overtime";
    }
}