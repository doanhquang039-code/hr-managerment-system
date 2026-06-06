package com.example.hr.service;


import com.example.hr.payroll.service.PayrollSecurityService;
import com.example.hr.enums.LeaveStatus;
import com.example.hr.enums.NotificationType;
import com.example.hr.enums.PaymentStatus;
import com.example.hr.enums.Role;
import com.example.hr.enums.UserStatus;
import com.example.hr.leave.entity.LeaveRequest;
import com.example.hr.payroll.entity.Payroll;
import com.example.hr.models.User;
import com.example.hr.leave.repository.LeaveRequestRepository;
import com.example.hr.payroll.repository.PayrollRepository;
import com.example.hr.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ApprovalReminderService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final PayrollRepository payrollRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final AdvancedNotificationService advancedNotificationService;
    private final PayrollSecurityService payrollSecurityService;

    @Scheduled(cron = "${hr.reminders.leave-approval-cron:0 0 9 * * *}", zone = "Asia/Ho_Chi_Minh")
    @Transactional
    public void remindPendingLeaveApprovals() {
        List<LeaveRequest> pending = leaveRequestRepository.findByStatus(LeaveStatus.PENDING);
        if (pending.isEmpty()) {
            return;
        }
        List<User> approvers = userRepository.findByRoleInAndStatus(List.of(Role.ADMIN, Role.MANAGER), UserStatus.ACTIVE);
        String message = "Có " + pending.size() + " đơn nghỉ phép đang chờ duyệt.";
        for (User approver : approvers) {
            notificationService.createNotification(approver, message, NotificationType.LEAVE_REQUEST, "/admin/leaves?status=PENDING");
        }
    }

    @Transactional
    public Map<String, Object> publishPayrollNotifications(Integer month, Integer year) {
        List<Payroll> payrolls = payrollRepository.findAll().stream()
                .filter(p -> month == null || month.equals(p.getMonth()))
                .filter(p -> year == null || year.equals(p.getYear()))
                .toList();

        int notified = 0;
        for (Payroll payroll : payrolls) {
            payrollSecurityService.attachEncryptedSalaryPayload(payroll);
            payrollRepository.save(payroll);
            User user = payroll.getUser();
            if (user == null) {
                continue;
            }
            String monthText = payroll.getMonth() + "/" + payroll.getYear();
            notificationService.createNotification(
                    user,
                    "Bảng lương " + monthText + " đã sẵn sàng để xem.",
                    NotificationType.PAYROLL,
                    "/user1/payroll");
            advancedNotificationService.sendEmailNotification(
                    user,
                    "Bảng lương " + monthText,
                    "Bảng lương của bạn đã được phát hành trên HRMS. Vui lòng đăng nhập để xem chi tiết.");
            notified++;
        }
        return Map.of("matchedPayrolls", payrolls.size(), "notifiedEmployees", notified, "publishedAt", LocalDate.now().toString());
    }

    @Transactional
    public Map<String, Object> publishCurrentMonthPayrollNotifications() {
        LocalDate now = LocalDate.now();
        return publishPayrollNotifications(now.getMonthValue(), now.getYear());
    }

    public boolean isPaid(Payroll payroll) {
        return payroll != null && payroll.getPaymentStatus() == PaymentStatus.PAID;
    }
}
