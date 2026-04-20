package com.example.hr.service;

import com.example.hr.dto.DashboardStatsDTO;
import com.example.hr.enums.AssetStatus;
import com.example.hr.models.*;
import com.example.hr.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service tổng hợp dữ liệu cho Dashboard chính.
 */
@Service
public class DashboardService {

    private static final Logger log = LoggerFactory.getLogger(DashboardService.class);

    private final UserRepository userRepository;
    private final PayrollRepository payrollRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final OvertimeRequestRepository overtimeRequestRepository;
    private final TrainingProgramRepository trainingProgramRepository;
    private final EmployeeWarningRepository warningRepository;
    private final EmployeeBenefitRepository benefitRepository;
    private final CompanyAssetRepository assetRepository;
    private final EmployeeDocumentRepository documentRepository;

    public DashboardService(
            UserRepository userRepository,
            PayrollRepository payrollRepository,
            LeaveRequestRepository leaveRequestRepository,
            OvertimeRequestRepository overtimeRequestRepository,
            TrainingProgramRepository trainingProgramRepository,
            EmployeeWarningRepository warningRepository,
            EmployeeBenefitRepository benefitRepository,
            CompanyAssetRepository assetRepository,
            EmployeeDocumentRepository documentRepository) {
        this.userRepository = userRepository;
        this.payrollRepository = payrollRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.overtimeRequestRepository = overtimeRequestRepository;
        this.trainingProgramRepository = trainingProgramRepository;
        this.warningRepository = warningRepository;
        this.benefitRepository = benefitRepository;
        this.assetRepository = assetRepository;
        this.documentRepository = documentRepository;
    }

    /**
     * Build tổng hợp stats cho Dashboard.
     */
    public DashboardStatsDTO buildDashboardStats() {
        log.info("Building dashboard statistics...");
        DashboardStatsDTO stats = new DashboardStatsDTO();

        // Employee overview
        long totalEmployees = userRepository.count();
        stats.setTotalEmployees(totalEmployees);

        // New hires this month
        LocalDate firstOfMonth = LocalDate.now().withDayOfMonth(1);
        long newHires = userRepository.findAll().stream()
                .filter(u -> u.getCreatedAt() != null)
                .filter(u -> u.getCreatedAt().toLocalDate().isAfter(firstOfMonth.minusDays(1)))
                .count();
        stats.setNewHiresThisMonth(newHires);

        // Pending leave requests
        long pendingLeaves = leaveRequestRepository.findAll().stream()
                .filter(l -> l.getStatus() != null && l.getStatus().name().equalsIgnoreCase("PENDING"))
                .count();
        stats.setPendingLeaveRequests(pendingLeaves);

        // Pending OT requests
        long pendingOT = overtimeRequestRepository.findAll().stream()
                .filter(ot -> ot.getStatus() == com.example.hr.enums.OvertimeStatus.PENDING)
                .count();
        stats.setPendingOvertimeRequests(pendingOT);

        // Active training programs
        long activeTrainings = trainingProgramRepository.findAll().stream()
                .filter(tp -> tp.getStatus() == com.example.hr.enums.TrainingStatus.IN_PROGRESS)
                .count();
        stats.setActiveTrainingPrograms(activeTrainings);

        // Unacknowledged warnings
        long unackWarnings = warningRepository.findAll().stream()
                .filter(w -> !Boolean.TRUE.equals(w.getIsAcknowledged()))
                .count();
        stats.setUnacknowledgedWarnings(unackWarnings);

        // Active benefits cost
        BigDecimal benefitCost = benefitRepository.findAll().stream()
                .filter(b -> b.getStatus() == com.example.hr.enums.BenefitStatus.ACTIVE)
                .map(EmployeeBenefit::getMonetaryValue)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.setMonthlyBenefitCost(benefitCost);
        stats.setTotalBenefitCost(benefitCost);

        // Asset summary
        long totalAssets = assetRepository.count();
        long availableAssets = assetRepository.findAll().stream()
                .filter(a -> a.getStatus() == AssetStatus.AVAILABLE)
                .count();
        stats.setTotalAssets(totalAssets);
        stats.setAvailableAssets(availableAssets);

        // Expiring documents (within 30 days)
        long expiringDocs = documentRepository.findAll().stream()
                .filter(d -> d.getExpiryDate() != null)
                .filter(d -> d.getExpiryDate().isBefore(LocalDate.now().plusDays(30)))
                .filter(d -> d.getExpiryDate().isAfter(LocalDate.now()))
                .count();
        stats.setExpiringDocuments(expiringDocs);

        log.info("Dashboard stats built: employees={}, pendingLeaves={}, pendingOT={}",
                totalEmployees, pendingLeaves, pendingOT);
        return stats;
    }

