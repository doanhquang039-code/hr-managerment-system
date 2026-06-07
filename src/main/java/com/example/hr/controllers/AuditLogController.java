package com.example.hr.controllers;

import com.example.hr.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/admin/audit-logs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AuditLogController {

    private final AuditLogService auditLogService;

    /**
     * List all audit logs
     */
    @GetMapping
    public String listAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Model model) {

        Pageable pageable = PageRequest.of(page, size);
        var logs = auditLogService.searchLogs(action, entityType, userId, startDate, endDate, pageable);

        model.addAttribute("logs", logs);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", logs.getTotalPages());
        model.addAttribute("action", action);
        model.addAttribute("entityType", entityType);
        model.addAttribute("userId", userId);

        // Activity statistics
        var activityStats = auditLogService.getActivityStatistics();
        model.addAttribute("activityStats", activityStats);

        return "admin/audit-logs";
    }

    /**
     * View audit log details
     */
    @GetMapping("/{id}")
    public String viewAuditLog(@PathVariable Integer id, Model model) {
        var log = auditLogService.getLogById(id);
        model.addAttribute("log", log);
        return "admin/audit-log-detail";
    }

    /**
     * Export audit logs
     */
    @GetMapping("/export")
    @ResponseBody
    public String exportAuditLogs(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        // TODO: Implement CSV/Excel export
        return "Export functionality coming soon";
    }

    /**
     * Get activity timeline
     */
    @GetMapping("/timeline")
    public String activityTimeline(
            @RequestParam(required = false) Integer userId,
            @RequestParam(defaultValue = "100") int limit,
            Model model) {

        var timeline = auditLogService.getActivityTimeline(userId, limit);
        model.addAttribute("timeline", timeline);
        model.addAttribute("userId", userId);

        return "admin/activity-timeline";
    }
}


