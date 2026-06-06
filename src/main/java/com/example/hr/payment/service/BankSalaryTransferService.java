package com.example.hr.payment.service;

import com.example.hr.enums.PaymentStatus;
import com.example.hr.models.EmployeeProfile;
import com.example.hr.payment.entity.Payment;
import com.example.hr.payroll.entity.Payroll;
import com.example.hr.models.User;
import com.example.hr.repository.EmployeeProfileRepository;
import com.example.hr.payment.repository.PaymentRepository;
import com.example.hr.payroll.repository.PayrollRepository;
import com.example.hr.service.NotificationService;
import com.example.hr.util.EncryptionUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BankSalaryTransferService {

    private final PayrollRepository payrollRepository;
    private final PaymentRepository paymentRepository;
    private final EmployeeProfileRepository profileRepository;
    private final NotificationService notificationService;

    @Value("${hr.bank-transfer.auto-complete:false}")
    private boolean autoComplete;

    @Transactional
    public Map<String, Object> createSalaryTransfers(Integer month, Integer year) {
        List<Payroll> payrolls = payrollRepository.findAll().stream()
                .filter(p -> month == null || month.equals(p.getMonth()))
                .filter(p -> year == null || year.equals(p.getYear()))
                .filter(p -> p.getPaymentStatus() != PaymentStatus.PAID)
                .toList();

        int created = 0;
        int skipped = 0;
        for (Payroll payroll : payrolls) {
            if (!paymentRepository.findByPayrollId(payroll.getId()).isEmpty()) {
                skipped++;
                continue;
            }
            User user = payroll.getUser();
            if (user == null) {
                skipped++;
                continue;
            }
            EmployeeProfile profile = profileRepository.findByUser(user).orElse(null);
            if (profile == null || isBlank(profile.getBankAccountNumber()) || isBlank(profile.getBankName())) {
                skipped++;
                continue;
            }

            Payment payment = new Payment();
            payment.setUser(user);
            payment.setPayroll(payroll);
            payment.setPaymentType("SALARY");
            payment.setPaymentMethod("BANK_TRANSFER");
            payment.setAmount(netSalary(payroll));
            payment.setBankName(profile.getBankName());
            payment.setAccountNumber(profile.getBankAccountNumber());
            payment.setTransactionId("SAL-" + payroll.getId() + "-" + System.currentTimeMillis());
            payment.setPaymentStatus(autoComplete ? "COMPLETED" : "PROCESSING");
            payment.setPaymentDate(autoComplete ? LocalDate.now() : null);
            payment.setNotes("Auto salary transfer to " + profile.getBankName()
                    + " / " + EncryptionUtils.maskSensitiveData(profile.getBankAccountNumber(), 3));
            payment.setCreatedAt(LocalDateTime.now());
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            if (autoComplete) {
                payroll.setPaymentStatus(PaymentStatus.PAID);
                payrollRepository.save(payroll);
                notificationService.createNotification(user, "Lương kỳ " + payroll.getMonth() + "/" + payroll.getYear() + " đã được chuyển khoản.", com.example.hr.enums.NotificationType.PAYROLL, "/user1/payroll");
            }
            created++;
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("matchedPayrolls", payrolls.size());
        result.put("createdTransfers", created);
        result.put("skipped", skipped);
        result.put("autoComplete", autoComplete);
        return result;
    }

    private static BigDecimal netSalary(Payroll payroll) {
        BigDecimal base = payroll.getBaseSalary() != null ? payroll.getBaseSalary() : BigDecimal.ZERO;
        BigDecimal bonus = payroll.getBonus() != null ? payroll.getBonus() : BigDecimal.ZERO;
        BigDecimal deductions = payroll.getDeductions() != null ? payroll.getDeductions() : BigDecimal.ZERO;
        return base.add(bonus).subtract(deductions);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
