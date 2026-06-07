package com.example.hr.service;


import com.example.hr.payroll.entity.Payroll;
import com.example.hr.dto.DashboardStatsDTO;
import com.example.hr.enums.AssetStatus;
import com.example.hr.enums.LeaveStatus;
import com.example.hr.enums.OvertimeStatus;
import com.example.hr.enums.TrainingStatus;
import com.example.hr.enums.UserStatus;
import com.example.hr.models.EmployeeWarning;
import com.example.hr.leave.entity.LeaveRequest;
import com.example.hr.models.OvertimeRequest;
import com.example.hr.models.User;
import com.example.hr.repository.AssetRepository;
import com.example.hr.repository.EmployeeBenefitRepository;
import com.example.hr.repository.EmployeeDocumentRepository;
import com.example.hr.repository.EmployeeWarningRepository;
import com.example.hr.leave.repository.LeaveRequestRepository;
import com.example.hr.repository.OvertimeRequestRepository;
import com.example.hr.payroll.repository.PayrollRepository;
import com.example.hr.training.repository.TrainingProgramRepository;
import com.example.hr.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private static final Logger log = LoggerFactory.getLogger(DashboardService.class);

    private final UserRepository userRepository;
    private final PayrollRepository payrollRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final OvertimeRequestRepository overtimeRequestRepository;
    private final TrainingProgramRepository trainingProgramRepository;
    private final EmployeeWarningRepository warningRepository;
    private final EmployeeBenefitRepository benefitRepository;
    private final AssetRepository assetRepository;
    private final EmployeeDocumentRepository documentRepository;

    public DashboardService(
            UserRepository userRepository,
            PayrollRepository payrollRepository,
            LeaveRequestRepository leaveRequestRepository,
            OvertimeRequestRepository overtimeRequestRepository,
            TrainingProgramRepository trainingProgramRepository,
            EmployeeWarningRepository warningRepository,
            EmployeeBenefitRepository benefitRepository,
            AssetRepository assetRepository,
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

    @Cacheable(value = "dashboard", key = "'stats'")
    public DashboardStatsDTO buildDashboardStats() {
        log.info("Building dashboard statistics...");

        DashboardStatsDTO stats = new DashboardStatsDTO();
        LocalDate today = LocalDate.now();
        LocalDateTime firstOfMonth = today.withDayOfMonth(1).atStartOfDay();

        long totalEmployees = userRepository.count();
        long newHires = userRepository.countByCreatedAtGreaterThanEqual(firstOfMonth);
        long pendingLeaves = leaveRequestRepository.countByStatus(LeaveStatus.PENDING);
        long pendingOT = overtimeRequestRepository.countByStatus(OvertimeStatus.PENDING.name());
        long activeTrainings = trainingProgramRepository.countByStatus(TrainingStatus.IN_PROGRESS);
        long unackWarnings = warningRepository.countByIsAcknowledged(false);
        BigDecimal benefitCost = benefitRepository.sumTotalActiveBenefitCost();
        long totalAssets = assetRepository.count();
        long availableAssets = assetRepository.countByStatus("AVAILABLE");
        // long expiringDocs = documentRepository.countByExpiryDateBetween(today.plusDays(1), today.plusDays(30));
        long expiringDocs = 0; // Temporary: EmployeeDocument doesn't have expiryDate field

        stats.setTotalEmployees(totalEmployees);
        stats.setNewHiresThisMonth(newHires);
        stats.setPendingLeaveRequests(pendingLeaves);
        stats.setPendingOvertimeRequests(pendingOT);
        stats.setActiveTrainingPrograms(activeTrainings);
        stats.setUnacknowledgedWarnings(unackWarnings);
        stats.setMonthlyBenefitCost(benefitCost);
        stats.setTotalBenefitCost(benefitCost);
        stats.setTotalAssets(totalAssets);
        stats.setAvailableAssets(availableAssets);
        stats.setExpiringDocuments(expiringDocs);

        log.info("Dashboard stats built: employees={}, pendingLeaves={}, pendingOT={}",
                totalEmployees, pendingLeaves, pendingOT);
        return stats;
    }

    @Cacheable(value = "dashboard", key = "'activity-' + #limit")
    public List<Map<String, Object>> getRecentActivity(int limit) {
        int boundedLimit = Math.max(1, Math.min(limit, 50));
        PageRequest page = PageRequest.of(0, boundedLimit);
        List<Map<String, Object>> activities = new ArrayList<>();

        leaveRequestRepository.findAllByOrderByCreatedAtDesc(page).forEach(l -> {
            Map<String, Object> act = new LinkedHashMap<>();
            act.put("type", "LEAVE_REQUEST");
            act.put("description", (l.getUser() != null ? l.getUser().getFullName() : "N/A") + " - Nghá»‰ phÃ©p");
            act.put("status", l.getStatus());
            act.put("time", l.getCreatedAt());
            activities.add(act);
        });

        // overtimeRequestRepository.findAllByOrderByCreatedAtDesc(page).forEach(ot -> {
        //     Map<String, Object> act = new LinkedHashMap<>();
        //     act.put("type", "OVERTIME_REQUEST");
        //     act.put("description",
        //             (ot.getUser() != null ? ot.getUser().getFullName() : "N/A")
        //                     + " - OT " + (ot.getTotalHours() != null ? ot.getTotalHours() : 0) + "h");
        //     act.put("status", ot.getStatus());
        //     act.put("time", ot.getCreatedAt());
        //     activities.add(act);
        // });

        warningRepository.findAllByOrderByIssuedDateDesc(page).forEach(w -> {
            Map<String, Object> act = new LinkedHashMap<>();
            act.put("type", "WARNING_ISSUED");
            act.put("description",
                    (w.getUser() != null ? w.getUser().getFullName() : "N/A")
                            + " - Cáº£nh cÃ¡o " + w.getWarningLevel().name());
            act.put("status", Boolean.TRUE.equals(w.getIsAcknowledged()) ? "ACKNOWLEDGED" : "PENDING");
            act.put("time", w.getIssuedDate());
            activities.add(act);
        });

        activities.sort((a, b) -> {
            Object timeA = a.get("time");
            Object timeB = b.get("time");
            if (timeA == null) {
                return 1;
            }
            if (timeB == null) {
                return -1;
            }
         return timeB.toString().compareTo(timeA.toString());
        });

        return activities.stream().limit(boundedLimit).collect(Collectors.toList());
    }

    public Map<String, Object> calculateAttritionMetrics(int year) {
        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("year", year);

        long totalAtStart = userRepository.count();
       // Má»šI
long terminatedThisYear = userRepository.countByStatus(UserStatus.INACTIVE);
List<User> terminatedUsers = userRepository.findByStatus(UserStatus.INACTIVE);

        double attritionRate = totalAtStart == 0 ? 0 : (double) terminatedThisYear / totalAtStart * 100;
        metrics.put("terminatedCount", terminatedThisYear);
        metrics.put("attritionRate", Math.round(attritionRate * 10.0) / 10.0);
        metrics.put("totalEmployees", totalAtStart);

        Map<String, Long> byDept = terminatedUsers.stream()
                .filter(u -> u.getDepartment() != null)
                .collect(Collectors.groupingBy(
                        u -> u.getDepartment().getDepartmentName(),
                        Collectors.counting()
                ));
        metrics.put("byDepartment", byDept);

        return metrics;
    }

    /**
     * Get business metrics for admin dashboard
     */
    @Cacheable(value = "dashboard", key = "'business-metrics'")
    public Map<String, Object> getBusinessMetrics() {
        Map<String, Object> metrics = new LinkedHashMap<>();
        
        // Employee metrics
        long totalEmployees = userRepository.count();
        long activeEmployees = userRepository.countByStatus(UserStatus.ACTIVE);
        long newHiresThisMonth = userRepository.countByCreatedAtGreaterThanEqual(
                LocalDate.now().withDayOfMonth(1).atStartOfDay());
        
        metrics.put("totalEmployees", totalEmployees);
        metrics.put("activeEmployees", activeEmployees);
        metrics.put("newHiresThisMonth", newHiresThisMonth);
        
        // Leave metrics
        long pendingLeaves = leaveRequestRepository.countByStatus(LeaveStatus.PENDING);
        long approvedLeaves = leaveRequestRepository.countByStatus(LeaveStatus.APPROVED);
        
        metrics.put("pendingLeaves", pendingLeaves);
        metrics.put("approvedLeaves", approvedLeaves);
        
        // Training metrics
        long activeTrainings = trainingProgramRepository.countByStatus(TrainingStatus.IN_PROGRESS);
        long completedTrainings = trainingProgramRepository.countByStatus(TrainingStatus.COMPLETED);
        
        metrics.put("activeTrainings", activeTrainings);
        metrics.put("completedTrainings", completedTrainings);
        
        // Asset metrics
        long totalAssets = assetRepository.count();
        long availableAssets = assetRepository.countByStatus("AVAILABLE");
        
        metrics.put("totalAssets", totalAssets);
        metrics.put("availableAssets", availableAssets);
        
        return metrics;
    }
}


