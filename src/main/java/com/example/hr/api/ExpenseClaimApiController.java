package com.example.hr.api;

import com.example.hr.dto.ExpenseClaimDTO;
import com.example.hr.enums.ExpenseStatus;
import com.example.hr.exception.ResourceNotFoundException;
import com.example.hr.models.ExpenseClaim;
import com.example.hr.models.User;
import com.example.hr.repository.UserRepository;
import com.example.hr.service.ExpenseClaimService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/expenses")
@Tag(name = "Expense Claims", description = "Quản lý yêu cầu hoàn tiền chi phí")
public class ExpenseClaimApiController {

    private final ExpenseClaimService expenseClaimService;
    private final UserRepository userRepository;

    public ExpenseClaimApiController(ExpenseClaimService expenseClaimService,
                                      UserRepository userRepository) {
        this.expenseClaimService = expenseClaimService;
        this.userRepository = userRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Lấy tất cả expense claims")
    public ResponseEntity<List<ExpenseClaim>> getAll(
            @RequestParam(required = false) ExpenseStatus status) {
        List<ExpenseClaim> claims = (status != null)
                ? expenseClaimService.findByStatus(status)
                : expenseClaimService.findAll();
        return ResponseEntity.ok(claims);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lấy expense claim theo ID")
    public ResponseEntity<ExpenseClaim> getById(@PathVariable Integer id) {
        return expenseClaimService.findById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Expense Claim", id));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lấy expense claims của một nhân viên")
    public ResponseEntity<List<ExpenseClaim>> getByUser(@PathVariable Integer userId) {
        return ResponseEntity.ok(expenseClaimService.findByUser(userId));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Lấy danh sách chờ duyệt")
    public ResponseEntity<List<ExpenseClaim>> getPending() {
        return ResponseEntity.ok(expenseClaimService.findByStatus(ExpenseStatus.PENDING));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Tạo yêu cầu hoàn tiền mới")
    public ResponseEntity<ExpenseClaim> create(@Valid @RequestBody ExpenseClaimDTO dto,
                                                Principal principal) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", dto.getUserId()));

        ExpenseClaim claim = new ExpenseClaim();
        claim.setUser(user);
        claim.setClaimTitle(dto.getClaimTitle());
        claim.setCategory(dto.getCategory());
        claim.setAmount(dto.getAmount());
        claim.setCurrency(dto.getCurrency() != null ? dto.getCurrency() : "VND");
        claim.setExpenseDate(dto.getExpenseDate());
        claim.setDescription(dto.getDescription());
        claim.setReceiptUrl(dto.getReceiptUrl());
        claim.setProjectCode(dto.getProjectCode());

        return ResponseEntity.status(HttpStatus.CREATED).body(expenseClaimService.save(claim));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cập nhật expense claim (chỉ khi PENDING)")
    public ResponseEntity<ExpenseClaim> update(@PathVariable Integer id,
                                                @Valid @RequestBody ExpenseClaimDTO dto) {
        ExpenseClaim existing = expenseClaimService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense Claim", id));

        if (existing.getStatus() != ExpenseStatus.PENDING) {
            return ResponseEntity.badRequest().build();
        }

        existing.setClaimTitle(dto.getClaimTitle());
        existing.setCategory(dto.getCategory());
        existing.setAmount(dto.getAmount());
        existing.setExpenseDate(dto.getExpenseDate());
        existing.setDescription(dto.getDescription());
        existing.setReceiptUrl(dto.getReceiptUrl());
        existing.setProjectCode(dto.getProjectCode());

        return ResponseEntity.ok(expenseClaimService.save(existing));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Duyệt expense claim")
    public ResponseEntity<ExpenseClaim> approve(@PathVariable Integer id, Principal principal) {
        User approver = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", principal.getName()));
        return ResponseEntity.ok(expenseClaimService.approve(id, approver));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Từ chối expense claim")
    public ResponseEntity<ExpenseClaim> reject(@PathVariable Integer id,
                                                @RequestBody Map<String, String> body,
                                                Principal principal) {
        User approver = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", principal.getName()));
        String reason = body.getOrDefault("reason", "");
        return ResponseEntity.ok(expenseClaimService.reject(id, approver, reason));
    }

    @PatchMapping("/{id}/paid")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Đánh dấu đã thanh toán")
    public ResponseEntity<ExpenseClaim> markPaid(@PathVariable Integer id) {
        return ResponseEntity.ok(expenseClaimService.markPaid(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xóa expense claim")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        expenseClaimService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense Claim", id));
        expenseClaimService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats/summary")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Thống kê tổng quan expense claims")
    public ResponseEntity<Map<String, Object>> getSummary() {
        BigDecimal totalPending = expenseClaimService.getTotalPendingAmount();
        return ResponseEntity.ok(Map.of(
                "countPending", expenseClaimService.countByStatus(ExpenseStatus.PENDING),
                "countApproved", expenseClaimService.countByStatus(ExpenseStatus.APPROVED),
                "countPaid", expenseClaimService.countByStatus(ExpenseStatus.PAID),
                "countRejected", expenseClaimService.countByStatus(ExpenseStatus.REJECTED),
                "totalPendingAmount", totalPending
        ));
    }
}
