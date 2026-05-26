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
@RequestMapping("/user1/groups")
@PreAuthorize("isAuthenticated()")
public class UserGroupController {

    private final GroupAccessService groupAccessService;

    public UserGroupController(GroupAccessService groupAccessService) {
        this.groupAccessService = groupAccessService;
    }

    @GetMapping
    public String dashboard(Model model) {
        addGroupModel(model);
        return "groups/dashboard";
    }

    @GetMapping("/members")
    @PreAuthorize("@groupAccessService.hasCurrentUserAccess() and @groupAccessService.hasFeature('MEMBERS')")
    public String members(Model model) {
        addGroupModel(model);
        return "groups/members";
    }

    @PostMapping("/notes")
    @PreAuthorize("@groupAccessService.hasCurrentUserAccess() and @groupAccessService.hasFeature('NOTES')")
    public String saveNote(@RequestParam String note, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("successMsg", "Group note accepted: " + note.strip());
        return "redirect:/user1/groups";
    }

    private void addGroupModel(Model model) {
        boolean hasGroupAccess = groupAccessService.hasCurrentUserAccess();
        model.addAttribute("group", groupAccessService.getDefaultGroup());
        model.addAttribute("hasGroupAccess", hasGroupAccess);
        model.addAttribute("canUseDashboard", hasGroupAccess && groupAccessService.hasFeature("DASHBOARD"));
        model.addAttribute("canUseMembers", hasGroupAccess && groupAccessService.hasFeature("MEMBERS"));
        model.addAttribute("canUseNotes", hasGroupAccess && groupAccessService.hasFeature("NOTES"));
        model.addAttribute("groupBasePath", "/user1/groups");
    }
}
