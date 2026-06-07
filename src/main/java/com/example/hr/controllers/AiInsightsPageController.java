package com.example.hr.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AiInsightsPageController {

    @GetMapping("/admin/ai-insights")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminAiInsights(Model model) {
        model.addAttribute("pageTitle", "AI HR Insights");
        model.addAttribute("roleView", "admin");
        model.addAttribute("showProviderStatus", true);
        return "ai/insights";
    }

    @GetMapping("/manager/ai-insights")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String managerAiInsights(Model model) {
        model.addAttribute("pageTitle", "Team AI Insights");
        model.addAttribute("roleView", "manager");
        model.addAttribute("showProviderStatus", false);
        return "ai/insights";
    }
}


