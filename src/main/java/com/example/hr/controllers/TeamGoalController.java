package com.example.hr.controllers;


import com.example.hr.department.entity.Department;
import com.example.hr.models.TeamGoal;
import com.example.hr.models.User;
import com.example.hr.service.AuthUserHelper;
import com.example.hr.service.TeamGoalService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/manager/goals")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
public class TeamGoalController {

    private final TeamGoalService teamGoalService;
    private final AuthUserHelper authUserHelper;

    /**
     * List team goals
     */
    @GetMapping
    public String listGoals(Authentication authentication, Model model) {
        User manager = authUserHelper.getCurrentUser(authentication);
        if (manager == null) return "redirect:/login";

        if (manager.getDepartment() == null) {
            model.addAttribute("errorMessage", "Manager must be assigned to a department");
            return "error/403";
        }

        var goals = teamGoalService.getGoalsByDepartment(manager.getDepartment());
        var goalStats = teamGoalService.getGoalStatistics(manager.getDepartment());
        
        model.addAttribute("goals", goals);
        model.addAttribute("goalStats", goalStats);
        model.addAttribute("manager", manager);

        return "manager/goals/list";
    }

    /**
     * Show create goal form
     */
    @GetMapping("/create")
    public String createGoalForm(Model model) {
        model.addAttribute("goal", new TeamGoal());
        return "manager/goals/create";
    }

    /**
     * Create new goal
     */
    @PostMapping("/create")
    public String createGoal(
            @ModelAttribute TeamGoal goal,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        
        try {
            User manager = authUserHelper.getCurrentUser(authentication);
            if (manager == null) throw new RuntimeException("Not authenticated");
            goal.setManager(manager);
            goal.setDepartment(manager.getDepartment());
            
            teamGoalService.createGoal(goal);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Goal created successfully: " + goal.getTitle());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                    "Failed to create goal: " + e.getMessage());
        }

        return "redirect:/manager/goals";
    }

    /**
     * Show edit goal form
     */
    @GetMapping("/edit/{id}")
    public String editGoalForm(@PathVariable Integer id, Model model) {
        try {
            var goal = teamGoalService.getGoalById(id);
            model.addAttribute("goal", goal);
            return "manager/goals/edit";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Goal not found");
            return "redirect:/manager/goals";
        }
    }

    /**
     * Update goal
     */
    @PostMapping("/edit/{id}")
    public String updateGoal(
            @PathVariable Integer id,
            @ModelAttribute TeamGoal goalData,
            RedirectAttributes redirectAttributes) {
        
        try {
            teamGoalService.updateGoal(id, goalData);
            redirectAttributes.addFlashAttribute("successMessage", "Goal updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                    "Failed to update goal: " + e.getMessage());
        }

        return "redirect:/manager/goals";
    }

    /**
     * Update goal progress
     */
    @PostMapping("/progress/{id}")
    public String updateProgress(
            @PathVariable Integer id,
            @RequestParam Integer currentValue,
            RedirectAttributes redirectAttributes) {
        
        try {
            teamGoalService.updateProgress(id, currentValue);
            redirectAttributes.addFlashAttribute("successMessage", "Progress updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                    "Failed to update progress: " + e.getMessage());
        }

        return "redirect:/manager/goals";
    }

    /**
     * Complete goal
     */
    @PostMapping("/complete/{id}")
    public String completeGoal(
            @PathVariable Integer id,
            RedirectAttributes redirectAttributes) {
        
        try {
            teamGoalService.completeGoal(id);
            redirectAttributes.addFlashAttribute("successMessage", "Goal marked as completed");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                    "Failed to complete goal: " + e.getMessage());
        }

        return "redirect:/manager/goals";
    }

    /**
     * Cancel goal
     */
    @PostMapping("/cancel/{id}")
    public String cancelGoal(
            @PathVariable Integer id,
            RedirectAttributes redirectAttributes) {
        
        try {
            teamGoalService.cancelGoal(id);
            redirectAttributes.addFlashAttribute("successMessage", "Goal cancelled");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                    "Failed to cancel goal: " + e.getMessage());
        }

        return "redirect:/manager/goals";
    }

    /**
     * View goal details
     */
    @GetMapping("/view/{id}")
    public String viewGoal(@PathVariable Integer id, Model model) {
        try {
            var goal = teamGoalService.getGoalById(id);
            model.addAttribute("goal", goal);
            return "manager/goals/view";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Goal not found");
            return "redirect:/manager/goals";
        }
    }
}

