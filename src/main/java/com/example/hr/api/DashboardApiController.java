package com.example.hr.api;

import com.example.hr.dto.DashboardStatsDTO;
import com.example.hr.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API controller cho Dashboard chÃ­nh.
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardApiController {

    private final DashboardService dashboardService;

    public DashboardApiController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> getStats() {
        return ResponseEntity.ok(dashboardService.buildDashboardStats());
    }

    @GetMapping("/activity")
    public ResponseEntity<List<Map<String, Object>>> getRecentActivity(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(dashboardService.getRecentActivity(limit));
    }

    @GetMapping("/attrition/{year}")
    public ResponseEntity<Map<String, Object>> getAttrition(@PathVariable int year) {
        return ResponseEntity.ok(dashboardService.calculateAttritionMetrics(year));
    }
}


