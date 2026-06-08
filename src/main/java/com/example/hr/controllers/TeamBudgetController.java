package com.example.hr.controllers;


import com.example.hr.department.entity.Department;
import com.example.hr.models.TeamBudget;
import com.example.hr.models.User;
import com.example.hr.service.AuthUserHelper;
import com.example.hr.service.TeamBudgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/manager/budget")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
public class TeamBudgetController {

    private final TeamBudgetService teamBudgetService;
    private final AuthUserHelper authUserHelper;

    /**
     * List team budgets
     */
    @GetMapping
    public String listBudgets(Authentication authentication, Model model) {
        User manager = authUserHelper.getCurrentUser(authentication);
        if (manager == null) return "redirect:/login";

        int currentYear = LocalDateTime.now().getYear();
        int currentMonth = LocalDateTime.now().getMonthValue();
        var budgets = manager.getDepartment() == null
                ? teamBudgetService.getAllBudgets()
                : teamBudgetService.getBudgetsByManager(manager);
        var currentBudgets = manager.getDepartment() == null
                ? budgets.stream()
                        .filter(b -> b.getYear() != null && b.getYear() == currentYear
                                && b.getMonth() != null && b.getMonth() == currentMonth)
                        .toList()
                : teamBudgetService.getCurrentMonthBudgets(manager);
        var budgetStats = manager.getDepartment() == null
                ? teamBudgetService.getBudgetStatistics(currentYear)
                : teamBudgetService.getBudgetStatistics(manager.getDepartment(), currentYear);
        
        model.addAttribute("budgets", budgets);
        model.addAttribute("currentBudgets", currentBudgets);
        model.addAttribute("budgetStats", budgetStats);
        model.addAttribute("currentYear", currentYear);

        return "manager/budget/list";
    }

    /**
     * Show create budget form
     */
    @GetMapping("/create")
    public String createBudgetForm(Model model) {
        model.addAttribute("budget", new TeamBudget());
        return "manager/budget/create";
    }

    /**
     * Create new budget
     */
    @PostMapping("/create")
    public String createBudget(
            @ModelAttribute TeamBudget budget,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        
        try {
            User manager = authUserHelper.getCurrentUser(authentication);
            if (manager == null) throw new RuntimeException("Not authenticated");
            budget.setManager(manager);
            budget.setDepartment(manager.getDepartment());
            
            teamBudgetService.createBudget(budget);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Budget created successfully for " + budget.getCategory());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                    "Failed to create budget: " + e.getMessage());
        }

        return "redirect:/manager/budget";
    }

    /**
     * Show edit budget form
     */
    @GetMapping("/edit/{id}")
    public String editBudgetForm(@PathVariable Integer id, Model model) {
        try {
            var budget = teamBudgetService.getBudgetById(id);
            model.addAttribute("budget", budget);
            return "manager/budget/edit";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Budget not found");
            return "redirect:/manager/budget";
        }
    }

    /**
     * Update budget
     */
    @PostMapping("/edit/{id}")
    public String updateBudget(
            @PathVariable Integer id,
            @ModelAttribute TeamBudget budgetData,
            RedirectAttributes redirectAttributes) {
        
        try {
            teamBudgetService.updateBudget(id, budgetData);
            redirectAttributes.addFlashAttribute("successMessage", "Budget updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                    "Failed to update budget: " + e.getMessage());
        }

        return "redirect:/manager/budget";
    }

    /**
     * Add spending to budget
     */
    @PostMapping("/spend/{id}")
    public String addSpending(
            @PathVariable Integer id,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String description,
            RedirectAttributes redirectAttributes) {
        
        try {
            teamBudgetService.updateSpending(id, amount);
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Spending of $" + amount + " added successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                    "Failed to add spending: " + e.getMessage());
        }

        return "redirect:/manager/budget";
    }

    /**
     * Close budget
     */
    @PostMapping("/close/{id}")
    public String closeBudget(
            @PathVariable Integer id,
            RedirectAttributes redirectAttributes) {
        
        try {
            teamBudgetService.closeBudget(id);
            redirectAttributes.addFlashAttribute("successMessage", "Budget closed successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                    "Failed to close budget: " + e.getMessage());
        }

        return "redirect:/manager/budget";
    }

    /**
     * View budget details
     */
    @GetMapping("/view/{id}")
    public String viewBudget(@PathVariable Integer id, Model model) {
        try {
            var budget = teamBudgetService.getBudgetById(id);
            model.addAttribute("budget", budget);
            return "manager/budget/view";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Budget not found");
            return "redirect:/manager/budget";
        }
    }

    /**
     * Budget analytics
     */
    @GetMapping("/analytics")
    public String budgetAnalytics(Authentication authentication, Model model) {
        User manager = authUserHelper.getCurrentUser(authentication);
        if (manager == null) return "redirect:/login";

        int currentYear = LocalDateTime.now().getYear();
        var budgetStats = manager.getDepartment() == null
                ? teamBudgetService.getBudgetStatistics(currentYear)
                : teamBudgetService.getBudgetStatistics(manager.getDepartment(), currentYear);
        var allBudgets = manager.getDepartment() == null
                ? teamBudgetService.getAllBudgets()
                : teamBudgetService.getBudgetsByDepartment(manager.getDepartment());
        
        model.addAttribute("budgetStats", budgetStats);
        model.addAttribute("allBudgets", allBudgets);
        model.addAttribute("currentYear", currentYear);

        return "manager/budget/analytics";
    }
}

