package com.example.hr.controllers;

import com.example.hr.service.CollaborationGroupPostService;
import com.example.hr.service.CollaborationGroupTaskService;
import com.example.hr.service.GroupAccessService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/groups")
@PreAuthorize("hasRole('ADMIN') or @groupAccessService.hasCurrentUserAccess()")
public class GroupController {

    private final GroupAccessService groupAccessService;
    private final CollaborationGroupPostService postService;
    private final CollaborationGroupTaskService taskService;

    public GroupController(GroupAccessService groupAccessService,
                           CollaborationGroupPostService postService,
                           CollaborationGroupTaskService taskService) {
        this.groupAccessService = groupAccessService;
        this.postService = postService;
        this.taskService = taskService;
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
        model.addAttribute("groupPosts", postService.getDefaultGroupFeed());
        model.addAttribute("groupTasks", taskService.getDefaultGroupTasks());
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
        model.addAttribute("groupPosts", postService.getDefaultGroupFeed());
        model.addAttribute("groupTasks", taskService.getDefaultGroupTasks());
        return "groups/members";
    }

    @PostMapping("/notes")
    @PreAuthorize("hasRole('ADMIN') or (@groupAccessService.hasCurrentUserAccess() and @groupAccessService.hasFeature('NOTES'))")
    public String saveNote(@RequestParam String note, RedirectAttributes redirectAttributes) {
        postService.createDefaultGroupUpdate(note);
        redirectAttributes.addFlashAttribute("successMsg", "Group update posted.");
        return "redirect:/groups";
    }

    @PostMapping("/tasks")
    @PreAuthorize("hasRole('ADMIN') or (@groupAccessService.hasCurrentUserAccess() and @groupAccessService.hasFeature('DASHBOARD'))")
    public String createTask(@RequestParam String title,
                             @RequestParam(required = false) Integer assigneeId,
                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate,
                             RedirectAttributes redirectAttributes) {
        taskService.createDefaultGroupTask(title, assigneeId, dueDate);
        redirectAttributes.addFlashAttribute("successMsg", "Group task created.");
        return "redirect:/groups";
    }

    @PostMapping("/tasks/{taskId}/status")
    @PreAuthorize("hasRole('ADMIN') or (@groupAccessService.hasCurrentUserAccess() and @groupAccessService.hasFeature('DASHBOARD'))")
    public String updateTaskStatus(@PathVariable Integer taskId,
                                   @RequestParam String status,
                                   RedirectAttributes redirectAttributes) {
        taskService.updateStatus(taskId, status);
        redirectAttributes.addFlashAttribute("successMsg", "Group task updated.");
        return "redirect:/groups";
    }
}
