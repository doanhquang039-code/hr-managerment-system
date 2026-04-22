package com.example.hr.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.hr.enums.LeaveStatus;
import com.example.hr.enums.LeaveType;
import com.example.hr.enums.NotificationType;
import com.example.hr.enums.UserStatus;
import com.example.hr.models.LeaveRequest;
import com.example.hr.models.User;
import com.example.hr.repository.LeaveRequestRepository;
import com.example.hr.repository.UserRepository;
import com.example.hr.service.HrAuditLogService;
import com.example.hr.service.NotificationService;
import com.example.hr.service.EmailFacade;

import java.time.LocalDate;
import java.util.List;

@Controller
public class LeaveRequestController {

    @Autowired
    private LeaveRequestRepository leaveRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private HrAuditLogService hrAuditLogService;
    @Autowired
    private EmailFacade emailFacade;

    // ==================== ADMIN ====================

    @GetMapping("/admin/leaves")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String listAll(@RequestParam(required = false) String keyword,
                          @RequestParam(required = false) String status,
                          Model model) {
        var all = leaveRepository.findAllWithUser(keyword);
        // Filter by status nếu có
        var leaves = (status != null && !status.isBlank())
                ? all.stream().filter(l -> l.getStatus() != null && l.getStatus().name().equals(status)).toList()
                : all;
        long countPending  = all.stream().filter(l -> l.getStatus() == LeaveStatus.PENDING).count();
        long countApproved = all.stream().filter(l -> l.getStatus() == LeaveStatus.APPROVED).count();
        long countRejected = all.stream().filter(l -> l.getStatus() == LeaveStatus.REJECTED).count();
        model.addAttribute("leaves", leaves);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("countPending",  countPending);
        model.addAttribute("countApproved", countApproved);
        model.addAttribute("countRejected", countRejected);
        return "admin/leave-list";
    }

    @GetMapping("/admin/leaves/add")
    @PreAuthorize("hasRole('ADMIN')")
    public String showAddForm(Model model) {
        model.addAttribute("leaveRequest", new LeaveRequest());
        model.addAttribute("users", userRepository.findByStatus(UserStatus.ACTIVE));
        model.addAttribute("statuses", LeaveStatus.values());
        return "admin/leave-form";
    }

    @GetMapping("/admin/leaves/edit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String showEditForm(@PathVariable Integer id, Model model) {
        LeaveRequest lr = leaveRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ID không tồn tại: " + id));
        model.addAttribute("leaveRequest", lr);
        model.addAttribute("users", userRepository.findByStatus(UserStatus.ACTIVE));
        model.addAttribute("statuses", LeaveStatus.values());
        return "admin/leave-form";
    }

    @PostMapping("/admin/leaves/save")
    @PreAuthorize("hasRole('ADMIN')")
    public String save(@ModelAttribute("leaveRequest") LeaveRequest lr) {
        if (lr.getId() == null && lr.getStatus() == null) {
            lr.setStatus(LeaveStatus.PENDING);
        }
        leaveRepository.save(lr);
        return "redirect:/admin/leaves";
    }

    @GetMapping("/admin/leaves/approve/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String approve(@PathVariable Integer id, Authentication auth) {
        LeaveRequest lr = leaveRepository.findById(id).orElseThrow();
        lr.setStatus(LeaveStatus.APPROVED);
        // Dùng người đang login làm approver
        if (auth != null) {
            userRepository.findByUsername(auth.getName()).ifPresent(lr::setApprovedBy);
        }
        leaveRepository.save(lr);
        // Gửi thông báo cho nhân viên
        if (lr.getUser() != null) {
            notificationService.createNotification(
                lr.getUser(),
                "✅ Đơn nghỉ phép " + lr.getLeaveType() + " từ " + lr.getStartDate() + " đến " + lr.getEndDate() + " đã được DUYỆT!",
                NotificationType.SUCCESS,
                "/user/leaves"
            );
            // Gửi email
            User u = lr.getUser();
            if (u.getEmail() != null && !u.getEmail().isBlank()) {
                emailFacade.sendLeaveStatus(u.getEmail(), u.getFullName(),
                        lr.getLeaveType().name(),
                        lr.getStartDate().toString(), lr.getEndDate().toString(),
                        true, null);
            }
        }
        hrAuditLogService.log(auth, "LEAVE_APPROVED", "LeaveRequest", String.valueOf(id),
                "Duyệt đơn nghỉ " + lr.getLeaveType() + " cho userId=" + (lr.getUser() != null ? lr.getUser().getId() : "?"),
                null);
        return "redirect:/admin/leaves";
    }

