package com.example.hr.controllers;

import com.example.hr.enums.SkillLevel;
import com.example.hr.models.EmployeeSkill;
import com.example.hr.models.User;
import com.example.hr.user.repository.UserRepository;
import com.example.hr.service.AuthUserHelper;
import com.example.hr.service.EmployeeSkillService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class EmployeeSkillController {

    private final EmployeeSkillService employeeSkillService;
    private final UserRepository userRepository;
    private final AuthUserHelper authUserHelper;

    public EmployeeSkillController(EmployeeSkillService employeeSkillService,
                                    UserRepository userRepository,
                                    AuthUserHelper authUserHelper) {
        this.employeeSkillService = employeeSkillService;
        this.userRepository = userRepository;
        this.authUserHelper = authUserHelper;
    }

    // ==================== ADMIN ====================

    @GetMapping("/admin/skills")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String adminList(@RequestParam(required = false) Integer userId,
                            @RequestParam(required = false) String category,
                            Model model) {
        List<EmployeeSkill> skills;
        if (userId != null && category != null && !category.isBlank()) {
            skills = employeeSkillService.findByUserAndCategory(userId, category);
        } else if (userId != null) {
            skills = employeeSkillService.findByUser(userId);
        } else {
            skills = employeeSkillService.findAll();
        }

        List<UserOption> userOptions = userOptions();
        Map<Integer, String> userNames = userOptions.stream()
                .collect(Collectors.toMap(UserOption::id, UserOption::fullName));
        model.addAttribute("skills", skills.stream().map(skill -> SkillRow.from(skill, userNames)).toList());
        model.addAttribute("userOptions", userOptions);
        model.addAttribute("skillLevels", SkillLevel.values());
        model.addAttribute("selectedUserId", userId);
        model.addAttribute("selectedCategory", category);
        return "admin/skill-list";
    }

    @GetMapping("/admin/skills/add")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String showAddForm(Model model) {
        model.addAttribute("skill", new EmployeeSkill());
        model.addAttribute("userOptions", userOptions());
        model.addAttribute("skillLevels", SkillLevel.values());
        model.addAttribute("selectedUserId", null);
        return "admin/skill-form";
    }

    @GetMapping("/admin/skills/edit/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String showEditForm(@PathVariable Integer id, Model model) {
        EmployeeSkill skill = employeeSkillService.findById(id)
                .orElseThrow(() -> new RuntimeException("Skill khÃ´ng tá»“n táº¡i"));
        model.addAttribute("skill", skill);
        model.addAttribute("userOptions", userOptions());
        model.addAttribute("skillLevels", SkillLevel.values());
        model.addAttribute("selectedUserId", skill.getUser() != null ? skill.getUser().getId() : null);
        return "admin/skill-form";
    }

    @PostMapping("/admin/skills/save")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String save(@ModelAttribute EmployeeSkill skill,
                       @RequestParam Integer userId,
                       Authentication auth,
                       RedirectAttributes ra) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User khÃ´ng tá»“n táº¡i"));

        if (employeeSkillService.existsForAnotherSkill(userId, skill.getSkillName(), skill.getId())) {
            ra.addFlashAttribute("error", "NhÃ¢n viÃªn nÃ y Ä‘Ã£ cÃ³ ká»¹ nÄƒng \"" + skill.getSkillName() + "\".");
            return skill.getId() != null
                    ? "redirect:/admin/skills/edit/" + skill.getId()
                    : "redirect:/admin/skills/add";
        }

        skill.setUser(user);

        User verifier = authUserHelper.getCurrentUser(auth);
        if (verifier != null) skill.setVerifiedBy(verifier);

        try {
            employeeSkillService.save(skill);
            ra.addFlashAttribute("success", "LÆ°u ká»¹ nÄƒng thÃ nh cÃ´ng!");
            return "redirect:/admin/skills";
        } catch (DataIntegrityViolationException ex) {
            ra.addFlashAttribute("error", "KhÃ´ng lÆ°u Ä‘Æ°á»£c ká»¹ nÄƒng. Vui lÃ²ng kiá»ƒm tra dá»¯ liá»‡u hoáº·c ká»¹ nÄƒng bá»‹ trÃ¹ng.");
            return skill.getId() != null
                    ? "redirect:/admin/skills/edit/" + skill.getId()
                    : "redirect:/admin/skills/add";
        }
    }

    @GetMapping("/admin/skills/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@PathVariable Integer id, RedirectAttributes ra) {
        employeeSkillService.delete(id);
        ra.addFlashAttribute("success", "ÄÃ£ xÃ³a ká»¹ nÄƒng!");
        return "redirect:/admin/skills";
    }

    // ==================== USER ====================

    @GetMapping("/user1/skills")
    @PreAuthorize("isAuthenticated()")
    public String userSkills(Authentication auth, Model model) {
        User currentUser = authUserHelper.getCurrentUser(auth);
        if (currentUser == null) return "redirect:/login";

        List<EmployeeSkill> mySkills = employeeSkillService.findByUser(currentUser.getId());
        List<EmployeeSkill> certifiedSkills = employeeSkillService.findCertifiedByUser(currentUser.getId());

        model.addAttribute("mySkills", mySkills);
        model.addAttribute("certifiedSkills", certifiedSkills);
        model.addAttribute("newSkill", new EmployeeSkill());
        model.addAttribute("skillLevels", SkillLevel.values());
        model.addAttribute("currentUser", currentUser);
        return "user1/skills";
    }

    @PostMapping("/user1/skills/save")
    @PreAuthorize("isAuthenticated()")
    public String userSaveSkill(@ModelAttribute EmployeeSkill skill,
                                 Authentication auth,
                                 RedirectAttributes ra) {
        User currentUser = authUserHelper.getCurrentUser(auth);
        if (currentUser == null) return "redirect:/login";

        skill.setUser(currentUser);
        employeeSkillService.save(skill);
        ra.addFlashAttribute("success", "ÄÃ£ thÃªm ká»¹ nÄƒng thÃ nh cÃ´ng!");
        return "redirect:/user1/skills";
    }

    @GetMapping("/user1/skills/delete/{id}")
    @PreAuthorize("isAuthenticated()")
    public String userDeleteSkill(@PathVariable Integer id, Authentication auth, RedirectAttributes ra) {
        User currentUser = authUserHelper.getCurrentUser(auth);
        employeeSkillService.findById(id).ifPresent(s -> {
            if (s.getUser().getId().equals(currentUser.getId())) {
                employeeSkillService.delete(id);
            }
        });
        ra.addFlashAttribute("success", "ÄÃ£ xÃ³a ká»¹ nÄƒng!");
        return "redirect:/user1/skills";
    }

    private List<UserOption> userOptions() {
        return userRepository.findAll().stream()
                .map(user -> new UserOption(user.getId(), user.getFullName()))
                .toList();
    }

    public record UserOption(Integer id, String fullName) {
    }

    public record SkillRow(
            Integer id,
            Integer userId,
            String userFullName,
            String skillName,
            String skillCategory,
            SkillLevel skillLevel,
            java.math.BigDecimal yearsExp,
            Boolean isCertified,
            String certificateName) {

        static SkillRow from(EmployeeSkill skill, Map<Integer, String> userNames) {
            User user = skill.getUser();
            Integer userId = user != null ? user.getId() : null;
            return new SkillRow(
                    skill.getId(),
                    userId,
                    userNames.getOrDefault(userId, "N/A"),
                    skill.getSkillName(),
                    skill.getSkillCategory(),
                    skill.getSkillLevel(),
                    skill.getYearsExp(),
                    Boolean.TRUE.equals(skill.getIsCertified()),
                    skill.getCertificateName());
        }
    }
}


