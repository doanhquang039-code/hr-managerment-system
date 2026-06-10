package com.example.hr.controllers;

import com.example.hr.service.GroupAccessService;
import com.example.hr.service.HrAuditLogService;
import org.springframework.data.domain.PageRequest;
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
@PreAuthorize("hasRole('ADMIN') or @groupAccessService.isCurrentUserAdmin()")
public class AdminGroupController {

    private final GroupAccessService groupAccessService;
    private final HrAuditLogService hrAuditLogService;

    public AdminGroupController(GroupAccessService groupAccessService,
                                HrAuditLogService hrAuditLogService) {
        this.groupAccessService = groupAccessService;
        this.hrAuditLogService = hrAuditLogService;
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
        var enabledPermissionKeys = groupAccessService.getEnabledPermissionKeys(group);
        var enabledMemberPermissionKeys = groupAccessService.getEnabledMemberPermissionKeys(group);
        model.addAttribute("enabledPermissionKeys", enabledPermissionKeys);
        model.addAttribute("enabledMemberPermissionKeys", enabledMemberPermissionKeys);
        model.addAttribute("effectiveMemberCount", groupAccessService.getEffectiveMembers().size());
        model.addAttribute("enabledPermissionCount", enabledPermissionKeys.size() + enabledMemberPermissionKeys.size());
        model.addAttribute("permissionActivity", hrAuditLogService.findLogs("GROUP_ACCESS", PageRequest.of(0, 6)).getContent());
        return "admin/group-management";
    }

    @PostMapping("/save")
    public String saveGroup(@RequestParam(name = "memberIds", required = false) Set<Integer> memberIds,
                            @RequestParam(name = "rolePermissions", required = false) Set<String> rolePermissions,
                            @RequestParam(name = "memberPermissions", required = false) Set<String> memberPermissions,
                            RedirectAttributes redirectAttributes) {
        groupAccessService.updateDefaultGroup(memberIds, rolePermissions, memberPermissions);
        redirectAttributes.addFlashAttribute("successMsg", "Group roles, members and user permissions updated successfully.");
        return "redirect:/admin/groups";
    }
}


