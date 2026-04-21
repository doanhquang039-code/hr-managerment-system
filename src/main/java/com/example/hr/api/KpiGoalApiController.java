package com.example.hr.api;

import com.example.hr.dto.KpiGoalDTO;
import com.example.hr.enums.KpiStatus;
import com.example.hr.exception.ResourceNotFoundException;
import com.example.hr.models.Department;
import com.example.hr.models.KpiGoal;
import com.example.hr.models.User;
import com.example.hr.repository.DepartmentRepository;
import com.example.hr.repository.UserRepository;
import com.example.hr.service.KpiGoalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/kpi")
@Tag(name = "KPI Goals", description = "Quản lý mục tiêu KPI nhân viên")
public class KpiGoalApiController {

    private final KpiGoalService kpiGoalService;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;

    public KpiGoalApiController(KpiGoalService kpiGoalService,
                                 UserRepository userRepository,
                                 DepartmentRepository departmentRepository) {
        this.kpiGoalService = kpiGoalService;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Lấy tất cả KPI Goals")
    public ResponseEntity<List<KpiGoal>> getAll(
            @RequestParam(required = false) KpiStatus status) {
        List<KpiGoal> goals = (status != null)
                ? kpiGoalService.findByStatus(status)
                : kpiGoalService.findAll();
        return ResponseEntity.ok(goals);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lấy KPI Goal theo ID")
    public ResponseEntity<KpiGoal> getById(@PathVariable Integer id) {
        return kpiGoalService.findById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("KPI Goal", id));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lấy KPI Goals của một nhân viên")
    public ResponseEntity<List<KpiGoal>> getByUser(@PathVariable Integer userId) {
        return ResponseEntity.ok(kpiGoalService.findByUser(userId));
    }

    @GetMapping("/user/{userId}/active")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lấy KPI Goals đang active của nhân viên")
    public ResponseEntity<List<KpiGoal>> getActiveByUser(@PathVariable Integer userId) {
        return ResponseEntity.ok(kpiGoalService.findActiveByUser(userId));
    }

    @GetMapping("/user/{userId}/avg-achievement")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Tỷ lệ hoàn thành KPI trung bình của nhân viên")
    public ResponseEntity<Map<String, Object>> getAvgAchievement(@PathVariable Integer userId) {
        Double avg = kpiGoalService.getAvgAchievement(userId);
        return ResponseEntity.ok(Map.of("userId", userId, "avgAchievementPct", avg));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Tạo KPI Goal mới")
    public ResponseEntity<KpiGoal> create(@Valid @RequestBody KpiGoalDTO dto, Principal principal) {
        KpiGoal goal = mapToGoal(dto, new KpiGoal());

        // Set creator
        userRepository.findByUsername(principal.getName())
                .ifPresent(goal::setCreatedBy);

        KpiGoal saved = kpiGoalService.save(goal);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Cập nhật KPI Goal")
    public ResponseEntity<KpiGoal> update(@PathVariable Integer id,
                                           @Valid @RequestBody KpiGoalDTO dto) {
        KpiGoal existing = kpiGoalService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("KPI Goal", id));
        KpiGoal updated = mapToGoal(dto, existing);
        return ResponseEntity.ok(kpiGoalService.save(updated));
    }

    @PatchMapping("/{id}/progress")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cập nhật tiến độ KPI (currentValue)")
    public ResponseEntity<KpiGoal> updateProgress(@PathVariable Integer id,
                                                   @RequestBody Map<String, BigDecimal> body) {
        BigDecimal currentValue = body.get("currentValue");
        if (currentValue == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(kpiGoalService.updateProgress(id, currentValue));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Thay đổi trạng thái KPI Goal")
    public ResponseEntity<KpiGoal> changeStatus(@PathVariable Integer id,
                                                 @RequestBody Map<String, String> body) {
        KpiGoal goal = kpiGoalService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("KPI Goal", id));
        goal.setStatus(KpiStatus.valueOf(body.get("status")));
        return ResponseEntity.ok(kpiGoalService.save(goal));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xóa KPI Goal")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        kpiGoalService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("KPI Goal", id));
        kpiGoalService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats/summary")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Thống kê tổng quan KPI")
    public ResponseEntity<Map<String, Object>> getSummary() {
        return ResponseEntity.ok(Map.of(
                "totalActive", kpiGoalService.countByStatus(KpiStatus.ACTIVE),
                "totalCompleted", kpiGoalService.countByStatus(KpiStatus.COMPLETED),
                "totalCancelled", kpiGoalService.countByStatus(KpiStatus.CANCELED)
        ));
    }

    // --- helper ---
    private KpiGoal mapToGoal(KpiGoalDTO dto, KpiGoal target) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", dto.getUserId()));
        target.setUser(user);

        if (dto.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", dto.getDepartmentId()));
            target.setDepartment(dept);
        }

        target.setGoalTitle(dto.getGoalTitle());
        target.setDescription(dto.getDescription());
        target.setCategory(dto.getCategory());
        target.setTargetValue(dto.getTargetValue());
        target.setCurrentValue(dto.getCurrentValue() != null ? dto.getCurrentValue() : BigDecimal.ZERO);
        target.setUnit(dto.getUnit());
        target.setWeight(dto.getWeight() != null ? dto.getWeight() : BigDecimal.ONE);
        target.setStartDate(dto.getStartDate());
        target.setEndDate(dto.getEndDate());
        target.setStatus(dto.getStatus() != null ? dto.getStatus() : KpiStatus.ACTIVE);
        return target;
    }
}
