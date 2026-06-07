package com.example.hr.api;

import com.example.hr.models.*;
import com.example.hr.service.AuthUserHelper;
import com.example.hr.service.OnboardingOffboardingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/onboarding")
@RequiredArgsConstructor
public class OnboardingApiController {
    
    private final OnboardingOffboardingService onboardingService;
    private final AuthUserHelper authUserHelper;
    
    @GetMapping("/my-checklist")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMyChecklist(Authentication auth) {
        User user = authUserHelper.getCurrentUser(auth);
        List<OnboardingChecklist> checklist = onboardingService.getUserChecklist(user);
        double completionPercent = onboardingService.getCompletionPercentage(user);
        
        Map<String, Object> response = new HashMap<>();
        response.put("checklist", checklist);
        response.put("completionPercent", completionPercent);
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/checklist/{itemId}/complete")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> completeChecklistItem(@PathVariable Integer itemId) {
        try {
            OnboardingChecklist item = onboardingService.completeItem(itemId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "ÄÃ£ hoÃ n thÃ nh task");
            response.put("item", item);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PostMapping("/checklist/create-standard")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<?> createStandardChecklist(@RequestParam Integer userId, Authentication auth) {
        try {
            User user = new User();
            user.setId(userId);
            
            User hr = authUserHelper.getCurrentUser(auth);
            
            onboardingService.createStandardOnboardingChecklist(user, hr);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "ÄÃ£ táº¡o checklist chuáº©n");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PostMapping("/exit-interview/submit")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> submitExitInterview(@RequestBody Map<String, Object> request,
                                                 Authentication auth) {
        try {
            User user = authUserHelper.getCurrentUser(auth);
            
            java.time.LocalDate interviewDate = java.time.LocalDate.now();
            User interviewer = authUserHelper.getCurrentUser(auth); // Can be changed to HR user
            
            ExitInterview interview = onboardingService.createExitInterview(user, interviewDate, interviewer);
            
            // Update with submitted data
            String reasonForLeaving = request.get("reason").toString();
            Integer satisfactionRating = Integer.parseInt(request.get("satisfactionRating").toString());
            String feedback = request.getOrDefault("feedback", "").toString();
            String suggestions = request.getOrDefault("suggestions", "").toString();
            Boolean wouldRecommend = Boolean.parseBoolean(request.get("wouldRecommend").toString());
            Boolean wouldRehire = Boolean.parseBoolean(request.getOrDefault("wouldRehire", "false").toString());
            String responses = request.getOrDefault("responses", "").toString();
            
            interview = onboardingService.updateExitInterview(interview.getId(), reasonForLeaving, 
                satisfactionRating, feedback, suggestions, wouldRecommend, wouldRehire, responses);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cáº£m Æ¡n báº¡n Ä‘Ã£ chia sáº»");
            response.put("interview", interview);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @GetMapping("/exit-interview/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<?> getExitInterviewStats() {
        Double avgSatisfaction = onboardingService.getAverageSatisfactionRating();
        Double recommendRate = onboardingService.getRecommendationRate();
        
        Map<String, Object> response = new HashMap<>();
        response.put("averageSatisfaction", avgSatisfaction);
        response.put("recommendationRate", recommendRate);
        
        return ResponseEntity.ok(response);
    }
}


