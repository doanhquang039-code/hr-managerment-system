package com.example.hr.api;

import com.example.hr.models.User;
import com.example.hr.service.AuthUserHelper;
import com.example.hr.service.HealthInsightService;
import com.example.hr.service.HealthInsightService.HealthInsightInput;
import com.example.hr.service.HealthInsightService.HealthInsightResult;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lifestyle")
@PreAuthorize("isAuthenticated()")
public class LifestyleApiController {

    private final AuthUserHelper authUserHelper;
    private final HealthInsightService healthInsightService;

    public LifestyleApiController(AuthUserHelper authUserHelper, HealthInsightService healthInsightService) {
        this.authUserHelper = authUserHelper;
        this.healthInsightService = healthInsightService;
    }

    @PostMapping("/health-insights")
    public ResponseEntity<HealthInsightResult> healthInsights(@RequestBody HealthInsightInput input,
                                                              Authentication authentication) {
        User user = authUserHelper.getCurrentUser(authentication);
        return ResponseEntity.ok(healthInsightService.analyze(user, input));
    }
}
