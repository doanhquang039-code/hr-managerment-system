package com.example.hr.controllers;

import com.example.hr.models.*;
import com.example.hr.enums.UserStatus;
import com.example.hr.user.repository.UserRepository;
import com.example.hr.service.AuthUserHelper;
import com.example.hr.service.OnboardingOffboardingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
public class OnboardingController {
    
    private final OnboardingOffboardingService onboardingService;
    private final AuthUserHelper authUserHelper;
    private final UserRepository userRepository;
    
    // ===== User Views =====
    
    @GetMapping({"/onboarding/my-checklist", "/user1/my-checklist"})
    @PreAuthorize("isAuthenticated()")
    public String myChecklist(Authentication auth, Model model) {
        User user = authUserHelper.getCurrentUser(auth);
        
        model.addAttribute("checklist", onboardingService.getUserChecklist(user));
        model.addAttribute("pending", onboardingService.getPendingItems(user));
        model.addAttribute("completionPercentage", onboardingService.getCompletionPercentage(user));
        
        return "user1/my-checklist";
    }
    
    @PostMapping({"/onboarding/checklist/{id}/complete", "/user1/checklist/{id}/complete"})
    @PreAuthorize("isAuthenticated()")
    public String completeItem(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            onboardingService.completeItem(id);
            ra.addFlashAttribute("success", "ÄÃ£ hoÃ n thÃ nh nhiá»‡m vá»¥!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/user1/my-checklist";
    }
    
    // ===== Admin Views =====
    
    @GetMapping("/admin/onboarding/checklists")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public String adminChecklistList(@RequestParam(required = false) String status,
                                     @RequestParam(required = false) String category,
                                     Authentication auth,
                                     Model model) {
        var checklists = category != null && !category.isBlank()
                ? onboardingService.getChecklistsByCategory(category)
                : onboardingService.getChecklistsByStatus(status);
        if (checklists.isEmpty()) {
            var users = userRepository.findByStatus(UserStatus.ACTIVE);
            if (!users.isEmpty()) {
                User hr = authUserHelper.getCurrentUser(auth);
                onboardingService.createStandardOnboardingChecklist(users.get(0), hr != null ? hr : users.get(0));
                checklists = category != null && !category.isBlank()
                        ? onboardingService.getChecklistsByCategory(category)
                        : onboardingService.getChecklistsByStatus(status);
            }
        }
        model.addAttribute("checklists", checklists);
        model.addAttribute("stats", onboardingService.getChecklistStats());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("categories", java.util.List.of("PAPERWORK", "IT_SETUP", "INTRODUCTION", "TRAINING", "OTHER"));
        return "admin/checklist-list";
    }
    
    @GetMapping("/admin/onboarding/create-checklist")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public String createChecklistForm(Model model) {
        return "onboarding/admin/create-checklist";
    }
    
    @PostMapping("/admin/onboarding/create-standard-checklist")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public String createStandardChecklist(@RequestParam Integer userId,
                                         Authentication auth,
                                         RedirectAttributes ra) {
        try {
            User newEmployee = new User();
            newEmployee.setId(userId);
            
            User hr = authUserHelper.getCurrentUser(auth);
            onboardingService.createStandardOnboardingChecklist(newEmployee, hr);
            
            ra.addFlashAttribute("success", "Táº¡o checklist onboarding thÃ nh cÃ´ng!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/onboarding/create-checklist";
    }
    
    // ===== Exit Interview =====
    
    @GetMapping("/onboarding/exit-interview")
    @PreAuthorize("isAuthenticated()")
    public String exitInterviewForm(Authentication auth, Model model) {
        User user = authUserHelper.getCurrentUser(auth);
        var interview = onboardingService.getExitInterview(user);
        
        model.addAttribute("interview", interview.orElse(null));
        model.addAttribute("hasInterview", interview.isPresent());
        
        return "user1/exit-interview";
    }
    
    @PostMapping("/onboarding/exit-interview/submit")
    @PreAuthorize("isAuthenticated()")
    public String submitExitInterview(@RequestParam String reasonForLeaving,
                                     @RequestParam Integer satisfactionRating,
                                     @RequestParam String feedback,
                                     @RequestParam(required = false) String suggestions,
                                     @RequestParam Boolean wouldRecommend,
                                     Authentication auth,
                                     RedirectAttributes ra) {
        try {
            User user = authUserHelper.getCurrentUser(auth);
            var existingInterview = onboardingService.getExitInterview(user);
            
            if (existingInterview.isEmpty()) {
                ExitInterview interview = onboardingService.createExitInterview(user, LocalDate.now(), null);
                onboardingService.updateExitInterview(interview.getId(), reasonForLeaving, 
                    satisfactionRating, feedback, suggestions, wouldRecommend, false, null);
            } else {
                onboardingService.updateExitInterview(existingInterview.get().getId(), 
                    reasonForLeaving, satisfactionRating, feedback, suggestions, wouldRecommend, false, null);
            }
            
            ra.addFlashAttribute("success", "Cáº£m Æ¡n báº¡n Ä‘Ã£ chia sáº»!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/onboarding/exit-interview";
    }
    
    @GetMapping("/admin/onboarding/exit-interviews")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public String exitInterviewList(Model model) {
        model.addAttribute("interviews", onboardingService.getAllExitInterviews());
        model.addAttribute("avgSatisfaction", onboardingService.getAverageSatisfactionRating());
        model.addAttribute("recommendationRate", onboardingService.getRecommendationRate());
        
        return "onboarding/admin/exit-interview-list";
    }
}


