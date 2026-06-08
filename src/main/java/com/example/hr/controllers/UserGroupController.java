package com.example.hr.controllers;

import com.example.hr.service.CollaborationGroupPostService;
import com.example.hr.service.CollaborationGroupTaskService;
import com.example.hr.service.GroupAccessService;
import com.example.hr.models.CollaborationGroupPost;
import com.example.hr.models.CollaborationGroupTask;
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
import java.util.List;

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
    public String members(@RequestParam(required = false) String q, Model model) {
        addGroupModel(model);
        addMemberFilter(model, q);
        return "groups/members";
    }

    @PostMapping("/notes")
    @PreAuthorize("@groupAccessService.hasCurrentUserAccess() and @groupAccessService.hasFeature('NOTES')")
    public String saveNote(@RequestParam String note, RedirectAttributes redirectAttributes) {
        try {
            postService.createDefaultGroupUpdate(note);
            redirectAttributes.addFlashAttribute("successMsg", "Group update posted.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMsg", ex.getMessage());
        }
        return "redirect:/user1/groups";
    }

    @PostMapping("/tasks")
    @PreAuthorize("@groupAccessService.hasCurrentUserAccess() and @groupAccessService.hasFeature('TASKS')")
    public String createTask(@RequestParam String title,
                             @RequestParam(required = false) Integer assigneeId,
                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate,
                             RedirectAttributes redirectAttributes) {
        try {
            taskService.createDefaultGroupTask(title, assigneeId, dueDate);
            redirectAttributes.addFlashAttribute("successMsg", "Group task created.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMsg", ex.getMessage());
        }
        return "redirect:/user1/groups";
    }

    @PostMapping("/tasks/{taskId}/status")
    @PreAuthorize("@groupAccessService.hasCurrentUserAccess() and @groupAccessService.hasFeature('TASKS')")
    public String updateTaskStatus(@PathVariable Integer taskId,
                                   @RequestParam String status,
                                   RedirectAttributes redirectAttributes) {
        try {
            taskService.updateStatus(taskId, status);
            redirectAttributes.addFlashAttribute("successMsg", "Group task updated.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMsg", ex.getMessage());
        }
        return "redirect:/user1/groups";
    }

    private void addGroupModel(Model model) {
        boolean hasGroupAccess = groupAccessService.hasCurrentUserAccess();
        List<CollaborationGroupPost> posts = hasGroupAccess ? postService.getDefaultGroupFeed() : java.util.List.of();
        List<CollaborationGroupTask> tasks = hasGroupAccess ? taskService.getDefaultGroupTasks() : java.util.List.of();
        model.addAttribute("group", groupAccessService.getDefaultGroup());
        model.addAttribute("hasGroupAccess", hasGroupAccess);
        model.addAttribute("canUseDashboard", hasGroupAccess && groupAccessService.hasFeature("DASHBOARD"));
        model.addAttribute("canUseMembers", hasGroupAccess && groupAccessService.hasFeature("MEMBERS"));
        model.addAttribute("canUseNotes", hasGroupAccess && groupAccessService.hasFeature("NOTES"));
        model.addAttribute("canUseTasks", hasGroupAccess && groupAccessService.hasFeature("TASKS"));
        model.addAttribute("canUseFiles", hasGroupAccess && groupAccessService.hasFeature("FILES"));
        model.addAttribute("canUseMeetings", hasGroupAccess && groupAccessService.hasFeature("MEETINGS"));
        model.addAttribute("canUseAnnouncements", hasGroupAccess && groupAccessService.hasFeature("ANNOUNCEMENTS"));
        model.addAttribute("canUseRecognition", hasGroupAccess && groupAccessService.hasFeature("RECOGNITION"));
        model.addAttribute("groupBasePath", "/user1/groups");
        model.addAttribute("groupPosts", posts);
        model.addAttribute("groupTasks", tasks);
        model.addAttribute("effectiveMembers", hasGroupAccess ? groupAccessService.getEffectiveMembers() : java.util.List.of());
        model.addAttribute("postCount", posts.size());
        model.addAttribute("taskCount", tasks.size());
        model.addAttribute("doneTaskCount", tasks.stream().filter(task -> "DONE".equals(task.getStatus())).count());
    }

    private void addMemberFilter(Model model, String query) {
        List<com.example.hr.models.User> members = groupAccessService.getEffectiveMembers();
        if (query != null && !query.isBlank()) {
            String lower = query.trim().toLowerCase();
            members = members.stream()
                    .filter(user -> contains(user.getFullName(), lower)
                            || contains(user.getUsername(), lower)
                            || contains(user.getEmail(), lower)
                            || (user.getDepartment() != null && contains(user.getDepartment().getDepartmentName(), lower)))
                    .toList();
        }
        model.addAttribute("members", members);
        model.addAttribute("q", query);
    }

    private boolean contains(String value, String lower) {
        return value != null && value.toLowerCase().contains(lower);
    }
}


