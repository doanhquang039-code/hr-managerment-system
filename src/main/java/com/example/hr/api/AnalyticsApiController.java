package com.example.hr.api;



import com.example.hr.department.entity.Department;
import com.example.hr.payroll.entity.Payroll;
import com.example.hr.dto.EmployeeAnalyticsDTO;
import com.example.hr.payroll.dto.PayrollSummaryDTO;
import com.example.hr.payroll.service.AdvancedPayrollService;
import com.example.hr.service.EmployeeAnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsApiController {

    private final EmployeeAnalyticsService analyticsService;
    private final AdvancedPayrollService payrollService;

    public AnalyticsApiController(EmployeeAnalyticsService analyticsService,
                                    AdvancedPayrollService payrollService) {
        this.analyticsService = analyticsService;
        this.payrollService = payrollService;
    }

    @GetMapping("/overview")
    public ResponseEntity<EmployeeAnalyticsDTO> getOverview() {
        return ResponseEntity.ok(analyticsService.buildAnalytics());
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        return ResponseEntity.ok(analyticsService.getDashboardQuickStats());
    }

    @GetMapping("/turnover/{year}")
    public ResponseEntity<Map<String, Double>> getTurnover(@PathVariable int year) {
        double rate = analyticsService.calculateTurnoverRate(year);
        return ResponseEntity.ok(Map.of("turnoverRate", rate, "year", (double) year));
    }

    @GetMapping("/headcount/department")
    public ResponseEntity<Map<String, Long>> headcountByDept() {
        return ResponseEntity.ok(analyticsService.getHeadcountByDepartment());
    }

    @GetMapping("/headcount/position")
    public ResponseEntity<Map<String, Long>> headcountByPosition() {
        return ResponseEntity.ok(analyticsService.getHeadcountByPosition());
    }

    @GetMapping("/cost/{month}/{year}")
    public ResponseEntity<Map<String, BigDecimal>> hrCost(@PathVariable int month, @PathVariable int year) {
        BigDecimal total = analyticsService.calculateTotalHRCost(month, year);
        BigDecimal avg = analyticsService.calculateAvgCostPerEmployee(month, year);
        return ResponseEntity.ok(Map.of("totalCost", total, "avgPerEmployee", avg));
    }

    @GetMapping("/payroll/{userId}/{month}/{year}")
    public ResponseEntity<PayrollSummaryDTO> payrollDetail(@PathVariable Integer userId,
                                                             @PathVariable int month,
                                                             @PathVariable int year) {
        return ResponseEntity.ok(payrollService.calculateDetailedPayroll(userId, month, year));
    }

    @GetMapping("/payroll/all/{month}/{year}")
    public ResponseEntity<List<PayrollSummaryDTO>> allPayrolls(@PathVariable int month,
                                                                  @PathVariable int year) {
        return ResponseEntity.ok(payrollService.calculateAllPayrolls(month, year));
    }
}


