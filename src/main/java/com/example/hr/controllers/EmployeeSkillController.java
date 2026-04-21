package com.example.hr.controllers;

import com.example.hr.enums.SkillLevel;
import com.example.hr.models.EmployeeSkill;
import com.example.hr.models.User;
import com.example.hr.repository.UserRepository;
import com.example.hr.service.AuthUserHelper;
import com.example.hr.service.EmployeeSkillService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

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

        model.addAttribute("skills", skills);
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("skillLevels", SkillLevel.values());
        model.addAttribute("selectedUserId", userId);
        model.addAttribute("selectedCategory", category);
        return "admin/skill-list";
    }

    @GetMapping("/admin/skills/add")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String showAddForm(Model model) {
        model.addAttribute("skill", new EmployeeSkill());
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("skillLevels", SkillLevel.values());
        return "admin/skill-form";
    }

    @GetMapping("/admin/skills/edit/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String showEditForm(@PathVariable Integer id, Model model) {
        EmployeeSkill skill = employeeSkillService.findById(id)
                .orElseThrow(() -> new RuntimeException("Skill không tồn tại"));
        model.addAttribute("skill", skill);
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("skillLevels", SkillLevel.values());
        return "admin/skill-form";
    }

    @PostMapping("/admin/skills/save")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String save(@ModelAttribute EmployeeSkill skill,
                       @RequestParam Integer userId,
                       Authentication auth,
                       RedirectAttributes ra) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));
        skill.setUser(user);

        User verifier = authUserHelper.getCurrentUser(auth);
        if (verifier != null) skill.setVerifiedBy(verifier);

        employeeSkillService.save(skill);
        ra.addFlashAttribute("success", "Lưu kỹ năng thành công!");
        return "redirect:/admin/skills";
    }

    @GetMapping("/admin/skills/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@PathVariable Integer id, RedirectAttributes ra) {
        employeeSkillService.delete(id);
        ra.addFlashAttribute("success", "Đã xóa kỹ năng!");
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
        ra.addFlashAttribute("success", "Đã thêm kỹ năng thành công!");
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
        ra.addFlashAttribute("success", "Đã xóa kỹ năng!");
        return "redirect:/user1/skills";
    }
}
