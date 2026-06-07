package com.example.hr.controllers;

import com.example.hr.dto.AnalyticsDashboardDTO;
import com.example.hr.service.AdvancedAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/admin/analytics")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'HR')")
public class AdvancedAnalyticsController {
    
    private final AdvancedAnalyticsService analyticsService;
    
    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        model.addAttribute("pageTitle", "Advanced Analytics Dashboard");
        return "admin/analytics-dashboard";
    }
    
    @GetMapping("/api/dashboard-data")
    @ResponseBody
    public AnalyticsDashboardDTO getDashboardData() {
        return analyticsService.getDashboardData();
    }
}


