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
import java.util.List;

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
        addGroupModel(model, "/groups", true);
        return "groups/dashboard";
    }

    @GetMapping("/members")
    @PreAuthorize("hasRole('ADMIN') or (@groupAccessService.hasCurrentUserAccess() and @groupAccessService.hasFeature('MEMBERS'))")
    public String members(@RequestParam(required = false) String q, Model model) {
        addGroupModel(model, "/groups", true);
        addMemberFilter(model, q);
        return "groups/members";
    }

    @PostMapping("/notes")
    @PreAuthorize("hasRole('ADMIN') or (@groupAccessService.hasCurrentUserAccess() and @groupAccessService.hasFeature('NOTES'))")
    public String saveNote(@RequestParam String note, RedirectAttributes redirectAttributes) {
        try {
            postService.createDefaultGroupUpdate(note);
            redirectAttributes.addFlashAttribute("successMsg", "Group update posted.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMsg", ex.getMessage());
        }
        return "redirect:/groups";
    }

    @PostMapping("/tasks")
    @PreAuthorize("hasRole('ADMIN') or (@groupAccessService.hasCurrentUserAccess() and @groupAccessService.hasFeature('TASKS'))")
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
        return "redirect:/groups";
    }

    @PostMapping("/tasks/{taskId}/status")
    @PreAuthorize("hasRole('ADMIN') or (@groupAccessService.hasCurrentUserAccess() and @groupAccessService.hasFeature('TASKS'))")
    public String updateTaskStatus(@PathVariable Integer taskId,
                                   @RequestParam String status,
                                   RedirectAttributes redirectAttributes) {
        try {
            taskService.updateStatus(taskId, status);
            redirectAttributes.addFlashAttribute("successMsg", "Group task updated.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMsg", ex.getMessage());
        }
        return "redirect:/groups";
    }

    private void addGroupModel(Model model, String basePath, boolean hasGroupAccess) {
        var posts = postService.getDefaultGroupFeed();
        var tasks = taskService.getDefaultGroupTasks();
        var effectiveMembers = groupAccessService.getEffectiveMembers();

        model.addAttribute("group", groupAccessService.getDefaultGroup());
        model.addAttribute("hasGroupAccess", hasGroupAccess);
        model.addAttribute("canUseDashboard", groupAccessService.hasFeature("DASHBOARD"));
        model.addAttribute("canUseMembers", groupAccessService.hasFeature("MEMBERS"));
        model.addAttribute("canUseNotes", groupAccessService.hasFeature("NOTES"));
        model.addAttribute("canUseTasks", groupAccessService.hasFeature("TASKS"));
        model.addAttribute("canUseFiles", groupAccessService.hasFeature("FILES"));
        model.addAttribute("canUseMeetings", groupAccessService.hasFeature("MEETINGS"));
        model.addAttribute("canUseAnnouncements", groupAccessService.hasFeature("ANNOUNCEMENTS"));
        model.addAttribute("canUseRecognition", groupAccessService.hasFeature("RECOGNITION"));
        model.addAttribute("groupBasePath", basePath);
        model.addAttribute("groupPosts", posts);
        model.addAttribute("groupTasks", tasks);
        model.addAttribute("effectiveMembers", effectiveMembers);
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


