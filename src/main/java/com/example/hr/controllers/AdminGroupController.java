package com.example.hr.controllers;

import com.example.hr.service.GroupAccessService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.stream.Collectors;
import java.util.Set;

@Controller
@RequestMapping("/admin/groups")
@PreAuthorize("hasRole('ADMIN')")
public class AdminGroupController {

    private final GroupAccessService groupAccessService;

    public AdminGroupController(GroupAccessService groupAccessService) {
        this.groupAccessService = groupAccessService;
    }

    @GetMapping
    public String editGroup(Model model) {
        var group = groupAccessService.getDefaultGroup();
        model.addAttribute("group", group);
        model.addAttribute("memberIds", group.getMembers().stream()
                .map(user -> user.getId())
                .collect(Collectors.toSet()));
        model.addAttribute("roles", groupAccessService.getAllRoles());
        model.addAttribute("users", groupAccessService.getAssignableUsers());
        model.addAttribute("features", groupAccessService.getAllFeatures());
        model.addAttribute("enabledPermissionKeys", groupAccessService.getEnabledPermissionKeys(group));
        return "admin/group-management";
    }

    @PostMapping("/save")
    public String saveGroup(@RequestParam(name = "memberIds", required = false) Set<Integer> memberIds,
                            @RequestParam(name = "rolePermissions", required = false) Set<String> rolePermissions,
                            RedirectAttributes redirectAttributes) {
        groupAccessService.updateDefaultGroup(memberIds, rolePermissions);
        redirectAttributes.addFlashAttribute("successMsg", "Group roles and permissions updated successfully.");
        return "redirect:/admin/groups";
    }
}


