package com.example.hr.controllers;

import com.example.hr.models.GroupRole;
import com.example.hr.service.GroupRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/group-roles")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
@RequiredArgsConstructor
public class AdminGroupRoleController {

    private final GroupRoleService groupRoleService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("roles", groupRoleService.findAll());
        return "admin/group-role-list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("formTitle", "Tạo Role mới");
        model.addAttribute("formAction", "/admin/group-roles/create");
        model.addAttribute("role", new GroupRole());
        return "admin/group-role-form";
    }

    @PostMapping("/create")
    public String create(@RequestParam String name,
                         @RequestParam String displayName,
                         @RequestParam(required = false) String color,
                         @RequestParam(required = false) String textColor,
                         @RequestParam(defaultValue = "99") int sortOrder,
                         RedirectAttributes ra) {
        try {
            groupRoleService.create(name, displayName, color, textColor, sortOrder);
            ra.addFlashAttribute("successMsg", "Tạo role '" + displayName + "' thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/group-roles";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Integer id, Model model, RedirectAttributes ra) {
        return groupRoleService.findById(id)
                .map(role -> {
                    model.addAttribute("formTitle", "Sửa Role: " + role.getDisplayName());
                    model.addAttribute("formAction", "/admin/group-roles/" + id + "/update");
                    model.addAttribute("role", role);
                    return "admin/group-role-form";
                })
                .orElseGet(() -> {
                    ra.addFlashAttribute("errorMsg", "Không tìm thấy role id=" + id);
                    return "redirect:/admin/group-roles";
                });
    }

    @PostMapping("/{id}/update")
    public String update(@PathVariable Integer id,
                         @RequestParam String displayName,
                         @RequestParam(required = false) String color,
                         @RequestParam(required = false) String textColor,
                         @RequestParam(defaultValue = "99") int sortOrder,
                         RedirectAttributes ra) {
        try {
            groupRoleService.update(id, displayName, color, textColor, sortOrder);
            ra.addFlashAttribute("successMsg", "Cập nhật role thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/group-roles";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            groupRoleService.delete(id);
            ra.addFlashAttribute("successMsg", "Đã xóa role.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/group-roles";
    }
}
