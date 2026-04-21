package com.example.hr.api;

import com.example.hr.dto.ShiftAssignmentDTO;
import com.example.hr.dto.WorkShiftDTO;
import com.example.hr.enums.ShiftAssignmentStatus;
import com.example.hr.exception.ResourceNotFoundException;
import com.example.hr.models.ShiftAssignment;
import com.example.hr.models.User;
import com.example.hr.models.WorkShift;
import com.example.hr.repository.UserRepository;
import com.example.hr.service.WorkShiftService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/shifts")
@Tag(name = "Work Shifts", description = "Quản lý ca làm việc và phân ca nhân viên")
public class WorkShiftApiController {

    private final WorkShiftService workShiftService;
    private final UserRepository userRepository;

    public WorkShiftApiController(WorkShiftService workShiftService,
                                   UserRepository userRepository) {
        this.workShiftService = workShiftService;
        this.userRepository = userRepository;
    }

    // ==================== WORK SHIFTS ====================

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lấy tất cả ca làm việc")
    public ResponseEntity<List<WorkShift>> getAllShifts(
            @RequestParam(defaultValue = "false") boolean activeOnly) {
        List<WorkShift> shifts = activeOnly
                ? workShiftService.findActiveShifts()
                : workShiftService.findAllShifts();
        return ResponseEntity.ok(shifts);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lấy ca làm việc theo ID")
    public ResponseEntity<WorkShift> getShiftById(@PathVariable Integer id) {
        return workShiftService.findShiftById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Work Shift", id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Tạo ca làm việc mới")
    public ResponseEntity<WorkShift> createShift(@Valid @RequestBody WorkShiftDTO dto) {
        WorkShift shift = mapToShift(dto, new WorkShift());
        return ResponseEntity.status(HttpStatus.CREATED).body(workShiftService.saveShift(shift));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cập nhật ca làm việc")
    public ResponseEntity<WorkShift> updateShift(@PathVariable Integer id,
                                                  @Valid @RequestBody WorkShiftDTO dto) {
        WorkShift existing = workShiftService.findShiftById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Work Shift", id));
        mapToShift(dto, existing);
        return ResponseEntity.ok(workShiftService.saveShift(existing));
    }

    @PatchMapping("/{id}/toggle-active")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Bật/tắt trạng thái active của ca")
    public ResponseEntity<WorkShift> toggleActive(@PathVariable Integer id) {
        WorkShift shift = workShiftService.findShiftById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Work Shift", id));
        shift.setIsActive(!Boolean.TRUE.equals(shift.getIsActive()));
        return ResponseEntity.ok(workShiftService.saveShift(shift));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xóa ca làm việc")
    public ResponseEntity<Void> deleteShift(@PathVariable Integer id) {
        workShiftService.findShiftById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Work Shift", id));
        workShiftService.deleteShift(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== SHIFT ASSIGNMENTS ====================

    @GetMapping("/assignments")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Lấy tất cả phân ca")
    public ResponseEntity<List<ShiftAssignment>> getAllAssignments(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        if (from != null && to != null) {
            return ResponseEntity.ok(workShiftService.findAssignmentsByDateRange(from, to));
        }
        return ResponseEntity.ok(workShiftService.findAllAssignments());
    }

    @GetMapping("/assignments/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lấy phân ca theo ID")
    public ResponseEntity<ShiftAssignment> getAssignmentById(@PathVariable Integer id) {
        return workShiftService.findAssignmentById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Shift Assignment", id));
    }

    @GetMapping("/assignments/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lấy phân ca của một nhân viên")
    public ResponseEntity<List<ShiftAssignment>> getAssignmentsByUser(
            @PathVariable Integer userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        if (from != null && to != null) {
            return ResponseEntity.ok(workShiftService.findAssignmentsByUserAndDateRange(userId, from, to));
        }
        return ResponseEntity.ok(workShiftService.findAssignmentsByUser(userId));
    }

    @GetMapping("/assignments/user/{userId}/today")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lấy ca làm việc hôm nay của nhân viên")
    public ResponseEntity<ShiftAssignment> getTodayAssignment(@PathVariable Integer userId) {
        return workShiftService.findAssignmentByUserAndDate(userId, LocalDate.now())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @PostMapping("/assignments")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Phân ca cho nhân viên")
    public ResponseEntity<ShiftAssignment> createAssignment(@Valid @RequestBody ShiftAssignmentDTO dto,
                                                             Principal principal) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", dto.getUserId()));
        WorkShift shift = workShiftService.findShiftById(dto.getShiftId())
                .orElseThrow(() -> new ResourceNotFoundException("Work Shift", dto.getShiftId()));

        // Kiểm tra trùng ca trong ngày
        if (workShiftService.findAssignmentByUserAndDate(dto.getUserId(), dto.getWorkDate()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .build(); // Đã có ca trong ngày này
        }

        ShiftAssignment assignment = new ShiftAssignment();
        assignment.setUser(user);
        assignment.setShift(shift);
        assignment.setWorkDate(dto.getWorkDate());
        assignment.setNote(dto.getNote());
        assignment.setStatus(ShiftAssignmentStatus.SCHEDULED);

        userRepository.findByUsername(principal.getName())
                .ifPresent(assignment::setAssignedBy);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(workShiftService.saveAssignment(assignment));
    }

    @PatchMapping("/assignments/{id}/checkin")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Ghi nhận check-in thực tế")
    public ResponseEntity<ShiftAssignment> checkIn(@PathVariable Integer id) {
        ShiftAssignment assignment = workShiftService.findAssignmentById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shift Assignment", id));
        assignment.setActualCheckIn(LocalTime.now());
        assignment.setStatus(ShiftAssignmentStatus.COMPLETED);
        return ResponseEntity.ok(workShiftService.saveAssignment(assignment));
    }

    @PatchMapping("/assignments/{id}/checkout")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Ghi nhận check-out thực tế")
    public ResponseEntity<ShiftAssignment> checkOut(@PathVariable Integer id) {
        ShiftAssignment assignment = workShiftService.findAssignmentById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shift Assignment", id));
        assignment.setActualCheckOut(LocalTime.now());
        return ResponseEntity.ok(workShiftService.saveAssignment(assignment));
    }

    @PatchMapping("/assignments/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Cập nhật trạng thái phân ca")
    public ResponseEntity<ShiftAssignment> updateStatus(@PathVariable Integer id,
                                                         @RequestBody Map<String, String> body) {
        ShiftAssignment assignment = workShiftService.findAssignmentById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shift Assignment", id));
        assignment.setStatus(ShiftAssignmentStatus.valueOf(body.get("status")));
        return ResponseEntity.ok(workShiftService.saveAssignment(assignment));
    }

    @DeleteMapping("/assignments/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Xóa phân ca")
    public ResponseEntity<Void> deleteAssignment(@PathVariable Integer id) {
        workShiftService.findAssignmentById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shift Assignment", id));
        workShiftService.deleteAssignment(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/assignments/stats/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Thống kê ca làm việc của nhân viên trong tháng")
    public ResponseEntity<Map<String, Object>> getMonthlyStats(
            @PathVariable Integer userId,
            @RequestParam(defaultValue = "0") int month,
            @RequestParam(defaultValue = "0") int year) {
        LocalDate now = LocalDate.now();
        int m = month > 0 ? month : now.getMonthValue();
        int y = year > 0 ? year : now.getYear();
        LocalDate from = LocalDate.of(y, m, 1);
        LocalDate to = from.withDayOfMonth(from.lengthOfMonth());

        long completed = workShiftService.countAssignmentsByStatus(userId, from, to, ShiftAssignmentStatus.COMPLETED);
        long scheduled = workShiftService.countAssignmentsByStatus(userId, from, to, ShiftAssignmentStatus.SCHEDULED);
        long absent = workShiftService.countAssignmentsByStatus(userId, from, to, ShiftAssignmentStatus.ABSENT);

        return ResponseEntity.ok(Map.of(
                "userId", userId,
                "month", m,
                "year", y,
                "completed", completed,
                "scheduled", scheduled,
                "absent", absent
        ));
    }

    // --- helper ---
    private WorkShift mapToShift(WorkShiftDTO dto, WorkShift target) {
        target.setShiftName(dto.getShiftName());
        target.setShiftCode(dto.getShiftCode());
        target.setShiftType(dto.getShiftType());
        target.setStartTime(dto.getStartTime());
        target.setEndTime(dto.getEndTime());
        target.setBreakMinutes(dto.getBreakMinutes() != null ? dto.getBreakMinutes() : 60);
        target.setAllowance(dto.getAllowance());
        target.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        target.setDescription(dto.getDescription());
        return target;
    }
}
