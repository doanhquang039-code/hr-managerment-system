package com.example.hr.service;







import com.example.hr.department.entity.Department;
import com.example.hr.training.repository.TrainingEnrollmentRepository;
import com.example.hr.user.repository.UserRepository;
import com.example.hr.training.repository.TrainingEnrollmentRepository;
import com.example.hr.payroll.entity.Payroll;
import com.example.hr.training.repository.TrainingEnrollmentRepository;
import com.example.hr.payroll.repository.PayrollRepository;
import com.example.hr.training.repository.TrainingEnrollmentRepository;
import com.example.hr.attendance.entity.Attendance;
import com.example.hr.training.repository.TrainingEnrollmentRepository;
import com.example.hr.attendance.repository.AttendanceRepository;
import com.example.hr.training.repository.TrainingEnrollmentRepository;
import com.example.hr.dto.EmployeeAnalyticsDTO;
import com.example.hr.training.repository.TrainingEnrollmentRepository;
import com.example.hr.enums.UserStatus;
import com.example.hr.training.repository.TrainingEnrollmentRepository;
import com.example.hr.models.User;
import com.example.hr.training.repository.TrainingEnrollmentRepository;
import com.example.hr.repository.*;
import com.example.hr.training.repository.TrainingEnrollmentRepository;
import org.springframework.stereotype.Service;
import com.example.hr.training.repository.TrainingEnrollmentRepository;
import org.springframework.transaction.annotation.Transactional;

import com.example.hr.training.repository.TrainingEnrollmentRepository;
import java.math.BigDecimal;
import com.example.hr.training.repository.TrainingEnrollmentRepository;
import java.math.RoundingMode;
import com.example.hr.training.repository.TrainingEnrollmentRepository;
import java.time.LocalDate;
import com.example.hr.training.repository.TrainingEnrollmentRepository;
import java.util.HashMap;
import com.example.hr.training.repository.TrainingEnrollmentRepository;
import java.util.List;
import com.example.hr.training.repository.TrainingEnrollmentRepository;
import java.util.Map;
import com.example.hr.training.repository.TrainingEnrollmentRepository;
import java.util.stream.Collectors;

/**
 * Service phÃ¢n tÃ­ch nhÃ¢n sá»± nÃ¢ng cao.
 * Bao gá»“m: turnover rate, headcount trending, department distribution, chi phÃ­.
 */
@Service
@Transactional(readOnly = true)
public class EmployeeAnalyticsService {

    private final UserRepository userRepository;
    private final PayrollRepository payrollRepository;
    private final AttendanceRepository attendanceRepository;
    private final TrainingEnrollmentRepository enrollmentRepository;
    private final EmployeeWarningRepository warningRepository;
    private final EmployeeBenefitRepository benefitRepository;
    private final OvertimeRequestRepository overtimeRepository;

    public EmployeeAnalyticsService(UserRepository userRepository,
                                     PayrollRepository payrollRepository,
                                     AttendanceRepository attendanceRepository,
                                     TrainingEnrollmentRepository enrollmentRepository,
                                     EmployeeWarningRepository warningRepository,
                                     EmployeeBenefitRepository benefitRepository,
                                     OvertimeRequestRepository overtimeRepository) {
        this.userRepository = userRepository;
        this.payrollRepository = payrollRepository;
        this.attendanceRepository = attendanceRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.warningRepository = warningRepository;
        this.benefitRepository = benefitRepository;
        this.overtimeRepository = overtimeRepository;
    }

