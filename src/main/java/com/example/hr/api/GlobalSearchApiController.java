package com.example.hr.api;







import com.example.hr.department.repository.DepartmentRepository;
import com.example.hr.department.entity.Department;
import com.example.hr.user.repository.UserRepository;
import com.example.hr.payroll.entity.Payroll;
import com.example.hr.payroll.repository.PayrollRepository;
import com.example.hr.leave.repository.LeaveRequestRepository;
import com.example.hr.enums.UserStatus;
import com.example.hr.repository.*;
import com.example.hr.task.repository.TaskRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/search")
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
@Tag(name = "Global Search", description = "TÃ¬m kiáº¿m toÃ n há»‡ thá»‘ng")
public class GlobalSearchApiController {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final LeaveRequestRepository leaveRepository;
    private final TaskRepository taskRepository;
    private final PayrollRepository payrollRepository;

    public GlobalSearchApiController(UserRepository userRepository,
                                      DepartmentRepository departmentRepository,
                                      LeaveRequestRepository leaveRepository,
                                      TaskRepository taskRepository,
                                      PayrollRepository payrollRepository) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.leaveRepository = leaveRepository;
        this.taskRepository = taskRepository;
        this.payrollRepository = payrollRepository;
    }

    @GetMapping
    @Operation(summary = "TÃ¬m kiáº¿m toÃ n há»‡ thá»‘ng")
    public ResponseEntity<Map<String, Object>> search(@RequestParam String q) {
        if (q == null || q.trim().length() < 2) {
            return ResponseEntity.ok(Map.of("results", List.of(), "total", 0));
        }

        String kw = q.trim().toLowerCase();
        List<Map<String, Object>> results = new ArrayList<>();

        // Search employees
        userRepository.findByStatus(UserStatus.ACTIVE).stream()
                .filter(u -> u.getFullName().toLowerCase().contains(kw)
                        || (u.getEmail() != null && u.getEmail().toLowerCase().contains(kw))
                        || (u.getEmployeeCode() != null && u.getEmployeeCode().toLowerCase().contains(kw)))
                .limit(5)
                .forEach(u -> results.add(Map.of(
                        "type", "employee",
                        "icon", "ðŸ‘¤",
                        "title", u.getFullName(),
                        "subtitle", u.getEmail() != null ? u.getEmail() : "",
                        "url", "/admin/users/edit/" + u.getId()
                )));

        // Search departments
        departmentRepository.findAll().stream()
                .filter(d -> d.getDepartmentName().toLowerCase().contains(kw))
                .limit(3)
                .forEach(d -> results.add(Map.of(
                        "type", "department",
                        "icon", "ðŸ¢",
                        "title", d.getDepartmentName(),
                        "subtitle", "Department",
                        "url", "/admin/departments"
                )));

        // Search tasks
        taskRepository.findAll().stream()
                .filter(t -> t.getTaskName() != null && t.getTaskName().toLowerCase().contains(kw))
                .limit(3)
                .forEach(t -> results.add(Map.of(
                        "type", "task",
                        "icon", "âœ…",
                        "title", t.getTaskName(),
                        "subtitle", "Task",
                        "url", "/admin/tasks"
                )));

        return ResponseEntity.ok(Map.of(
                "query", q,
                "results", results,
                "total", results.size()
        ));
    }
}


