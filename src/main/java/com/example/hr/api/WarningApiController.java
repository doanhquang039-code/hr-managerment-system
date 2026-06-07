package com.example.hr.api;


import com.example.hr.department.entity.Department;
import com.example.hr.dto.EmployeeWarningDTO;
import com.example.hr.models.EmployeeWarning;
import com.example.hr.service.WarningService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/warnings")
public class WarningApiController {

    private final WarningService warningService;

    public WarningApiController(WarningService warningService) {
        this.warningService = warningService;
    }

    @GetMapping
    public ResponseEntity<List<EmployeeWarning>> getAll() {
        return ResponseEntity.ok(warningService.getAllWarnings());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeWarning> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(warningService.getWarningById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<EmployeeWarning>> getByUser(@PathVariable Integer userId) {
        return ResponseEntity.ok(warningService.getWarningsByUser(userId));
    }

    @GetMapping("/user/{userId}/active")
    public ResponseEntity<List<EmployeeWarning>> getActiveByUser(@PathVariable Integer userId) {
        return ResponseEntity.ok(warningService.getActiveWarnings(userId));
    }

    @PostMapping
    public ResponseEntity<EmployeeWarning> issue(@Valid @RequestBody EmployeeWarningDTO dto) {
        return ResponseEntity.ok(warningService.issueWarning(dto));
    }

    @PatchMapping("/{id}/acknowledge")
    public ResponseEntity<EmployeeWarning> acknowledge(@PathVariable Integer id) {
        return ResponseEntity.ok(warningService.acknowledgeWarning(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        warningService.deleteWarning(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats/level")
    public ResponseEntity<Map<?, ?>> statsByLevel() {
        return ResponseEntity.ok(warningService.countByWarningLevel());
    }

    @GetMapping("/stats/department")
    public ResponseEntity<Map<String, Long>> statsByDepartment() {
        return ResponseEntity.ok(warningService.countByDepartment());
    }
}


