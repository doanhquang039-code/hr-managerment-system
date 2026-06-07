package com.example.hr.controllers;

import com.example.hr.dto.AiAdvisorRequest;
import com.example.hr.dto.AiAdvisorResponse;
import com.example.hr.dto.AiAgentRequest;
import com.example.hr.dto.AiAgentResponse;
import com.example.hr.dto.AiStatusResponse;
import com.example.hr.dto.AiInsightResponse;
import com.example.hr.models.User;
import com.example.hr.service.AiAgentService;
import com.example.hr.service.AiHrInsightService;
import com.example.hr.service.AuthUserHelper;
import com.example.hr.service.GeminiAiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@PreAuthorize("isAuthenticated()")
public class AiOperationsController {

    @Value("${ai.gemini.enabled:false}")
    private boolean geminiEnabled;

    @Value("${ai.gemini.model:gemini-1.5-flash}")
    private String geminiModel;

    @Autowired(required = false)
    private GeminiAiService geminiAiService;

    @Autowired
    private AiHrInsightService aiHrInsightService;

    @Autowired
    private AiAgentService aiAgentService;

    @Autowired
    private AuthUserHelper authUserHelper;

    @GetMapping("/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AiStatusResponse> status() {
        boolean available = geminiAiService != null && geminiAiService.isAvailable();
        String mode = available ? "live" : "rule-based-fallback";
        String message = available
                ? "Gemini is configured and HR assistant can use live AI responses."
                : "Gemini is not configured; chatbot continues with local HR rules and database context.";

        return ResponseEntity.ok(AiStatusResponse.builder()
                .enabled(geminiEnabled)
                .available(available)
                .provider("google-gemini")
                .model(geminiAiService != null ? geminiAiService.getModel() : geminiModel)
                .mode(mode)
                .message(message)
                .build());
    }

    @GetMapping("/hr-insights")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<AiInsightResponse> hrInsights(Authentication authentication) {
        User actor = authUserHelper.getCurrentUser(authentication);
        if (actor == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(aiHrInsightService.generate(actor));
    }

    @PostMapping("/hr-advisor")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<AiAdvisorResponse> hrAdvisor(@RequestBody(required = false) AiAdvisorRequest request,
                                                       Authentication authentication) {
        User actor = authUserHelper.getCurrentUser(authentication);
        if (actor == null) {
            return ResponseEntity.status(401).build();
        }
        String question = request != null ? request.getQuestion() : null;
        return ResponseEntity.ok(aiHrInsightService.advise(actor, question));
    }

    @PostMapping("/agent")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<AiAgentResponse> agent(@RequestBody(required = false) AiAgentRequest request,
                                                 Authentication authentication) {
        User actor = authUserHelper.getCurrentUser(authentication);
        if (actor == null) {
            return ResponseEntity.status(401).build();
        }
        String query = request != null ? request.getQuery() : null;
        String mode = request != null ? request.getMode() : null;
        return ResponseEntity.ok(aiAgentService.run(actor, query, mode));
    }
}


