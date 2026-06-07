package com.example.hr.api;


import com.example.hr.department.entity.Department;
import com.example.hr.dto.EmployeeBenefitDTO;
import com.example.hr.models.EmployeeBenefit;
import com.example.hr.service.BenefitService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/benefits")
public class BenefitApiController {

    private final BenefitService benefitService;

    public BenefitApiController(BenefitService benefitService) {
        this.benefitService = benefitService;
    }

    @GetMapping
    public ResponseEntity<List<EmployeeBenefit>> getAll() {
        return ResponseEntity.ok(benefitService.getAllBenefits());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeBenefit> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(benefitService.getBenefitById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<EmployeeBenefit>> getByUser(@PathVariable Integer userId) {
        return ResponseEntity.ok(benefitService.getBenefitsByUser(userId));
    }

    @GetMapping("/user/{userId}/active")
    public ResponseEntity<List<EmployeeBenefit>> getActiveByUser(@PathVariable Integer userId) {
        return ResponseEntity.ok(benefitService.getActiveBenefitsByUser(userId));
    }

    @PostMapping
    public ResponseEntity<EmployeeBenefit> assign(@Valid @RequestBody EmployeeBenefitDTO dto) {
        return ResponseEntity.ok(benefitService.assignBenefit(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeBenefit> update(@PathVariable Integer id,
                                                    @RequestBody EmployeeBenefitDTO dto) {
        return ResponseEntity.ok(benefitService.updateBenefit(id, dto));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<EmployeeBenefit> cancel(@PathVariable Integer id) {
        return ResponseEntity.ok(benefitService.cancelBenefit(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        benefitService.deleteBenefit(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/expiring")
    public ResponseEntity<List<EmployeeBenefit>> getExpiring() {
        return ResponseEntity.ok(benefitService.getExpiringSoonBenefits());
    }

    @GetMapping("/total-cost")
    public ResponseEntity<BigDecimal> getTotalCost() {
        return ResponseEntity.ok(benefitService.getTotalActiveBenefitCost());
    }

    @GetMapping("/stats/type")
    public ResponseEntity<Map<?, ?>> statsByType() {
        return ResponseEntity.ok(benefitService.countActiveBenefitsByType());
    }

    @GetMapping("/stats/department")
    public ResponseEntity<Map<String, BigDecimal>> statsByDepartment() {
        return ResponseEntity.ok(benefitService.getCostByDepartment());
    }
}


