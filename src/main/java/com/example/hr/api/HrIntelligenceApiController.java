package com.example.hr.api;



import com.example.hr.payment.entity.Payment;
import com.example.hr.payroll.entity.Payroll;
import com.example.hr.service.ApprovalReminderService;
import com.example.hr.service.AttritionRiskService;
import com.example.hr.payment.service.BankSalaryTransferService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hr-intelligence")
@PreAuthorize("hasAnyRole('ADMIN','MANAGER','HIRING')")
public class HrIntelligenceApiController {

    private final ApprovalReminderService approvalReminderService;
    private final AttritionRiskService attritionRiskService;
    private final BankSalaryTransferService bankSalaryTransferService;

    public HrIntelligenceApiController(ApprovalReminderService approvalReminderService,
                                       AttritionRiskService attritionRiskService,
                                       BankSalaryTransferService bankSalaryTransferService) {
        this.approvalReminderService = approvalReminderService;
        this.attritionRiskService = attritionRiskService;
        this.bankSalaryTransferService = bankSalaryTransferService;
    }

    @PostMapping("/approval-reminders/leave")
    public ResponseEntity<Map<String, String>> remindLeaveApprovals() {
        approvalReminderService.remindPendingLeaveApprovals();
        return ResponseEntity.ok(Map.of("status", "sent"));
    }

    @PostMapping("/payroll-notifications")
    public ResponseEntity<Map<String, Object>> publishPayrollNotifications(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        return ResponseEntity.ok(approvalReminderService.publishPayrollNotifications(month, year));
    }

    @GetMapping("/attrition-risk")
    public ResponseEntity<List<Map<String, Object>>> attritionRisk() {
        return ResponseEntity.ok(attritionRiskService.calculateRisks());
    }

    @PostMapping("/attrition-risk/notify")
    public ResponseEntity<Map<String, String>> notifyAttritionRisk() {
        attritionRiskService.notifyHighRiskEmployees();
        return ResponseEntity.ok(Map.of("status", "sent"));
    }

    @PostMapping("/bank-salary-transfers")
    public ResponseEntity<Map<String, Object>> createBankSalaryTransfers(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        return ResponseEntity.ok(bankSalaryTransferService.createSalaryTransfers(month, year));
    }
}
