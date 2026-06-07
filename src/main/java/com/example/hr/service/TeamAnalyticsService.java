package com.example.hr.service;





import com.example.hr.user.repository.UserRepository;
import com.example.hr.leave.repository.LeaveRequestRepository;
import com.example.hr.attendance.entity.Attendance;
import com.example.hr.attendance.repository.AttendanceRepository;
import com.example.hr.department.entity.Department;
import com.example.hr.models.User;
import com.example.hr.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TeamAnalyticsService {

    private final UserRepository userRepository;
    private final AttendanceRepository attendanceRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final TeamGoalRepository teamGoalRepository;

    /**
     * Get team overview metrics
     */
    public TeamOverview getTeamOverview(Department department) {
        List<User> teamMembers = userRepository.findByDepartment(department);
        
        long totalMembers = teamMembers.size();
        long activeMembers = teamMembers.stream()
                .filter(u -> "ACTIVE".equals(u.getStatus().name()))
                .count();
        
        // Attendance today
        LocalDate today = LocalDate.now();
        long presentToday = attendanceRepository.countByDepartmentAndDate(department, today);
        double attendanceRate = totalMembers > 0 ? (presentToday * 100.0 / totalMembers) : 0;
        
        // Leave requests
        long pendingLeaves = leaveRequestRepository.countPendingByDepartment(department);
        
        // Team goals
        long activeGoals = teamGoalRepository.findByDepartmentAndStatus(department, "IN_PROGRESS").size();
        Double avgProgress = teamGoalRepository.getAverageProgressByDepartment(department);
        
        return new TeamOverview(
                totalMembers,
                activeMembers,
                presentToday,
                attendanceRate,
                pendingLeaves,
                activeGoals,
                avgProgress != null ? avgProgress : 0.0
        );
    }

    /**
     * Get attendance trends for team
     */
    public List<AttendanceTrend> getAttendanceTrends(Department department, int days) {
        List<AttendanceTrend> trends = new ArrayList<>();
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days);
        
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            long present = attendanceRepository.countByDepartmentAndDate(department, date);
            long totalMembers = userRepository.findByDepartment(department).size();
            double rate = totalMembers > 0 ? (present * 100.0 / totalMembers) : 0;
            
            trends.add(new AttendanceTrend(date, present, totalMembers, rate));
        }
        
        return trends;
    }

    /**
     * Get leave patterns
     */
    public LeavePatterns getLeavePatterns(Department department) {
        LocalDate startDate = LocalDate.now().minusMonths(3);
        LocalDate endDate = LocalDate.now();
        
        var leaveRequests = leaveRequestRepository.findByDepartmentAndDateRange(
                department, startDate, endDate);
        
        Map<String, Long> byType = new HashMap<>();
        Map<String, Long> byMonth = new HashMap<>();
        
        leaveRequests.forEach(leave -> {
            String type = leave.getLeaveType().name(); // Convert enum to String
            byType.put(type, byType.getOrDefault(type, 0L) + 1);
            
            String month = leave.getStartDate().getMonth().toString();
            byMonth.put(month, byMonth.getOrDefault(month, 0L) + 1);
        });
        
        return new LeavePatterns(byType, byMonth, leaveRequests.size());
    }

    /**
     * Get performance metrics
     */
    public TeamPerformanceMetrics getPerformanceMetrics(Department department) {
        List<User> teamMembers = userRepository.findByDepartment(department);
        
        // Calculate average performance score
        double avgScore = teamMembers.stream()
                .mapToDouble(u -> 75.0) // Placeholder - should get from performance reviews
                .average()
                .orElse(0.0);
        
        // Goals completion rate
        long totalGoals = teamGoalRepository.findByDepartment(department).size();
        long completedGoals = teamGoalRepository.countCompletedGoalsByDepartment(department);
        double completionRate = totalGoals > 0 ? (completedGoals * 100.0 / totalGoals) : 0;
        
        return new TeamPerformanceMetrics(
                avgScore,
                completionRate,
                totalGoals,
                completedGoals
        );
    }

    // DTOs
    public record TeamOverview(
            long totalMembers,
            long activeMembers,
            long presentToday,
            double attendanceRate,
            long pendingLeaves,
            long activeGoals,
            double avgGoalProgress
    ) {}

    public record AttendanceTrend(
            LocalDate date,
            long present,
            long total,
            double rate
    ) {}

    public record LeavePatterns(
            Map<String, Long> byType,
            Map<String, Long> byMonth,
            long totalLeaves
    ) {}

    public record TeamPerformanceMetrics(
            double avgPerformanceScore,
            double goalsCompletionRate,
            long totalGoals,
            long completedGoals
    ) {}
}


