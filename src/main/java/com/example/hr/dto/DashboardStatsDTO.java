package com.example.hr.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO tổng hợp thống kê dashboard nâng cao.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsDTO {

    // Employee stats
    private long totalEmployees;
    private long activeEmployees;
    private long newHiresThisMonth;
    private double turnoverRate;

    // Attendance
    private long presentToday;
    private long absentToday;
    private long lateToday;
    private double attendanceRate;

    // Leave
    private long pendingLeaveRequests;
    private long approvedLeavesToday;

    // Overtime
    private long pendingOvertimeRequests;
    private BigDecimal totalOvertimeHoursThisMonth;

    // Payroll
    private BigDecimal totalPayrollThisMonth;
    private long unpaidPayrolls;

    // Training
    private long activeTrainingPrograms;
    private long upcomingTrainings;
    private long employeesInTraining;

    // Warnings
    private long activeWarnings;
    private long severeWarnings;
    private long unacknowledgedWarnings;

    // Benefits
    private BigDecimal totalBenefitCost;
    private BigDecimal monthlyBenefitCost;
    private long expiringBenefits;

    // Assets
    private long totalAssets;
    private long availableAssets;
    private BigDecimal totalAssetValue;
    private long overdueAssetReturns;

    private long expiringDocuments;

    // Documents
    private long pendingDocumentVerifications;

    // Contracts
    private long expiringContracts;
}
