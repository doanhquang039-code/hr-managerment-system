package com.example.hr.controllers;

import com.example.hr.enums.TaskStatus;
import com.example.hr.models.Payroll;
import com.example.hr.models.TaskAssignment;
import com.example.hr.models.User;
import com.example.hr.repository.*;
import com.example.hr.service.AuthUserHelper;
import com.example.hr.service.NotificationService;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/user1")
public class User1Controller {

    @Autowired private PayrollRepository payrollRepository;
    @Autowired private LeaveRequestRepository leaveRepository;
    @Autowired private TaskRepository taskRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private TaskAssignmentRepository taskAssignmentRepository;
    @Autowired private AttendanceRepository attendanceRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private AuthUserHelper authUserHelper;
    @Autowired private NotificationService notificationService;

    private static final String UPLOAD_DIR = "public/test1/";

    // ==================== HELPER ====================

    private User getCurrentUser(Authentication auth) {
        return authUserHelper.getCurrentUser(auth);
    }

    // ==================== DASHBOARD ====================

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        User user = getCurrentUser(authentication);
        if (user == null) return "redirect:/login?error=user_not_found";

        // Today attendance
        var todayAttendance = attendanceRepository
                .findByUserAndAttendanceDate(user, LocalDate.now())
                .orElse(null);

        // This month attendance count
        int month = LocalDate.now().getMonthValue();
        int year  = LocalDate.now().getYear();
        long attendanceDays = attendanceRepository.findByUserAndYearAndMonth(user, year, month).size();

        // Attendance rate (workdays in month ≈ attendance days / 22 * 100)
        int rate = (int) Math.min(100, (attendanceDays * 100 / 22));

        // Recent attendance (last 7)
        var allAtt = attendanceRepository.findByUserAndYearAndMonth(user, year, month);
        var recent = allAtt.stream()
                .sorted((a, b) -> b.getAttendanceDate().compareTo(a.getAttendanceDate()))
                .limit(7).collect(Collectors.toList());

        // Pending leaves
        long pendingLeaves = leaveRepository.findByUser(user).stream()
                .filter(l -> l.getStatus().name().equals("PENDING")).count();

        // My tasks
        List<TaskAssignment> myTasks = taskAssignmentRepository.findByUser(user);
        long totalTasks = myTasks.size();

        // Latest payroll
        List<Payroll> payrolls = payrollRepository.findByUser(user);
        Payroll latestSalary = payrolls.stream()
                .sorted((a, b) -> {
                    int cmp = b.getYear().compareTo(a.getYear());
                    return cmp != 0 ? cmp : b.getMonth().compareTo(a.getMonth());
                }).findFirst().orElse(null);

        // My leaves (recent 5)
        var myLeaves = leaveRepository.findByUser(user).stream()
                .limit(5).collect(Collectors.toList());

        model.addAttribute("user", user);
        model.addAttribute("todayAttendance", todayAttendance);
        model.addAttribute("attendanceDays", attendanceDays);
        model.addAttribute("attendanceRate", rate);
        model.addAttribute("recentAttendance", recent);
        model.addAttribute("pendingLeaves", pendingLeaves);
        model.addAttribute("totalTasks", totalTasks);
        model.addAttribute("myTasks", myTasks.stream().limit(5).collect(Collectors.toList()));
        model.addAttribute("myLeaves", myLeaves);
        model.addAttribute("latestSalary", latestSalary);
        model.addAttribute("latestPayrollMonth", latestSalary != null ? latestSalary.getMonth() : null);
        model.addAttribute("latestPayrollYear", latestSalary != null ? latestSalary.getYear() : null);
        model.addAttribute("today", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        model.addAttribute("unreadNotifications", notificationService.countUnread(user));

        return "user1/dashboard";
    }

    // ==================== PROFILE ====================

    @GetMapping("/profile")
    public String profile(Authentication authentication, Model model) {
        User user = getCurrentUser(authentication);
        if (user == null) return "redirect:/login?error=user_not_found";

        int month = LocalDate.now().getMonthValue();
        int year  = LocalDate.now().getYear();
        long attendanceDays = attendanceRepository.findByUserAndYearAndMonth(user, year, month).size();
        long totalLeaves    = leaveRepository.findByUser(user).size();
        long totalTasks     = taskAssignmentRepository.findByUser(user).size();

        model.addAttribute("user", user);
        model.addAttribute("attendanceDays", attendanceDays);
        model.addAttribute("totalLeaves", totalLeaves);
        model.addAttribute("totalTasks", totalTasks);
        model.addAttribute("unreadNotifications", notificationService.countUnread(user));
        return "user1/profile";
    }

