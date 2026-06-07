package com.example.hr.controllers;

import com.example.hr.enums.KpiStatus;
import com.example.hr.department.entity.Department;
import com.example.hr.models.KpiGoal;
import com.example.hr.models.User;
import com.example.hr.department.repository.DepartmentRepository;
import com.example.hr.user.repository.UserRepository;
import com.example.hr.service.AuthUserHelper;
import com.example.hr.service.KpiGoalService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class KpiGoalController {

    private final KpiGoalService kpiGoalService;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final AuthUserHelper authUserHelper;

    public KpiGoalController(KpiGoalService kpiGoalService,
                              UserRepository userRepository,
                              DepartmentRepository departmentRepository,
                              AuthUserHelper authUserHelper) {
        this.kpiGoalService = kpiGoalService;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.authUserHelper = authUserHelper;
    }

    // ==================== ADMIN ====================

    @GetMapping("/admin/kpi")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String adminList(@RequestParam(required = false) String status,
                            @RequestParam(required = false) Integer userId,
                            Model model) {
        List<KpiGoal> goals;
        if (userId != null) {
            goals = kpiGoalService.findByUser(userId);
        } else if (status != null && !status.isBlank()) {
            goals = kpiGoalService.findByStatus(KpiStatus.valueOf(status));
        } else {
            goals = kpiGoalService.findAll();
        }

        model.addAttribute("goals", goals);
        model.addAttribute("goalRows", goals.stream().map(KpiGoalRow::from).collect(Collectors.toList()));
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("departments", departmentRepository.findAll());
        model.addAttribute("statuses", KpiStatus.values());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedUserId", userId);
        model.addAttribute("countActive", kpiGoalService.countByStatus(KpiStatus.ACTIVE));
        model.addAttribute("countCompleted", kpiGoalService.countByStatus(KpiStatus.COMPLETED));
        model.addAttribute("countCancelled", kpiGoalService.countByStatus(KpiStatus.CANCELED));
        return "admin/kpi-list";
    }

    public record KpiGoalRow(Integer id, String employeeName, String departmentName, String title,
                             String category, String progress, String deadline, String status,
                             String statusClass) {
        static KpiGoalRow from(KpiGoal goal) {
            String employeeName = goal.getUser() != null ? goal.getUser().getFullName() : "N/A";
            String departmentName = goal.getUser() != null && goal.getUser().getDepartment() != null
                    ? goal.getUser().getDepartment().getDepartmentName() : "";
            String current = goal.getCurrentValue() != null ? goal.getCurrentValue().stripTrailingZeros().toPlainString() : "0";
            String target = goal.getTargetValue() != null ? goal.getTargetValue().stripTrailingZeros().toPlainString() : "0";
            String unit = goal.getUnit() != null ? " " + goal.getUnit() : "";
            String progress = current + " / " + target + unit;
            if (goal.getAchievementPct() != null) {
                progress += " (" + goal.getAchievementPct().stripTrailingZeros().toPlainString() + "%)";
            }
            String status = switch (goal.getStatus()) {
                case ACTIVE -> "Active";
                case COMPLETED -> "HoÃ n thÃ nh";
                case FAILED -> "KhÃ´ng Ä‘áº¡t";
                case CANCELED -> "ÄÃ£ há»§y";
                case DRAFT -> "NhÃ¡p";
            };
            String statusClass = switch (goal.getStatus()) {
                case ACTIVE -> "status-active";
                case COMPLETED -> "status-completed";
                case FAILED, CANCELED -> "status-canceled";
                case DRAFT -> "";
            };
            return new KpiGoalRow(goal.getId(), employeeName, departmentName, goal.getGoalTitle(),
                    goal.getCategory(), progress, String.valueOf(goal.getEndDate()), status, statusClass);
        }
    }

    @GetMapping("/admin/kpi/add")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String showAddForm(Model model) {
        model.addAttribute("goal", new KpiGoal());
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("departments", departmentRepository.findAll());
        model.addAttribute("statuses", KpiStatus.values());
        return "admin/kpi-form";
    }

    @GetMapping("/admin/kpi/edit/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String showEditForm(@PathVariable Integer id, Model model) {
        KpiGoal goal = kpiGoalService.findById(id)
                .orElseThrow(() -> new RuntimeException("KPI Goal khÃ´ng tá»“n táº¡i"));
        model.addAttribute("goal", goal);
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("departments", departmentRepository.findAll());
        model.addAttribute("statuses", KpiStatus.values());
        return "admin/kpi-form";
    }

    @PostMapping("/admin/kpi/save")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String save(@ModelAttribute KpiGoal goal,
                       @RequestParam Integer userId,
                       @RequestParam(required = false) Integer departmentId,
                       Authentication auth,
                       RedirectAttributes ra) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User khÃ´ng tá»“n táº¡i"));
        goal.setUser(user);

        if (departmentId != null) {
            departmentRepository.findById(departmentId).ifPresent(goal::setDepartment);
        }

        User creator = authUserHelper.getCurrentUser(auth);
        if (goal.getId() == null && creator != null) {
            goal.setCreatedBy(creator);
        }

        kpiGoalService.save(goal);
        ra.addFlashAttribute("success", "LÆ°u KPI Goal thÃ nh cÃ´ng!");
        return "redirect:/admin/kpi";
    }

    @PostMapping("/admin/kpi/update-progress/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String updateProgress(@PathVariable Integer id,
                                 @RequestParam BigDecimal currentValue,
                                 RedirectAttributes ra) {
        kpiGoalService.updateProgress(id, currentValue);
        ra.addFlashAttribute("success", "Cáº­p nháº­t tiáº¿n Ä‘á»™ KPI thÃ nh cÃ´ng!");
        return "redirect:/admin/kpi";
    }

    @GetMapping("/admin/kpi/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@PathVariable Integer id, RedirectAttributes ra) {
        kpiGoalService.delete(id);
        ra.addFlashAttribute("success", "ÄÃ£ xÃ³a KPI Goal!");
        return "redirect:/admin/kpi";
    }

    // ==================== USER ====================

    @GetMapping("/user1/kpi")
    @PreAuthorize("isAuthenticated()")
    public String userKpi(Authentication auth, Model model) {
        User currentUser = authUserHelper.getCurrentUser(auth);
        if (currentUser == null) return "redirect:/login";

        List<KpiGoal> myGoals = kpiGoalService.findByUser(currentUser.getId());
        List<KpiGoal> activeGoals = kpiGoalService.findActiveByUser(currentUser.getId());
        Double avgAchievement = kpiGoalService.getAvgAchievement(currentUser.getId());

        model.addAttribute("myGoals", myGoals);
        model.addAttribute("activeGoals", activeGoals);
        model.addAttribute("avgAchievement", avgAchievement);
        model.addAttribute("currentUser", currentUser);
        return "user1/kpi";
    }
}


