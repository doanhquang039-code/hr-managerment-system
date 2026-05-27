package com.example.hr.service;

import com.example.hr.models.CollaborationGroup;
import com.example.hr.models.CollaborationGroupTask;
import com.example.hr.models.User;
import com.example.hr.repository.CollaborationGroupTaskRepository;
import com.example.hr.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class CollaborationGroupTaskService {

    private static final Set<String> ALLOWED_STATUSES = Set.of("TODO", "IN_PROGRESS", "DONE");

    private final CollaborationGroupTaskRepository taskRepository;
    private final UserRepository userRepository;
    private final GroupAccessService groupAccessService;
    private final AuthUserHelper authUserHelper;

    public CollaborationGroupTaskService(CollaborationGroupTaskRepository taskRepository,
                                         UserRepository userRepository,
                                         GroupAccessService groupAccessService,
                                         AuthUserHelper authUserHelper) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.groupAccessService = groupAccessService;
        this.authUserHelper = authUserHelper;
    }

    @Transactional(readOnly = true)
    public List<CollaborationGroupTask> getDefaultGroupTasks() {
        return taskRepository.findBoardByGroup(groupAccessService.getDefaultGroup());
    }

    public CollaborationGroupTask createDefaultGroupTask(String title, Integer assigneeId, LocalDate dueDate) {
        User creator = getCurrentUser();
        if (creator == null) {
            throw new IllegalStateException("Current user is required to create a group task.");
        }

        String normalizedTitle = title == null ? "" : title.strip();
        if (normalizedTitle.isBlank()) {
            throw new IllegalArgumentException("Task title is required.");
        }

        CollaborationGroup group = groupAccessService.getDefaultGroup();
        CollaborationGroupTask task = new CollaborationGroupTask();
        task.setGroup(group);
        task.setCreatedBy(creator);
        task.setTitle(normalizedTitle);
        task.setDueDate(dueDate);

        if (assigneeId != null) {
            userRepository.findById(assigneeId).ifPresent(task::setAssignee);
        }

        return taskRepository.save(task);
    }

    public void updateStatus(Integer taskId, String status) {
        String normalizedStatus = status == null ? "" : status.strip().toUpperCase();
        if (!ALLOWED_STATUSES.contains(normalizedStatus)) {
            throw new IllegalArgumentException("Unsupported task status.");
        }

        CollaborationGroup group = groupAccessService.getDefaultGroup();
        CollaborationGroupTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Group task not found."));
        if (!task.getGroup().getId().equals(group.getId())) {
            throw new IllegalArgumentException("Task does not belong to the default group.");
        }

        task.setStatus(normalizedStatus);
        taskRepository.save(task);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authUserHelper.getCurrentUser(authentication);
    }
}
