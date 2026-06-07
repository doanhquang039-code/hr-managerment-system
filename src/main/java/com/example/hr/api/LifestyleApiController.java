package com.example.hr.api;

import com.example.hr.kafka.events.HealthInsightEvent;
import com.example.hr.kafka.producer.HREventProducer;
import com.example.hr.models.User;
import com.example.hr.service.AuthUserHelper;
import com.example.hr.service.HealthInsightService;
import com.example.hr.service.HealthInsightService.HealthInsightInput;
import com.example.hr.service.HealthInsightService.HealthInsightResult;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/lifestyle")
@PreAuthorize("isAuthenticated()")
public class LifestyleApiController {

    private final AuthUserHelper authUserHelper;
    private final HealthInsightService healthInsightService;
    private final ObjectProvider<HREventProducer> eventProducerProvider;

    public LifestyleApiController(AuthUserHelper authUserHelper,
                                  HealthInsightService healthInsightService,
                                  ObjectProvider<HREventProducer> eventProducerProvider) {
        this.authUserHelper = authUserHelper;
        this.healthInsightService = healthInsightService;
        this.eventProducerProvider = eventProducerProvider;
    }

    @PostMapping("/health-insights")
    public ResponseEntity<HealthInsightResult> healthInsights(@RequestBody HealthInsightInput input,
                                                              Authentication authentication) {
        User user = authUserHelper.getCurrentUser(authentication);
        HealthInsightResult result = healthInsightService.analyze(user, input);
        publishHealthInsightEvent(user, input, result);
        return ResponseEntity.ok(result);
    }

    private void publishHealthInsightEvent(User user, HealthInsightInput input, HealthInsightResult result) {
        HREventProducer eventProducer = eventProducerProvider.getIfAvailable();
        if (eventProducer == null || user == null || result == null) {
            return;
        }

        try {
            HealthInsightEvent event = new HealthInsightEvent(
                    user.getId(),
                    user.getUsername(),
                    user.getRole() != null ? user.getRole().name() : "USER",
                    result.wellnessScore(),
                    result.riskLevel(),
                    result.flags(),
                    result.recommendations(),
                    input != null ? input.sleepHours() : null,
                    input != null ? input.stressLevel() : null,
                    input != null ? input.steps() : null,
                    input != null ? input.waterLiters() : null,
                    input != null ? input.overtimeHours() : null,
                    LocalDateTime.now()
            );
            eventProducer.publishHealthInsightEvent(event);
        } catch (Exception ignored) {
            // Health insight should still return to the UI if Kafka is unavailable.
        }
    }
}


