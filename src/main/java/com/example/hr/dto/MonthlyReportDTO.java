package com.example.hr.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO bÃ¡o cÃ¡o tá»•ng há»£p thÃ¡ng/nÄƒm.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyReportDTO {

    private int month;
    private int year;
    private LocalDateTime generatedAt;

    // Compatibility fields for report generation/export
    private long totalEmployees;
    private long totalLeaveRequests;
    private long totalOvertimeRequests;
    private long activeTrainingPrograms;
    private long activeWarnings;
    private BigDecimal totalPayrollAmount;

    // NhÃ¢n sá»±
    private long totalHeadcount;
    private long newHires;
    private long terminations;
    private double attendanceRate;

    // TÃ i chÃ­nh
    private BigDecimal totalPayroll;
    private BigDecimal totalOvertime;
    private BigDecimal totalBenefitCost;
    private BigDecimal totalTrainingBudget;

    // Hoáº¡t Ä‘á»™ng
    private long leaveRequestsCount;
    private long overtimeRequestsCount;
    private long warningsIssuedCount;
    private long trainingsCompletedCount;

    // PhÃ¢n bá»‘ theo phÃ²ng ban
    private Map<String, BigDecimal> payrollByDepartment;
    private Map<String, Long> headcountByDepartment;
    private Map<String, Double> attendanceByDepartment;
}


