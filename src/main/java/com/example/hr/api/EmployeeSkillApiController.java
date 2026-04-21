package com.example.hr.api;

import com.example.hr.dto.EmployeeSkillDTO;
import com.example.hr.enums.SkillLevel;
import com.example.hr.exception.ResourceNotFoundException;
import com.example.hr.models.EmployeeSkill;
import com.example.hr.models.User;
import com.example.hr.repository.UserRepository;
import com.example.hr.service.EmployeeSkillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/skills")
@Tag(name = "Employee Skills", description = "Quản lý kỹ năng nhân viên")
public class EmployeeSkillApiController {

    private final EmployeeSkillService employeeSkillService;
    private final UserRepository userRepository;

    public EmployeeSkillApiController(EmployeeSkillService employeeSkillService,
                                       UserRepository userRepository) {
        this.employeeSkillService = employeeSkillService;
        this.userRepository = userRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Lấy tất cả kỹ năng nhân viên")
    public ResponseEntity<List<EmployeeSkill>> getAll(
            @RequestParam(required = false) String category) {
        List<EmployeeSkill> skills = employeeSkillService.findAll();
        if (category != null && !category.isBlank()) {
            skills = skills.stream()
                    .filter(s -> category.equalsIgnoreCase(s.getSkillCategory()))
                    .toList();
        }
        return ResponseEntity.ok(skills);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lấy kỹ năng theo ID")
    public ResponseEntity<EmployeeSkill> getById(@PathVariable Integer id) {
        return employeeSkillService.findById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Employee Skill", id));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lấy kỹ năng của một nhân viên")
    public ResponseEntity<List<EmployeeSkill>> getByUser(
            @PathVariable Integer userId,
            @RequestParam(required = false) String category) {
        List<EmployeeSkill> skills = (category != null && !category.isBlank())
                ? employeeSkillService.findByUserAndCategory(userId, category)
                : employeeSkillService.findByUser(userId);
        return ResponseEntity.ok(skills);
    }

    @GetMapping("/user/{userId}/certified")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lấy kỹ năng có chứng chỉ của nhân viên")
    public ResponseEntity<List<EmployeeSkill>> getCertifiedByUser(@PathVariable Integer userId) {
        return ResponseEntity.ok(employeeSkillService.findCertifiedByUser(userId));
    }

    @GetMapping("/categories/{category}/names")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lấy danh sách tên kỹ năng theo category")
    public ResponseEntity<List<String>> getSkillNamesByCategory(@PathVariable String category) {
        return ResponseEntity.ok(employeeSkillService.getDistinctSkillsByCategory(category));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Thêm kỹ năng mới cho nhân viên")
    public ResponseEntity<EmployeeSkill> create(@Valid @RequestBody EmployeeSkillDTO dto,
                                                 Principal principal) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", dto.getUserId()));

        if (employeeSkillService.existsByUserAndSkillName(dto.getUserId(), dto.getSkillName())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        EmployeeSkill skill = mapToSkill(dto, new EmployeeSkill());
        skill.setUser(user);

        // Người xác nhận là người đang đăng nhập (nếu là admin/manager)
        userRepository.findByUsername(principal.getName()).ifPresent(skill::setVerifiedBy);

        return ResponseEntity.status(HttpStatus.CREATED).body(employeeSkillService.save(skill));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cập nhật kỹ năng")
    public ResponseEntity<EmployeeSkill> update(@PathVariable Integer id,
                                                 @Valid @RequestBody EmployeeSkillDTO dto) {
        EmployeeSkill existing = employeeSkillService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee Skill", id));
        mapToSkill(dto, existing);
        return ResponseEntity.ok(employeeSkillService.save(existing));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Xóa kỹ năng")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        employeeSkillService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee Skill", id));
        employeeSkillService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats/levels")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Thống kê kỹ năng theo cấp độ")
    public ResponseEntity<Map<String, Long>> statsByLevel() {
        List<EmployeeSkill> all = employeeSkillService.findAll();
        Map<String, Long> stats = new java.util.LinkedHashMap<>();
        for (SkillLevel level : SkillLevel.values()) {
            long count = all.stream().filter(s -> s.getSkillLevel() == level).count();
            stats.put(level.name(), count);
        }
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/stats/categories")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Thống kê kỹ năng theo category")
    public ResponseEntity<Map<String, Long>> statsByCategory() {
        List<EmployeeSkill> all = employeeSkillService.findAll();
        Map<String, Long> stats = new java.util.LinkedHashMap<>();
        all.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        s -> s.getSkillCategory() != null ? s.getSkillCategory() : "OTHER",
                        java.util.stream.Collectors.counting()))
                .forEach(stats::put);
        return ResponseEntity.ok(stats);
    }

    // --- helper ---
    private EmployeeSkill mapToSkill(EmployeeSkillDTO dto, EmployeeSkill target) {
        target.setSkillName(dto.getSkillName());
        target.setSkillCategory(dto.getSkillCategory() != null ? dto.getSkillCategory() : "TECHNICAL");
        target.setSkillLevel(dto.getSkillLevel() != null ? dto.getSkillLevel() : SkillLevel.BEGINNER);
        target.setYearsExp(dto.getYearsExp() != null ? dto.getYearsExp() : java.math.BigDecimal.ZERO);
        target.setIsCertified(dto.getIsCertified() != null ? dto.getIsCertified() : false);
        target.setCertificateName(dto.getCertificateName());
        target.setCertificateUrl(dto.getCertificateUrl());
        target.setIssuedDate(dto.getIssuedDate());
        target.setExpiryDate(dto.getExpiryDate());
        target.setNotes(dto.getNotes());
        return target;
    }
}
