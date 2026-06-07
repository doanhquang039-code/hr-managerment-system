package com.example.hr.service;










import com.example.hr.department.repository.DepartmentRepository;
import com.example.hr.department.entity.Department;
import com.example.hr.training.repository.TrainingEnrollmentRepository;
import com.example.hr.training.repository.TrainingProgramRepository;
import com.example.hr.user.repository.UserRepository;
import com.example.hr.training.repository.TrainingEnrollmentRepository;
import com.example.hr.training.repository.TrainingProgramRepository;
import com.example.hr.payroll.entity.Payroll;
import com.example.hr.training.repository.TrainingEnrollmentRepository;
import com.example.hr.training.repository.TrainingProgramRepository;
import com.example.hr.payroll.repository.PayrollRepository;
import com.example.hr.training.repository.TrainingEnrollmentRepository;
import com.example.hr.training.repository.TrainingProgramRepository;
import com.example.hr.leave.entity.LeaveRequest;
import com.example.hr.training.repository.TrainingEnrollmentRepository;
import com.example.hr.training.repository.TrainingProgramRepository;
import com.example.hr.leave.repository.LeaveRequestRepository;
import com.example.hr.training.repository.TrainingEnrollmentRepository;
import com.example.hr.training.repository.TrainingProgramRepository;
import com.example.hr.attendance.entity.Attendance;
import com.example.hr.training.repository.TrainingEnrollmentRepository;
import com.example.hr.training.repository.TrainingProgramRepository;
import com.example.hr.attendance.repository.AttendanceRepository;
import com.example.hr.training.repository.TrainingEnrollmentRepository;
import com.example.hr.training.repository.TrainingProgramRepository;
import com.example.hr.dto.AnalyticsDashboardDTO;
import com.example.hr.training.repository.TrainingEnrollmentRepository;
import com.example.hr.training.repository.TrainingProgramRepository;
import com.example.hr.enums.AttendanceStatus;
import com.example.hr.training.repository.TrainingEnrollmentRepository;
import com.example.hr.training.repository.TrainingProgramRepository;
import com.example.hr.enums.LeaveStatus;
import com.example.hr.training.repository.TrainingEnrollmentRepository;
import com.example.hr.training.repository.TrainingProgramRepository;
import com.example.hr.enums.UserStatus;
import com.example.hr.training.repository.TrainingEnrollmentRepository;
import com.example.hr.training.repository.TrainingProgramRepository;
import com.example.hr.models.*;
import com.example.hr.training.repository.TrainingEnrollmentRepository;
import com.example.hr.training.repository.TrainingProgramRepository;
import com.example.hr.recruitment.repository.CandidateRepository;
import com.example.hr.recruitment.repository.JobPostingRepository;
import com.example.hr.repository.*;
import com.example.hr.training.repository.TrainingEnrollmentRepository;
import com.example.hr.training.repository.TrainingProgramRepository;
import lombok.RequiredArgsConstructor;
import com.example.hr.training.repository.TrainingEnrollmentRepository;
import com.example.hr.training.repository.TrainingProgramRepository;
import org.springframework.stereotype.Service;
import com.example.hr.training.repository.TrainingEnrollmentRepository;
import com.example.hr.training.repository.TrainingProgramRepository;
import org.springframework.transaction.annotation.Transactional;

