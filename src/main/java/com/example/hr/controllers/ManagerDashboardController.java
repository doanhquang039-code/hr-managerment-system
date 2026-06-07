package com.example.hr.controllers;


import com.example.hr.department.entity.Department;
import com.example.hr.service.TeamAnalyticsService;
import com.example.hr.service.TeamGoalService;
import com.example.hr.service.MeetingService;
import com.example.hr.service.TeamBudgetService;
import com.example.hr.service.AuthUserHelper;
import com.example.hr.models.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/manager")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MANAGER')")
public class ManagerDashboardController {

    private final TeamAnalyticsService teamAnalyticsService;
    private final TeamGoalService teamGoalService;
    private final MeetingService meetingService;
    private final TeamBudgetService teamBudgetService;
    private final AuthUserHelper authUserHelper;

    /**
     * Manager advanced dashboard with analytics
     */
    @GetMapping("/dashboard-advanced")
    public String dashboardAdvanced(Authentication authentication, Model model) {
        User manager = authUserHelper.getCurrentUser(authentication);
        if (manager == null) return "redirect:/login";

        if (manager.getDepartment() == null) {
            model.addAttribute("errorMessage", "Manager must be assigned to a department");
            return "error/403";
        }

        // Team overview
        var teamOverview = teamAnalyticsService.getTeamOverview(manager.getDepartment());
        model.addAttribute("teamOverview", teamOverview);

        // Active goals
        var activeGoals = teamGoalService.getActiveGoalsByManager(manager);
        model.addAttribute("activeGoals", activeGoals);

        // Upcoming meetings
        var upcomingMeetings = meetingService.getUpcomingMeetings(manager);
        model.addAttribute("upcomingMeetings", upcomingMeetings);

        // Current month budgets
        var currentBudgets = teamBudgetService.getCurrentMonthBudgets(manager);
        model.addAttribute("currentBudgets", currentBudgets);

        // Attendance trends (last 7 days)
        var attendanceTrends = teamAnalyticsService.getAttendanceTrends(manager.getDepartment(), 7);
        model.addAttribute("attendanceTrends", attendanceTrends);

        // Performance metrics
        var performanceMetrics = teamAnalyticsService.getPerformanceMetrics(manager.getDepartment());
        model.addAttribute("performanceMetrics", performanceMetrics);

        return "manager/dashboard-advanced";
    }

    /**
     * Team analytics page
     */
    @GetMapping("/analytics")
    public String analytics(Authentication authentication, Model model) {
        User manager = authUserHelper.getCurrentUser(authentication);
        if (manager == null) return "redirect:/login";
        
        if (manager.getDepartment() == null) {
            model.addAttribute("errorMessage", "Manager must be assigned to a department");
            return "error/403";
        }

        // Team overview
        var teamOverview = teamAnalyticsService.getTeamOverview(manager.getDepartment());
        model.addAttribute("teamOverview", teamOverview);

        // Attendance trends (last 30 days)
        var attendanceTrends = teamAnalyticsService.getAttendanceTrends(manager.getDepartment(), 30);
        model.addAttribute("attendanceTrends", attendanceTrends);

        // Leave patterns
        var leavePatterns = teamAnalyticsService.getLeavePatterns(manager.getDepartment());
        model.addAttribute("leavePatterns", leavePatterns);

        // Performance metrics
        var performanceMetrics = teamAnalyticsService.getPerformanceMetrics(manager.getDepartment());
        model.addAttribute("performanceMetrics", performanceMetrics);

        // Goal statistics
        var goalStats = teamGoalService.getGoalStatistics(manager.getDepartment());
        model.addAttribute("goalStats", goalStats);

        return "manager/analytics";
    }
}

