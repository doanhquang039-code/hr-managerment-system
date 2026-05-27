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
@RequestMapping("/user1/groups")
@PreAuthorize("isAuthenticated()")
public class UserGroupController {

    private final GroupAccessService groupAccessService;
    private final CollaborationGroupPostService postService;
    private final CollaborationGroupTaskService taskService;

    public UserGroupController(GroupAccessService groupAccessService,
                               CollaborationGroupPostService postService,
                               CollaborationGroupTaskService taskService) {
        this.groupAccessService = groupAccessService;
        this.postService = postService;
        this.taskService = taskService;
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
        postService.createDefaultGroupUpdate(note);
        redirectAttributes.addFlashAttribute("successMsg", "Group update posted.");
        return "redirect:/user1/groups";
    }

    @PostMapping("/tasks")
    @PreAuthorize("@groupAccessService.hasCurrentUserAccess() and @groupAccessService.hasFeature('DASHBOARD')")
    public String createTask(@RequestParam String title,
                             @RequestParam(required = false) Integer assigneeId,
                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate,
                             RedirectAttributes redirectAttributes) {
        taskService.createDefaultGroupTask(title, assigneeId, dueDate);
        redirectAttributes.addFlashAttribute("successMsg", "Group task created.");
        return "redirect:/user1/groups";
    }

    @PostMapping("/tasks/{taskId}/status")
    @PreAuthorize("@groupAccessService.hasCurrentUserAccess() and @groupAccessService.hasFeature('DASHBOARD')")
    public String updateTaskStatus(@PathVariable Integer taskId,
                                   @RequestParam String status,
                                   RedirectAttributes redirectAttributes) {
        taskService.updateStatus(taskId, status);
        redirectAttributes.addFlashAttribute("successMsg", "Group task updated.");
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
        model.addAttribute("groupPosts", hasGroupAccess ? postService.getDefaultGroupFeed() : java.util.List.of());
        model.addAttribute("groupTasks", hasGroupAccess ? taskService.getDefaultGroupTasks() : java.util.List.of());
    }
}