    @PostMapping("/profile/update-avatar")
    public String updateAvatar(@RequestParam("image") MultipartFile file,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) throws IOException {
        User user = getCurrentUser(authentication);
        if (user == null) return "redirect:/login";

        if (!file.isEmpty()) {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path path = Paths.get(UPLOAD_DIR + fileName);
            Files.createDirectories(path.getParent());
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            user.setProfileImage(fileName);
            userRepository.save(user);
            redirectAttributes.addFlashAttribute("successMsg", "✅ Ảnh đại diện đã được cập nhật!");
        }
        return "redirect:/user1/profile";
    }

    @PostMapping("/profile/change-password")
    public String changePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(authentication);
        if (user == null) return "redirect:/login";

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            redirectAttributes.addFlashAttribute("passwordError", "Mật khẩu hiện tại không đúng!");
            return "redirect:/user1/profile";
        }
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("passwordError", "Mật khẩu mới và xác nhận không khớp!");
            return "redirect:/user1/profile";
        }
        if (newPassword.length() < 6) {
            redirectAttributes.addFlashAttribute("passwordError", "Mật khẩu mới phải có ít nhất 6 ký tự!");
            return "redirect:/user1/profile";
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("successMsg", "✅ Đổi mật khẩu thành công!");
        return "redirect:/user1/profile";
    }

    // ==================== PAYROLL ====================

    @GetMapping("/payroll")
    public String payroll(Authentication authentication, Model model) {
        User user = getCurrentUser(authentication);
        if (user == null) return "redirect:/login";

        List<Payroll> payrolls = payrollRepository.findByUser(user).stream()
                .sorted((a, b) -> {
                    int cmp = b.getYear().compareTo(a.getYear());
                    return cmp != 0 ? cmp : b.getMonth().compareTo(a.getMonth());
                }).collect(Collectors.toList());

        long totalPaid = payrolls.stream()
                .filter(p -> p.getPaymentStatus().name().equals("PAID")).count();

        BigDecimal latestNetSalary = payrolls.isEmpty() ? null :
                payrolls.get(0).getBaseSalary()
                        .add(payrolls.get(0).getBonus())
                        .subtract(payrolls.get(0).getDeductions());

        model.addAttribute("user", user);
        model.addAttribute("payrolls", payrolls);
        model.addAttribute("totalPaid", totalPaid);
        model.addAttribute("latestNetSalary", latestNetSalary);
        model.addAttribute("unreadNotifications", notificationService.countUnread(user));
        return "user1/payroll";
    }

    // ==================== MY TASKS ====================

    @GetMapping("/tasks")
    public String myTasks(@RequestParam(required = false) String filter,
                          Authentication authentication, Model model) {
        User user = getCurrentUser(authentication);
        if (user == null) return "redirect:/login";

        List<TaskAssignment> allTasks = taskAssignmentRepository.findByUser(user);

        List<TaskAssignment> myTasks = (filter != null && !filter.isBlank())
                ? allTasks.stream().filter(a -> a.getStatus().name().equals(filter)).collect(Collectors.toList())
                : allTasks;

        long pendingCount    = allTasks.stream().filter(a -> a.getStatus() == TaskStatus.PENDING).count();
        long inProgressCount = allTasks.stream().filter(a -> a.getStatus() == TaskStatus.IN_PROGRESS).count();
        long doneCount       = allTasks.stream().filter(a -> a.getStatus() == TaskStatus.COMPLETED).count();

        model.addAttribute("user", user);
        model.addAttribute("myTasks", myTasks);
        model.addAttribute("filter", filter);
        model.addAttribute("totalTasks", allTasks.size());
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("inProgressCount", inProgressCount);
        model.addAttribute("doneCount", doneCount);
        model.addAttribute("unreadNotifications", notificationService.countUnread(user));
        return "user1/my-tasks";
    }

    @PostMapping("/tasks/{id}/update-status")
    public String updateTaskStatus(@PathVariable Integer id,
                                   @RequestParam String status,
                                   Authentication authentication) {
        User user = getCurrentUser(authentication);
        if (user == null) return "redirect:/login";

        Optional<TaskAssignment> optAssignment = taskAssignmentRepository.findById(id);
        if (optAssignment.isPresent()) {
            TaskAssignment assignment = optAssignment.get();
            // Make sure user owns this assignment
            if (assignment.getUser().getId().equals(user.getId())) {
                try {
                    // Map frontend values to actual enum (DONE→COMPLETED)
                    String mapped = status.equals("DONE") ? "COMPLETED" : status;
                    assignment.setStatus(TaskStatus.valueOf(mapped));
                    taskAssignmentRepository.save(assignment);
                } catch (IllegalArgumentException ignored) {}
            }
        }
        return "redirect:/user1/tasks";
    }
}