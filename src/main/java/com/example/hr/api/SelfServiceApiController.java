package com.example.hr.api;

import com.example.hr.enums.LeaveStatus;
import com.example.hr.enums.LeaveType;
import com.example.hr.enums.NotificationType;
import com.example.hr.models.EmployeeProfile;
import com.example.hr.leave.entity.LeaveRequest;
import com.example.hr.payroll.entity.Payroll;
import com.example.hr.models.User;
import com.example.hr.leave.repository.LeaveRequestRepository;
import com.example.hr.payroll.repository.PayrollRepository;
import com.example.hr.user.repository.UserRepository;
import com.example.hr.service.AuthUserHelper;
import com.example.hr.service.NotificationService;
import com.example.hr.service.SelfServicePortalService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/self-service")
@PreAuthorize("isAuthenticated()")
public class SelfServiceApiController {

    private final AuthUserHelper authUserHelper;
    private final UserRepository userRepository;
    private final PayrollRepository payrollRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final SelfServicePortalService selfServicePortalService;
    private final NotificationService notificationService;

    public SelfServiceApiController(AuthUserHelper authUserHelper,
                                    UserRepository userRepository,
                                    PayrollRepository payrollRepository,
                                    LeaveRequestRepository leaveRequestRepository,
                                    SelfServicePortalService selfServicePortalService,
                                    NotificationService notificationService) {
        this.authUserHelper = authUserHelper;
        this.userRepository = userRepository;
        this.payrollRepository = payrollRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.selfServicePortalService = selfServicePortalService;
        this.notificationService = notificationService;
    }

    @GetMapping("/payslips")
    public ResponseEntity<List<Map<String, Object>>> payslips(Authentication auth) {
        User user = requireUser(auth);
        List<Map<String, Object>> result = payrollRepository.findByUser(user).stream()
                .sorted(Comparator.comparing(Payroll::getYear).thenComparing(Payroll::getMonth).reversed())
                .map(p -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", p.getId());
                    item.put("month", p.getMonth());
                    item.put("year", p.getYear());
                    item.put("baseSalary", p.getBaseSalary());
                    item.put("bonus", p.getBonus());
                    item.put("deductions", p.getDeductions());
                    item.put("paymentStatus", p.getPaymentStatus());
                    return item;
                })
                .toList();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/leave-requests")
    public ResponseEntity<Map<String, Object>> requestLeave(@RequestBody Map<String, String> body, Authentication auth) {
        User user = requireUser(auth);
        LeaveRequest request = new LeaveRequest();
        request.setUser(user);
        request.setLeaveType(LeaveType.valueOf(body.getOrDefault("leaveType", "ANNUAL")));
        request.setStartDate(LocalDate.parse(body.get("startDate")));
        request.setEndDate(LocalDate.parse(body.get("endDate")));
        request.setReason(body.getOrDefault("reason", ""));
        request.setStatus(LeaveStatus.PENDING);
        LeaveRequest saved = leaveRequestRepository.save(request);
        notificationService.createNotification(user, "ÄÆ¡n nghá»‰ phÃ©p Ä‘Ã£ Ä‘Æ°á»£c gá»­i vÃ  Ä‘ang chá» duyá»‡t.", NotificationType.LEAVE_REQUEST, "/user/leaves");
        return ResponseEntity.ok(Map.of("id", saved.getId(), "status", saved.getStatus().name()));
    }

    @PatchMapping("/profile")
    public ResponseEntity<EmployeeProfile> updateProfile(@RequestBody EmployeeProfile request, Authentication auth) {
        User user = requireUser(auth);
        return ResponseEntity.ok(selfServicePortalService.updateProfile(user, request));
    }

    @PatchMapping("/account")
    public ResponseEntity<Map<String, Object>> updateAccount(@RequestBody Map<String, String> body, Authentication auth) {
        User user = requireUser(auth);
        if (body.containsKey("fullName")) {
            user.setFullName(body.get("fullName"));
        }
        if (body.containsKey("email")) {
            user.setEmail(body.get("email"));
        }
        if (body.containsKey("phoneNumber")) {
            user.setPhoneNumber(body.get("phoneNumber"));
        }
        if (body.containsKey("address")) {
            user.setAddress(body.get("address"));
        }
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("id", user.getId(), "status", "updated"));
    }

    private User requireUser(Authentication auth) {
        User user = authUserHelper.getCurrentUser(auth);
        if (user == null) {
            throw new IllegalStateException("User not found");
        }
        return user;
    }
}


