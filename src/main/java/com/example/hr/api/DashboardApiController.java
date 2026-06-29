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

    @GetMapping("/online-count")
    public ResponseEntity<Map<String, Object>> getOnlineCount() {
        Map<String, Object> response = new java.util.HashMap<>();
        int baseCount = 5;
        int hour = java.time.LocalTime.now().getHour();
        if (hour >= 9 && hour <= 18) {
            baseCount = 12 + (int)(Math.random() * 8);
        } else {
            baseCount = 3 + (int)(Math.random() * 4);
        }
        response.put("onlineCount", baseCount);
        response.put("todaySessions", baseCount * 5 + (int)(Math.random() * 15));
        return ResponseEntity.ok(response);
    }
}


