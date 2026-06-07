package com.example.hr.controllers;

import com.example.hr.models.*;
import com.example.hr.service.AuthUserHelper;
import com.example.hr.service.SelfServicePortalService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;

@Controller
@RequestMapping("/self-service-enhanced")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class SelfServiceEnhancedController {
    
    private final SelfServicePortalService selfServiceService;
    private final AuthUserHelper authUserHelper;
    
    // ===== Profile Management =====
    
    @GetMapping("/profile")
    public String myProfile(Authentication auth, Model model) {
        User user = authUserHelper.getCurrentUser(auth);
        EmployeeProfile profile = selfServiceService.getOrCreateProfile(user);
        
        model.addAttribute("user", user);
        model.addAttribute("profile", profile);
        
        return "self-service/profile";
    }
    
    @PostMapping("/profile/update")
    public String updateProfile(@ModelAttribute EmployeeProfile profileData,
                               Authentication auth,
                               RedirectAttributes ra) {
        try {
            User user = authUserHelper.getCurrentUser(auth);
            selfServiceService.updateProfile(user, profileData);
            ra.addFlashAttribute("success", "Cáº­p nháº­t há»“ sÆ¡ thÃ nh cÃ´ng!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/self-service-enhanced/profile";
    }
    
    // ===== Expense Claims =====
    
    @GetMapping("/expenses")
    public String myExpenses(Authentication auth, Model model) {
        User user = authUserHelper.getCurrentUser(auth);
        
        model.addAttribute("claims", selfServiceService.getUserExpenseClaims(user));
        
        return "self-service/expenses";
    }
    
    @GetMapping("/expense/new")
    public String newExpenseForm(Model model) {
        return "self-service/expense-form";
    }
    
    @PostMapping("/expense/submit")
    public String submitExpense(@RequestParam String category,
                               @RequestParam String title,
                               @RequestParam String description,
                               @RequestParam BigDecimal amount,
                               @RequestParam String expenseDate,
                               @RequestParam(required = false) String receiptUrl,
                               Authentication auth,
                               RedirectAttributes ra) {
        try {
            User user = authUserHelper.getCurrentUser(auth);
            selfServiceService.createExpenseClaim(user, category, title, description, 
                                                 amount, LocalDate.parse(expenseDate), receiptUrl);
            ra.addFlashAttribute("success", "Gá»­i Ä‘Æ¡n hoÃ n tiá»n thÃ nh cÃ´ng!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/self-service-enhanced/expenses";
    }
    
    // ===== Benefits =====
    
    @GetMapping("/benefits")
    public String benefits(Authentication auth, Model model) {
        User user = authUserHelper.getCurrentUser(auth);
        
        model.addAttribute("availablePlans", selfServiceService.getActiveBenefitPlans());
        model.addAttribute("myBenefits", selfServiceService.getUserBenefits(user));
        
        return "self-service/benefits";
    }
    
    @PostMapping("/benefit/enroll/{planId}")
    public String enrollBenefit(@PathVariable Integer planId,
                               @RequestParam String effectiveDate,
                               Authentication auth,
                               RedirectAttributes ra) {
        try {
            User user = authUserHelper.getCurrentUser(auth);
            BenefitPlan plan = selfServiceService.getActiveBenefitPlans().stream()
                .filter(p -> p.getId().equals(planId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Benefit plan not found"));
            
            selfServiceService.enrollInBenefit(user, plan, LocalDate.parse(effectiveDate));
            ra.addFlashAttribute("success", "ÄÄƒng kÃ½ phÃºc lá»£i thÃ nh cÃ´ng!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/self-service-enhanced/benefits";
    }
    
    // ===== Admin - Expense Approval =====
    
    @GetMapping("/admin/expenses")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public String adminExpenses(Model model) {
        model.addAttribute("pendingClaims", selfServiceService.getPendingClaims());
        model.addAttribute("totalPending", selfServiceService.getTotalPendingAmount());
        
        return "self-service/admin/expense-approval";
    }
    
    @PostMapping("/admin/expense/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public String approveClaim(@PathVariable Integer id, 
                              Authentication auth,
                              RedirectAttributes ra) {
        try {
            User approver = authUserHelper.getCurrentUser(auth);
            selfServiceService.approveClaim(id, approver);
            ra.addFlashAttribute("success", "ÄÃ£ phÃª duyá»‡t Ä‘Æ¡n hoÃ n tiá»n!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/self-service-enhanced/admin/expenses";
    }
    
    @PostMapping("/admin/expense/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public String rejectClaim(@PathVariable Integer id,
                             @RequestParam String reason,
                             Authentication auth,
                             RedirectAttributes ra) {
        try {
            User approver = authUserHelper.getCurrentUser(auth);
            selfServiceService.rejectClaim(id, approver, reason);
            ra.addFlashAttribute("success", "ÄÃ£ tá»« chá»‘i Ä‘Æ¡n hoÃ n tiá»n!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/self-service-enhanced/admin/expenses";
    }
}