    /**
     * Lấy activity log gần đây cho dashboard.
     */
    public List<Map<String, Object>> getRecentActivity(int limit) {
        List<Map<String, Object>> activities = new ArrayList<>();

        // Recent leave requests
        leaveRequestRepository.findAll().stream()
                .sorted(Comparator.comparing(LeaveRequest::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(5)
                .forEach(l -> {
                    Map<String, Object> act = new LinkedHashMap<>();
                    act.put("type", "LEAVE_REQUEST");
                    act.put("description", (l.getUser() != null ? l.getUser().getFullName() : "N/A") + " - Nghỉ phép");
                    act.put("status", l.getStatus());
                    act.put("time", l.getCreatedAt());
                    activities.add(act);
                });

        // Recent OT requests
        overtimeRequestRepository.findAll().stream()
                .sorted(Comparator.comparing(OvertimeRequest::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(5)
                .forEach(ot -> {
                    Map<String, Object> act = new LinkedHashMap<>();
                    act.put("type", "OVERTIME_REQUEST");
                    act.put("description", (ot.getUser() != null ? ot.getUser().getFullName() : "N/A") + " - OT " + (ot.getTotalHours() != null ? ot.getTotalHours() : 0) + "h");
                    act.put("status", ot.getStatus());
                    act.put("time", ot.getCreatedAt());
                    activities.add(act);
                });

        // Recent warnings
        warningRepository.findAll().stream()
                .sorted(Comparator.comparing(EmployeeWarning::getIssuedDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(3)
                .forEach(w -> {
                    Map<String, Object> act = new LinkedHashMap<>();
                    act.put("type", "WARNING_ISSUED");
                    act.put("description", (w.getUser() != null ? w.getUser().getFullName() : "N/A") + " - Cảnh cáo " + w.getWarningLevel().name());
                    act.put("status", Boolean.TRUE.equals(w.getIsAcknowledged()) ? "ACKNOWLEDGED" : "PENDING");
                    act.put("time", w.getIssuedDate());
                    activities.add(act);
                });

        // Sort by time, most recent first
        activities.sort((a, b) -> {
            Object timeA = a.get("time");
            Object timeB = b.get("time");
            if (timeA == null) return 1;
            if (timeB == null) return -1;
            return timeB.toString().compareTo(timeA.toString());
        });

        return activities.stream().limit(limit).collect(Collectors.toList());
    }

    /**
     * Tính tỷ lệ biến động nhân sự (attrition rate).
     */
    public Map<String, Object> calculateAttritionMetrics(int year) {
        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("year", year);

        long totalAtStart = userRepository.count(); // Simplified
        // In production, query historical data

        List<User> allUsers = userRepository.findAll();
        long terminatedThisYear = allUsers.stream()
                .filter(u -> u.getStatus() != null && u.getStatus().name().equals("TERMINATED"))
                .count();

        double attritionRate = totalAtStart == 0 ? 0 : (double) terminatedThisYear / totalAtStart * 100;
        metrics.put("terminatedCount", terminatedThisYear);
        metrics.put("attritionRate", Math.round(attritionRate * 10.0) / 10.0);
        metrics.put("totalEmployees", totalAtStart);

        // Breakdown by department
        Map<String, Long> byDept = allUsers.stream()
                .filter(u -> u.getStatus() != null && u.getStatus().name().equals("TERMINATED"))
                .filter(u -> u.getDepartment() != null)
                .collect(Collectors.groupingBy(
                        u -> u.getDepartment().getDepartmentName(),
                        Collectors.counting()
                ));
        metrics.put("byDepartment", byDept);

        return metrics;
    }
}
