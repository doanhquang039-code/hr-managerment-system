package com.example.hr.controllers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.hr.enums.NotificationType;
import com.example.hr.enums.PaymentStatus;
import com.example.hr.models.Contract;
import com.example.hr.models.Payroll;
import com.example.hr.models.User;
import com.example.hr.repository.ContractRepository;
import com.example.hr.repository.PayrollRepository;
import com.example.hr.repository.UserRepository;
import com.example.hr.service.HrAuditLogService;
import com.example.hr.service.NotificationService;

@Controller
@RequestMapping("/admin/payroll")
public class PayrollController {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    @Autowired
    private PayrollRepository payrollRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private HrAuditLogService hrAuditLogService;

    @GetMapping
    public String list(@RequestParam(name = "keyword", required = false) String keyword, Model model) {
        List<Payroll> payrolls = payrollRepository.findAllWithUser(keyword);
        model.addAttribute("payrolls", payrolls);
        model.addAttribute("keyword", keyword);
        return "admin/payroll-list";
    }

    @PostMapping("/generate/{userId}")
    public String generatePayroll(@PathVariable Integer userId, Authentication auth) {
        User user = userRepository.findById(userId).orElseThrow();
        LocalDate today = LocalDate.now();

        payrollRepository.findByUserIdAndMonthAndYear(userId, today.getMonthValue(), today.getYear())
                .ifPresent(existing -> {
                    throw new IllegalStateException("Bang luong thang nay da ton tai cho nhan vien nay.");
                });

        Contract contract = contractRepository.findByUser(user).stream()
                .filter(this::isContractUsable)
                .max(Comparator.comparing(Contract::getSignDate, Comparator.nullsLast(LocalDate::compareTo)))
                .orElseThrow(() -> new IllegalStateException("Khong tim thay hop dong hop le de tao bang luong."));

        BigDecimal baseSalary = contract.getBaseSalaryOnContract();
        if (baseSalary == null) {
            throw new IllegalStateException("Hop dong chua co luong co ban.");
        }

        BigDecimal allowanceCoeff = ZERO;
        if (user.getPosition() != null && user.getPosition().getAllowanceCoeff() != null) {
            allowanceCoeff = user.getPosition().getAllowanceCoeff();
        }

        Payroll payroll = new Payroll();
        payroll.setUser(user);
        payroll.setMonth(today.getMonthValue());
        payroll.setYear(today.getYear());
        payroll.setBaseSalary(baseSalary);
        payroll.setBonus(baseSalary.multiply(allowanceCoeff));
        payroll.setDeductions(ZERO);
        payroll.setPaymentStatus(PaymentStatus.PENDING);

        payrollRepository.save(payroll);
        notificationService.createNotification(
                user,
                "Phiu luong thang " + payroll.getMonth() + "/" + payroll.getYear() + " da duoc tao. Hay kiem tra ngay!",
                NotificationType.PAYROLL,
                "/user1/payroll");
        hrAuditLogService.log(auth, "PAYROLL_GENERATED", "Payroll", String.valueOf(payroll.getId()),
                "Tạo bảng lương tháng " + payroll.getMonth() + "/" + payroll.getYear() + " cho userId=" + userId, null);
        return "redirect:/admin/payroll";
    }

    @GetMapping("/mark-paid/{id}")
    public String markAsPaid(@PathVariable Integer id, Authentication auth) {
        Payroll payroll = payrollRepository.findById(id).orElseThrow();
        if (payroll.getPaymentStatus() == PaymentStatus.PAID) {
            return "redirect:/admin/payroll";
        }

        payroll.setPaymentStatus(PaymentStatus.PAID);
        payrollRepository.save(payroll);

        if (payroll.getUser() != null) {
            notificationService.createNotification(
                    payroll.getUser(),
                    "Luong thang " + payroll.getMonth() + "/" + payroll.getYear() + " da duoc thanh toan toan bo!",
                    NotificationType.SUCCESS,
                    "/user1/payroll");
        }
        hrAuditLogService.log(auth, "PAYROLL_MARK_PAID", "Payroll", String.valueOf(id),
                "Đánh dấu đã trả lương tháng " + payroll.getMonth() + "/" + payroll.getYear(), null);
        return "redirect:/admin/payroll";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, Authentication auth) {
        payrollRepository.findById(id).ifPresent(p ->
                hrAuditLogService.log(auth, "PAYROLL_DELETED", "Payroll", String.valueOf(id),
                        "Xóa bảng lương tháng " + p.getMonth() + "/" + p.getYear(), null));
        payrollRepository.deleteById(id);
        return "redirect:/admin/payroll";
    }

    private boolean isContractUsable(Contract contract) {
        if (contract == null || contract.getBaseSalaryOnContract() == null) {
            return false;
        }

        LocalDate today = LocalDate.now();
        LocalDate signDate = contract.getSignDate();
        LocalDate expiryDate = contract.getExpiryDate();

        boolean started = signDate == null || !signDate.isAfter(today);
        boolean notExpired = expiryDate == null || !expiryDate.isBefore(today);
        return started && notExpired;
    }
}
