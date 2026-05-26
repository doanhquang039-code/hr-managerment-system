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

@Controller
@RequestMapping("/groups")
@PreAuthorize("hasRole('ADMIN') or @groupAccessService.hasCurrentUserAccess()")
public class GroupController {

    private final GroupAccessService groupAccessService;

    public GroupController(GroupAccessService groupAccessService) {
        this.groupAccessService = groupAccessService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or @groupAccessService.hasCurrentUserAccess()")
    public String dashboard(Model model) {
        model.addAttribute("group", groupAccessService.getDefaultGroup());
        model.addAttribute("hasGroupAccess", true);
        model.addAttribute("canUseDashboard", groupAccessService.hasFeature("DASHBOARD"));
        model.addAttribute("canUseMembers", groupAccessService.hasFeature("MEMBERS"));
        model.addAttribute("canUseNotes", groupAccessService.hasFeature("NOTES"));
        model.addAttribute("groupBasePath", "/groups");
        return "groups/dashboard";
    }

    @GetMapping("/members")
    @PreAuthorize("hasRole('ADMIN') or (@groupAccessService.hasCurrentUserAccess() and @groupAccessService.hasFeature('MEMBERS'))")
    public String members(Model model) {
        model.addAttribute("group", groupAccessService.getDefaultGroup());
        model.addAttribute("hasGroupAccess", true);
        model.addAttribute("canUseDashboard", groupAccessService.hasFeature("DASHBOARD"));
        model.addAttribute("canUseMembers", groupAccessService.hasFeature("MEMBERS"));
        model.addAttribute("canUseNotes", groupAccessService.hasFeature("NOTES"));
        model.addAttribute("groupBasePath", "/groups");
        return "groups/members";
    }

    @PostMapping("/notes")
    @PreAuthorize("hasRole('ADMIN') or (@groupAccessService.hasCurrentUserAccess() and @groupAccessService.hasFeature('NOTES'))")
    public String saveNote(@RequestParam String note, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("successMsg", "Group note accepted: " + note.strip());
        return "redirect:/groups";
    }
}
