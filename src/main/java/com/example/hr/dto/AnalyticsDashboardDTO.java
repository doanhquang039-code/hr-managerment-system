package com.example.hr.dto;



import com.example.hr.department.entity.Department;
import com.example.hr.payroll.entity.Payroll;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsDashboardDTO {
    
    // Overview metrics
    private Long totalEmployees;
    private Long activeEmployees;
    private Long newEmployeesThisMonth;
    private Double attendanceRateToday;
    private Double attendanceRateThisMonth;
    
    // Payroll metrics
    private Double totalPayrollThisMonth;
    private Double averageSalary;
    
    // Leave metrics
    private Long pendingLeaveRequests;
    private Long approvedLeavesToday;
    
    // Training metrics
    private Long activeTrainingPrograms;
    private Double trainingCompletionRate;
    
    // Recruitment metrics
    private Long activeJobPostings;
    private Long candidatesThisMonth;
    
    // Charts data
    private List<ChartDataDTO> employeeGrowthChart;
    private List<ChartDataDTO> attendanceTrendChart;
    private List<ChartDataDTO> departmentDistributionChart;
    private List<ChartDataDTO> payrollTrendChart;
    
    // Top performers
    private List<EmployeePerformanceDTO> topPerformers;
    
    // Department stats
    private List<DepartmentStatsDTO> departmentStats;
    
    // Recent activities
    private List<ActivityDTO> recentActivities;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChartDataDTO {
        private String label;
        private Double value;
        private String color;
        private Map<String, Object> metadata;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmployeePerformanceDTO {
        private Long userId;
        private String name;
        private String department;
        private Double performanceScore;
        private String avatar;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DepartmentStatsDTO {
        private Long departmentId;
        private String departmentName;
        private Long employeeCount;
        private Double attendanceRate;
        private Double averagePerformance;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityDTO {
        private String type;
        private String description;
        private String timestamp;
        private String icon;
    }
}


