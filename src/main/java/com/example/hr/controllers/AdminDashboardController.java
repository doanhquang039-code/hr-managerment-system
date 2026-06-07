package com.example.hr.controllers;

import com.example.hr.backup.service.BackupService;
import com.example.hr.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    private final SystemMonitorService systemMonitorService;
    private final DashboardService dashboardService;
    private final AuditLogService auditLogService;
    private final BackupService backupService;

    /**
     * Advanced admin dashboard
     */
    @GetMapping("/dashboard-advanced")
    public String advancedDashboard(Model model) {
        // System health metrics
        var systemHealth = systemMonitorService.getSystemHealth();
        model.addAttribute("systemHealth", systemHealth);

        // Business metrics
        var businessMetrics = dashboardService.getBusinessMetrics();
        model.addAttribute("businessMetrics", businessMetrics);

        // Recent audit logs
        var recentLogs = auditLogService.getRecentLogs(10);
        model.addAttribute("recentLogs", recentLogs);

        // Backup statistics
        var backupStats = backupService.getBackupStatistics();
        model.addAttribute("backupStats", backupStats);

        // Activity statistics
        var activityStats = auditLogService.getActivityStatistics();
        model.addAttribute("activityStats", activityStats);

        return "admin/dashboard-advanced";
    }

    /**
     * System monitor page
     */
    @GetMapping("/system-monitor")
    public String systemMonitor(Model model) {
        var systemHealth = systemMonitorService.getSystemHealth();
        var performanceMetrics = systemMonitorService.getPerformanceMetrics();
        var operationsMonitor = systemMonitorService.getOperationsMonitor();
        
        model.addAttribute("systemHealth", systemHealth);
        model.addAttribute("performanceMetrics", performanceMetrics);
        model.addAttribute("operationsMonitor", operationsMonitor);

        return "admin/system-monitor";
    }
}


