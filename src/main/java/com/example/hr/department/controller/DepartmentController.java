package com.example.hr.department.controller;

import com.example.hr.department.entity.Department;
import com.example.hr.models.User;
import com.example.hr.enums.Role;
import com.example.hr.enums.UserStatus;
import com.example.hr.user.repository.UserRepository;
import com.example.hr.department.repository.DepartmentRepository;
import com.example.hr.department.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.access.prepost.PreAuthorize;

@Controller
@RequestMapping("/admin/departments")
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public String list(@RequestParam(name = "keyword", required = false) String keyword,
                       Model model) {
        if (keyword != null && !keyword.isBlank()) {
            model.addAttribute("departments", departmentRepository.findByDepartmentNameContainingIgnoreCase(keyword));
        } else {
            model.addAttribute("departments", departmentService.getAllDepartments());
        }
        model.addAttribute("allDepartments", departmentService.getAllDepartments());
        model.addAttribute("keyword", keyword);
        return "admin/department-list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("department", new Department());
        addDepartmentFormData(model);
        return "admin/department-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute("department") Department dept,
                       @RequestParam(name = "managerId", required = false) Integer managerId,
                       Authentication authentication) {
        if (managerId != null) {
            userRepository.findById(managerId).ifPresent(dept::setManager);
        } else {
            dept.setManager(null);
        }

        departmentService.saveDepartment(dept, authentication);
        return "redirect:/admin/departments";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Integer id, Model model) {
        model.addAttribute("department", departmentService.getDepartmentById(id));
        addDepartmentFormData(model);
        return "admin/department-form";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            departmentService.deleteDepartment(id);
            redirectAttributes.addFlashAttribute("successMessage", "XÃ³a phÃ²ng ban thÃ nh cÃ´ng.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "ÄÃ£ xáº£y ra lá»—i khi xÃ³a phÃ²ng ban.");
        }
        return "redirect:/admin/departments";
    }

    @GetMapping("/{id}/members")
    public String viewDepartmentMembers(@PathVariable("id") Integer id,
                                        @RequestParam(name = "keyword", required = false) String keyword,
                                        @RequestParam(name = "role", required = false) String role,
                                        Model model) {
        Department department = departmentService.getDepartmentById(id);
        
        Role roleEnum = null;
        if (role != null && !role.isBlank()) {
            try {
                roleEnum = Role.valueOf(role.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Ignore invalid role
            }
        }
        
        List<User> members = userRepository.searchDepartmentMembers(department, keyword, roleEnum);

        model.addAttribute("department", department);
        model.addAttribute("members", members);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedRole", role);
        return "admin/department-members";
    }

    private void addDepartmentFormData(Model model) {
        model.addAttribute("managers", userRepository.findByRoleInAndStatus(
                List.of(Role.ADMIN, Role.MANAGER),
                UserStatus.ACTIVE));
    }
}