    /**
     * Build comprehensive analytics report.
     */
    public EmployeeAnalyticsDTO buildAnalytics() {
        List<User> allUsers = userRepository.findAll();
        long totalHeadcount = allUsers.size();
        long activeCount = allUsers.stream()
                .filter(u -> u.getStatus() == UserStatus.ACTIVE)
                .count();

        // Headcount by department
        Map<String, Long> byDepartment = allUsers.stream()
                .filter(u -> u.getDepartment() != null)
                .collect(Collectors.groupingBy(
                        u -> u.getDepartment().getDepartmentName(),
                        Collectors.counting()
                ));

        // Headcount by position
        Map<String, Long> byPosition = allUsers.stream()
                .filter(u -> u.getPosition() != null)
                .collect(Collectors.groupingBy(
                        u -> u.getPosition().getPositionName(),
                        Collectors.counting()
                ));

        // Headcount by status
        Map<String, Long> byStatus = allUsers.stream()
                .collect(Collectors.groupingBy(
                        u -> u.getStatus().name(),
                        Collectors.counting()
                ));

        // Warning rate
        long totalWarnings = warningRepository.count();
        double warningRate = totalHeadcount > 0
                ? (double) totalWarnings / totalHeadcount * 100 : 0;

        return EmployeeAnalyticsDTO.builder()
                .totalHeadcount(totalHeadcount)
                .headcountByDepartment(byDepartment)
                .headcountByPosition(byPosition)
                .headcountByStatus(byStatus)
                .totalActiveWarnings(totalWarnings)
                .warningRatePercent(Math.round(warningRate * 100.0) / 100.0)
                .build();
    }

    /**
     * TÃ­nh turnover rate (tá»· lá»‡ nghá»‰ viá»‡c).
     * Formula: (Terminations / Average Headcount) * 100
     */
    public double calculateTurnoverRate(int year) {
        List<User> allUsers = userRepository.findAll();
        long totalHeadcount = allUsers.size();
        long inactiveCount = allUsers.stream()
                .filter(u -> u.getStatus() == UserStatus.INACTIVE)
                .count();

        if (totalHeadcount == 0) return 0;
        return (double) inactiveCount / totalHeadcount * 100;
    }

    /**
     * PhÃ¢n bá»‘ nhÃ¢n viÃªn theo phÃ²ng ban.
     */
    public Map<String, Long> getHeadcountByDepartment() {
        return userRepository.findAll().stream()
                .filter(u -> u.getDepartment() != null)
                .collect(Collectors.groupingBy(
                        u -> u.getDepartment().getDepartmentName(),
                        Collectors.counting()
                ));
    }

    /**
     * PhÃ¢n bá»‘ nhÃ¢n viÃªn theo vá»‹ trÃ­.
     */
    public Map<String, Long> getHeadcountByPosition() {
        return userRepository.findAll().stream()
                .filter(u -> u.getPosition() != null)
                .collect(Collectors.groupingBy(
                        u -> u.getPosition().getPositionName(),
                        Collectors.counting()
                ));
    }

    /**
     * Tá»•ng chi phÃ­ nhÃ¢n sá»± (lÆ°Æ¡ng + phÃºc lá»£i).
     */
    public BigDecimal calculateTotalHRCost(int month, int year) {
        BigDecimal payrollCost = payrollRepository.findAll().stream()
                .filter(p -> p.getMonth() == month && p.getYear() == year && p.getNetSalary() != null)
                .map(p -> p.getNetSalary())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal benefitCost = benefitRepository.sumTotalActiveBenefitCost();

        return payrollCost.add(benefitCost != null ? benefitCost : BigDecimal.ZERO);
    }

    /**
     * Chi phÃ­ trung bÃ¬nh trÃªn má»—i nhÃ¢n viÃªn.
     */
    public BigDecimal calculateAvgCostPerEmployee(int month, int year) {
        BigDecimal totalCost = calculateTotalHRCost(month, year);
        long activeCount = userRepository.findAll().stream()
                .filter(u -> u.getStatus() == UserStatus.ACTIVE)
                .count();

        if (activeCount == 0) return BigDecimal.ZERO;
        return totalCost.divide(BigDecimal.valueOf(activeCount), 0, RoundingMode.HALF_UP);
    }

    /**
     * Láº¥y overview metrics nhanh cho dashboard.
     */
    public Map<String, Object> getDashboardQuickStats() {
        Map<String, Object> stats = new HashMap<>();
        List<User> allUsers = userRepository.findAll();

        stats.put("totalEmployees", allUsers.size());
        stats.put("activeEmployees", allUsers.stream()
                .filter(u -> u.getStatus() == UserStatus.ACTIVE).count());
        stats.put("turnoverRate", calculateTurnoverRate(LocalDate.now().getYear()));
        stats.put("totalWarnings", warningRepository.count());
        long pendingOvertime = overtimeRepository.findByStatus("PENDING").size();
        stats.put("pendingOvertime", pendingOvertime);
        stats.put("totalBenefitCost", benefitRepository.sumTotalActiveBenefitCost());

        return stats;
    }
}