import com.example.hr.training.repository.TrainingEnrollmentRepository;
import com.example.hr.training.repository.TrainingProgramRepository;
import java.time.LocalDate;
import com.example.hr.training.repository.TrainingEnrollmentRepository;
import com.example.hr.training.repository.TrainingProgramRepository;
import java.time.LocalDateTime;
import com.example.hr.training.repository.TrainingEnrollmentRepository;
import com.example.hr.training.repository.TrainingProgramRepository;
import java.time.format.DateTimeFormatter;
import com.example.hr.training.repository.TrainingEnrollmentRepository;
import com.example.hr.training.repository.TrainingProgramRepository;
import java.util.*;
import com.example.hr.training.repository.TrainingEnrollmentRepository;
import com.example.hr.training.repository.TrainingProgramRepository;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdvancedAnalyticsService {
    
    private final UserRepository userRepository;
    private final AttendanceRepository attendanceRepository;
    private final PayrollRepository payrollRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final TrainingProgramRepository trainingProgramRepository;
    private final TrainingEnrollmentRepository trainingEnrollmentRepository;
    private final JobPostingRepository jobPostingRepository;
    private final CandidateRepository candidateRepository;
    private final DepartmentRepository departmentRepository;
    private final PerformanceReviewRepository performanceReviewRepository;
    
    @Transactional(readOnly = true)
    public AnalyticsDashboardDTO getDashboardData() {
        AnalyticsDashboardDTO dashboard = new AnalyticsDashboardDTO();
        
        // Overview metrics
        dashboard.setTotalEmployees(userRepository.count());
        dashboard.setActiveEmployees(userRepository.countByStatus(UserStatus.ACTIVE));
        dashboard.setNewEmployeesThisMonth(getNewEmployeesThisMonth());
        dashboard.setAttendanceRateToday(calculateAttendanceRateToday());
        dashboard.setAttendanceRateThisMonth(calculateAttendanceRateThisMonth());
        
        // Payroll metrics
        dashboard.setTotalPayrollThisMonth(calculateTotalPayrollThisMonth());
        dashboard.setAverageSalary(calculateAverageSalary());
        
        // Leave metrics
        dashboard.setPendingLeaveRequests(leaveRequestRepository.countByStatusString(LeaveStatus.PENDING));
        dashboard.setApprovedLeavesToday(getApprovedLeavesToday());
        
        // Training metrics
        dashboard.setActiveTrainingPrograms(trainingProgramRepository.count());
        dashboard.setTrainingCompletionRate(calculateTrainingCompletionRate());
        
        // Recruitment metrics
        dashboard.setActiveJobPostings(jobPostingRepository.countByStatus("ACTIVE"));
        dashboard.setCandidatesThisMonth(getCandidatesThisMonth());
        
        // Charts
        dashboard.setEmployeeGrowthChart(getEmployeeGrowthChart());
        dashboard.setAttendanceTrendChart(getAttendanceTrendChart());
        dashboard.setDepartmentDistributionChart(getDepartmentDistributionChart());
        dashboard.setPayrollTrendChart(getPayrollTrendChart());
        
        // Top performers
        dashboard.setTopPerformers(getTopPerformers());
        
        // Department stats
        dashboard.setDepartmentStats(getDepartmentStats());
        
        // Recent activities
        dashboard.setRecentActivities(getRecentActivities());
        
        return dashboard;
    }
    
    private Long getNewEmployeesThisMonth() {
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        return userRepository.countByCreatedAtAfter(startOfMonth);
    }
    
    private Double calculateAttendanceRateToday() {
        LocalDate today = LocalDate.now();
        long totalEmployees = userRepository.countByStatus(UserStatus.ACTIVE);
        if (totalEmployees == 0) return 0.0;
        
        long presentToday = attendanceRepository.countByAttendanceDateAndStatus(today, AttendanceStatus.PRESENT);
        return (presentToday * 100.0) / totalEmployees;
    }
    
    private Double calculateAttendanceRateThisMonth() {
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate today = LocalDate.now();
        
        long totalEmployees = userRepository.countByStatus(UserStatus.ACTIVE);
        if (totalEmployees == 0) return 0.0;
        
        long workingDays = startOfMonth.datesUntil(today.plusDays(1))
                .filter(date -> date.getDayOfWeek().getValue() < 6)
                .count();
        
        if (workingDays == 0) return 0.0;
        
        long totalPresent = attendanceRepository.countByAttendanceDateBetweenAndStatus(startOfMonth, today, AttendanceStatus.PRESENT);
        long expectedAttendance = totalEmployees * workingDays;
        
        return (totalPresent * 100.0) / expectedAttendance;
    }
    
    private Double calculateTotalPayrollThisMonth() {
        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();
        
        List<Payroll> payrolls = payrollRepository.findByYearMonthBetween(currentYear, currentMonth, currentYear, currentMonth);
        return payrolls.stream()
                .filter(p -> p.getNetSalary() != null)
                .mapToDouble(p -> p.getNetSalary().doubleValue())
                .sum();
    }
    
    private Double calculateAverageSalary() {
        List<Payroll> recentPayrolls = payrollRepository.findTop100ByOrderByCreatedAtDesc();
        if (recentPayrolls.isEmpty()) return 0.0;
        
        return recentPayrolls.stream()
                .filter(p -> p.getNetSalary() != null)
                .mapToDouble(p -> p.getNetSalary().doubleValue())
                .average()
                .orElse(0.0);
    }
    
    private Long getApprovedLeavesToday() {
        LocalDate today = LocalDate.now();
        // countByStartDateLessThanEqualAndEndDateGreaterThanEqualAndStatus: endDate param first, startDate second (Spring naming)
        return leaveRequestRepository.countByStartDateLessThanEqualAndEndDateGreaterThanEqualAndStatus(
                today, today, LeaveStatus.APPROVED);
    }
    
    private Double calculateTrainingCompletionRate() {
        long totalEnrollments = trainingEnrollmentRepository.count();
        if (totalEnrollments == 0) return 0.0;
        
        long completedEnrollments = trainingEnrollmentRepository.countByStatusString("COMPLETED");
        return (completedEnrollments * 100.0) / totalEnrollments;
    }
    
    private Long getCandidatesThisMonth() {
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        return candidateRepository.countByAppliedDateAfter(startOfMonth);
    }
    
    private List<AnalyticsDashboardDTO.ChartDataDTO> getEmployeeGrowthChart() {
        List<AnalyticsDashboardDTO.ChartDataDTO> chartData = new ArrayList<>();
        LocalDate today = LocalDate.now();
        
        for (int i = 11; i >= 0; i--) {
            LocalDate month = today.minusMonths(i);
            LocalDate endOfMonth = month.withDayOfMonth(month.lengthOfMonth());
            
            long count = userRepository.countByCreatedAtBefore(endOfMonth.atTime(23, 59, 59));
            
            chartData.add(new AnalyticsDashboardDTO.ChartDataDTO(
                    month.format(DateTimeFormatter.ofPattern("MMM yyyy")),
                    (double) count,
                    "#4F46E5",
                    null
            ));
        }
        
        return chartData;
    }
    
    private List<AnalyticsDashboardDTO.ChartDataDTO> getAttendanceTrendChart() {
        List<AnalyticsDashboardDTO.ChartDataDTO> chartData = new ArrayList<>();
        LocalDate today = LocalDate.now();
        
        for (int i = 29; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            long present = attendanceRepository.countByAttendanceDateAndStatus(date, AttendanceStatus.PRESENT);
            long total = userRepository.countByStatus(UserStatus.ACTIVE);
            
            double rate = total > 0 ? (present * 100.0) / total : 0.0;
            
            chartData.add(new AnalyticsDashboardDTO.ChartDataDTO(
                    date.format(DateTimeFormatter.ofPattern("dd/MM")),
                    rate,
                    "#10B981",
                    null
            ));
        }
        
        return chartData;
    }
    
    private List<AnalyticsDashboardDTO.ChartDataDTO> getDepartmentDistributionChart() {
        List<Department> departments = departmentRepository.findAll();
        
        return departments.stream()
                .map(dept -> {
                    long count = userRepository.countByDepartment(dept);
                    return new AnalyticsDashboardDTO.ChartDataDTO(
                            dept.getDepartmentName(),
                            (double) count,
                            getRandomColor(),
                            null
                    );
                })
                .collect(Collectors.toList());
    }
    
    private List<AnalyticsDashboardDTO.ChartDataDTO> getPayrollTrendChart() {
        List<AnalyticsDashboardDTO.ChartDataDTO> chartData = new ArrayList<>();
        LocalDate today = LocalDate.now();
        
        for (int i = 5; i >= 0; i--) {
            LocalDate month = today.minusMonths(i);
            int year = month.getYear();
            int monthValue = month.getMonthValue();
            
            List<Payroll> payrolls = payrollRepository.findByYearMonthBetween(year, monthValue, year, monthValue);
            double total = payrolls.stream()
                    .filter(p -> p.getNetSalary() != null)
                    .mapToDouble(p -> p.getNetSalary().doubleValue())
                    .sum();
            
            chartData.add(new AnalyticsDashboardDTO.ChartDataDTO(
                    month.format(DateTimeFormatter.ofPattern("MMM yyyy")),
                    total,
                    "#F59E0B",
                    null
            ));
        }
        
        return chartData;
    }
    
    private List<AnalyticsDashboardDTO.EmployeePerformanceDTO> getTopPerformers() {
        List<PerformanceReview> reviews = performanceReviewRepository.findTop10ByOrderByOverallScoreDesc();
        
        return reviews.stream()
                .map(review -> new AnalyticsDashboardDTO.EmployeePerformanceDTO(
                        Long.valueOf(review.getUser().getId()),
                        review.getUser().getFullName(),
                        review.getUser().getDepartment() != null ? 
                            review.getUser().getDepartment().getDepartmentName() : "N/A",
                        review.getOverallScore().doubleValue(),
                        review.getUser().getProfileImage()
                ))
                .collect(Collectors.toList());
    }
    
    private List<AnalyticsDashboardDTO.DepartmentStatsDTO> getDepartmentStats() {
        List<Department> departments = departmentRepository.findAll();
        
        return departments.stream()
                .map(dept -> {
                    long employeeCount = userRepository.countByDepartment(dept);
                    double attendanceRate = calculateDepartmentAttendanceRate(dept);
                    double avgPerformance = calculateDepartmentAvgPerformance(dept);
                    
                    return new AnalyticsDashboardDTO.DepartmentStatsDTO(
                            Long.valueOf(dept.getId()),
                            dept.getDepartmentName(),
                            employeeCount,
                            attendanceRate,
                            avgPerformance
                    );
                })
                .collect(Collectors.toList());
    }
    
    private Double calculateDepartmentAttendanceRate(Department department) {
        LocalDate today = LocalDate.now();
        List<User> employees = userRepository.findByDepartmentAndStatus(department, UserStatus.ACTIVE);
        
        if (employees.isEmpty()) return 0.0;
        
        long present = employees.stream()
                .filter(emp -> attendanceRepository.existsByUserAndAttendanceDateAndStatus(emp, today, AttendanceStatus.PRESENT))
                .count();
        
        return (present * 100.0) / employees.size();
    }
    
    private Double calculateDepartmentAvgPerformance(Department department) {
        List<User> employees = userRepository.findByDepartmentAndStatus(department, UserStatus.ACTIVE);
        
        if (employees.isEmpty()) return 0.0;
        
        return employees.stream()
                .mapToDouble(emp -> {
                    Optional<PerformanceReview> latestReview = 
                        performanceReviewRepository.findTopByEmployeeOrderByReviewDateDesc(emp);
                    return latestReview.map(r -> r.getOverallScore().doubleValue()).orElse(0.0);
                })
                .average()
                .orElse(0.0);
    }
    
    private List<AnalyticsDashboardDTO.ActivityDTO> getRecentActivities() {
        List<AnalyticsDashboardDTO.ActivityDTO> activities = new ArrayList<>();
        
        // Recent leave requests
        List<LeaveRequest> recentLeaves = leaveRequestRepository.findTop5ByOrderByCreatedAtDesc();
        recentLeaves.forEach(leave -> activities.add(new AnalyticsDashboardDTO.ActivityDTO(
                "LEAVE_REQUEST",
                leave.getUser().getFullName() + " requested " + leave.getLeaveType() + " leave",
                formatDateTime(leave.getCreatedAt()),
                "calendar"
        )));
        
        // Recent hires
        List<User> recentHires = userRepository.findTop5ByOrderByCreatedAtDesc();
        recentHires.forEach(user -> activities.add(new AnalyticsDashboardDTO.ActivityDTO(
                "NEW_HIRE",
                user.getFullName() + " joined the company",
                formatDateTime(user.getCreatedAt()),
                "user-plus"
        )));
        
        return activities.stream()
                .sorted(Comparator.comparing(AnalyticsDashboardDTO.ActivityDTO::getTimestamp).reversed())
                .limit(10)
                .collect(Collectors.toList());
    }
    
    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
    
    private String getRandomColor() {
        String[] colors = {"#4F46E5", "#10B981", "#F59E0B", "#EF4444", "#8B5CF6", "#EC4899"};
        return colors[new Random().nextInt(colors.length)];
    }
}