    @GetMapping("/admin/leaves/reject/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String reject(@PathVariable Integer id, Authentication auth) {
        LeaveRequest lr = leaveRepository.findById(id).orElseThrow();
        lr.setStatus(LeaveStatus.REJECTED);
        leaveRepository.save(lr);
        // Gửi thông báo từ chối
        if (lr.getUser() != null) {
            notificationService.createNotification(
                lr.getUser(),
                "❌ Đơn nghỉ phép " + lr.getLeaveType() + " từ " + lr.getStartDate() + " đã bị TỪ CHỐI.",
                NotificationType.DANGER,
                "/user/leaves"
            );
            // Gửi email
            User u = lr.getUser();
            if (u.getEmail() != null && !u.getEmail().isBlank()) {
                emailFacade.sendLeaveStatus(u.getEmail(), u.getFullName(),
                        lr.getLeaveType().name(),
                        lr.getStartDate().toString(), lr.getEndDate().toString(),
                        false, "Đơn không được chấp thuận");
            }
        }
        hrAuditLogService.log(auth, "LEAVE_REJECTED", "LeaveRequest", String.valueOf(id),
                "Từ chối đơn nghỉ " + lr.getLeaveType() + " userId=" + (lr.getUser() != null ? lr.getUser().getId() : "?"),
                null);
        return "redirect:/admin/leaves";
    }

    @GetMapping("/admin/leaves/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@PathVariable Integer id) {
        leaveRepository.deleteById(id);
        return "redirect:/admin/leaves";
    }

    // ==================== USER SELF-SERVICE ====================

    @GetMapping("/user/leaves")
    @PreAuthorize("isAuthenticated()")
    public String userLeaves(Authentication auth, Model model) {
        User currentUser = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
        List<LeaveRequest> myLeaves = leaveRepository.findByUser(currentUser);
        model.addAttribute("myLeaves", myLeaves);
        model.addAttribute("currentUser", currentUser);
        return "user1/leave-request";
    }

    @PostMapping("/user/leaves/submit")
    @PreAuthorize("isAuthenticated()")
    public String submitLeave(@RequestParam String leaveType,
                              @RequestParam String startDate,
                              @RequestParam String endDate,
                              @RequestParam String reason,
                              Authentication auth) {
        User currentUser = userRepository.findByUsername(auth.getName())
                .orElseGet(() -> userRepository.findByEmail(auth.getName()).orElseThrow());

        LeaveRequest lr = new LeaveRequest();
        lr.setUser(currentUser);
        lr.setLeaveType(LeaveType.valueOf(leaveType));
        lr.setStartDate(LocalDate.parse(startDate));
        lr.setEndDate(LocalDate.parse(endDate));
        lr.setReason(reason);
        lr.setStatus(LeaveStatus.PENDING);

        leaveRepository.save(lr);

        // Thông báo xác nhận cho nhân viên
        notificationService.createNotification(
            currentUser,
            "📅 Đơn xin nghỉ " + leaveType + " từ " + startDate + " đến " + endDate + " đã được gửi, đang chờ duyệt.",
            NotificationType.LEAVE_REQUEST,
            "/user/leaves"
        );

        return "redirect:/user/leaves";
    }
}
