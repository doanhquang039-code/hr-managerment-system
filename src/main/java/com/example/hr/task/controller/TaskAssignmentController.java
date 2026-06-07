package com.example.hr.task.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.hr.enums.NotificationType;
import com.example.hr.enums.Role;
import com.example.hr.enums.TaskStatus;
import com.example.hr.enums.UserStatus;
import com.example.hr.department.entity.Department;
import com.example.hr.task.entity.TaskAssignment;
import com.example.hr.models.User;
import com.example.hr.department.repository.DepartmentRepository;
import com.example.hr.task.repository.TaskAssignmentRepository;
import com.example.hr.task.repository.TaskRepository;
import com.example.hr.user.repository.UserRepository;
import com.example.hr.service.NotificationService;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/assignments")
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
public class TaskAssignmentController {

    @Autowired
    private TaskAssignmentRepository assignmentRepository;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private NotificationService notificationService;

    // 1. Hiá»ƒn thá»‹ danh sÃ¡ch phÃ¢n cÃ´ng
    @GetMapping
    @Transactional(readOnly = true)
    public String list(@RequestParam(name = "departmentId", required = false) Integer departmentId,
                       @RequestParam(name = "keyword", required = false) String keyword,
                       @RequestParam(name = "status", required = false) TaskStatus status,
                       Model model) {
        List<TaskAssignment> assignments = assignmentRepository.findAllWithUser();

        if (departmentId != null) {
            assignments = assignments.stream()
                    .filter(a -> a.getUser() != null
                            && a.getUser().getDepartment() != null
                            && departmentId.equals(a.getUser().getDepartment().getId()))
                    .collect(Collectors.toList());
        }

        if (keyword != null && !keyword.isBlank()) {
            String normalized = keyword.trim().toLowerCase();
            assignments = assignments.stream()
                    .filter(a -> (a.getUser() != null
                            && a.getUser().getFullName() != null
                            && a.getUser().getFullName().toLowerCase().contains(normalized))
                            || (a.getTask() != null
                            && a.getTask().getTaskName() != null
                            && a.getTask().getTaskName().toLowerCase().contains(normalized)))
                    .collect(Collectors.toList());
        }

        if (status != null) {
            assignments = assignments.stream()
                    .filter(a -> a.getStatus() == status)
                    .collect(Collectors.toList());
        }

        List<User> activeManagers = userRepository.findByRoleInAndStatus(
                List.of(Role.MANAGER, Role.ADMIN), UserStatus.ACTIVE);
        Map<Integer, List<User>> managersByDepartmentId = activeManagers.stream()
                .filter(u -> u.getDepartment() != null && u.getDepartment().getId() != null)
                .collect(Collectors.groupingBy(u -> u.getDepartment().getId()));

        Map<Integer, String> assignmentManagers = assignments.stream()
                .collect(Collectors.toMap(
                        TaskAssignment::getId,
                        a -> {
                            Integer depId = a.getUser() != null && a.getUser().getDepartment() != null
                                    ? a.getUser().getDepartment().getId() : null;
                            if (depId == null) return "ChÆ°a cÃ³ quáº£n lÃ½";
                            List<User> managers = managersByDepartmentId.getOrDefault(depId, new ArrayList<>());
                            if (managers.isEmpty()) return "ChÆ°a cÃ³ quáº£n lÃ½";
                            return managers.stream()
                                    .limit(3)
                                    .map(User::getFullName)
                                    .collect(Collectors.joining(", "));
                        }
                ));

        List<Department> departments = departmentRepository.findAll();
        model.addAttribute("assignments", assignments);
        model.addAttribute("departments", departments);
        model.addAttribute("selectedDepartmentId", departmentId);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("keyword", keyword);
        model.addAttribute("assignmentManagers", assignmentManagers);
        model.addAttribute("statuses", TaskStatus.values());
        return "admin/assignment-list";
    }

    // 2. Form giao viá»‡c má»›i
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("assignment", new TaskAssignment());
        model.addAttribute("tasks", taskRepository.findAll());
        model.addAttribute("users", userRepository.findByStatus(UserStatus.ACTIVE));
        model.addAttribute("statuses", TaskStatus.values());
        return "admin/assignment-form";
    }

    // 3. Form chá»‰nh sá»­a phÃ¢n cÃ´ng
    @GetMapping("/edit/{id}")
    @Transactional(readOnly = true)
    public String showEditForm(@PathVariable Integer id, Model model) {
        TaskAssignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("KhÃ´ng tÃ¬m tháº¥y phÃ¢n cÃ´ng: " + id));
        model.addAttribute("assignment", assignment);
        model.addAttribute("tasks", taskRepository.findAll());
        model.addAttribute("users", userRepository.findByStatus(UserStatus.ACTIVE));
        model.addAttribute("statuses", TaskStatus.values());
        return "admin/assignment-form";
    }

    // 4. LÆ°u phÃ¢n cÃ´ng
    @PostMapping("/save")
    public String save(@ModelAttribute("assignment") TaskAssignment assignment) {
        assignmentRepository.save(assignment);
        // Notify employee about new task
        if (assignment.getUser() != null && assignment.getTask() != null) {
            notificationService.createNotification(
                assignment.getUser(),
                "ðŸ“ Báº¡n Ä‘Æ°á»£c giao cÃ´ng viá»‡c má»›i: \"" + assignment.getTask().getTaskName() + "\".",
                NotificationType.TASK_ASSIGNED,
                "/user1/tasks"
            );
        }
        return "redirect:/admin/assignments";
    }

    // 5. XÃ³a phÃ¢n cÃ´ng (Thu há»“i viá»‡c)
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        assignmentRepository.deleteById(id);
        return "redirect:/admin/assignments";
    }
}


