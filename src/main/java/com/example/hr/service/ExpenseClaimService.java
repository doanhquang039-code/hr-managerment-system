package com.example.hr.service;

import com.example.hr.enums.ExpenseStatus;
import com.example.hr.models.ExpenseClaim;
import com.example.hr.models.User;
import com.example.hr.repository.ExpenseClaimRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ExpenseClaimService {

    private final ExpenseClaimRepository expenseClaimRepository;

    public ExpenseClaimService(ExpenseClaimRepository expenseClaimRepository) {
        this.expenseClaimRepository = expenseClaimRepository;
    }

    public List<ExpenseClaim> findAll() {
        return expenseClaimRepository.findAll();
    }

    public List<ExpenseClaim> findByUser(Integer userId) {
        return expenseClaimRepository.findByUserId(userId);
    }

    public List<ExpenseClaim> findByStatus(ExpenseStatus status) {
        return expenseClaimRepository.findByStatus(status);
    }

    public Optional<ExpenseClaim> findById(Integer id) {
        return expenseClaimRepository.findById(id);
    }

    public ExpenseClaim save(ExpenseClaim claim) {
        if (claim.getId() == null) {
            claim.setCreatedAt(LocalDateTime.now());
            claim.setStatus(ExpenseStatus.PENDING);
        }
        claim.setUpdatedAt(LocalDateTime.now());
        return expenseClaimRepository.save(claim);
    }

    public ExpenseClaim approve(Integer id, User approver) {
        ExpenseClaim claim = expenseClaimRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense claim không tồn tại: " + id));
        claim.setStatus(ExpenseStatus.APPROVED);
        claim.setApprovedBy(approver);
        claim.setApprovedAt(LocalDateTime.now());
        claim.setUpdatedAt(LocalDateTime.now());
        return expenseClaimRepository.save(claim);
    }

    public ExpenseClaim reject(Integer id, User approver, String reason) {
        ExpenseClaim claim = expenseClaimRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense claim không tồn tại: " + id));
        claim.setStatus(ExpenseStatus.REJECTED);
        claim.setApprovedBy(approver);
        claim.setApprovedAt(LocalDateTime.now());
        claim.setRejectionReason(reason);
        claim.setUpdatedAt(LocalDateTime.now());
        return expenseClaimRepository.save(claim);
    }

    public ExpenseClaim markPaid(Integer id) {
        ExpenseClaim claim = expenseClaimRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense claim không tồn tại: " + id));
        claim.setStatus(ExpenseStatus.PAID);
        claim.setPaidAt(LocalDateTime.now());
        claim.setUpdatedAt(LocalDateTime.now());
        return expenseClaimRepository.save(claim);
    }

    public void delete(Integer id) {
        expenseClaimRepository.deleteById(id);
    }

    public BigDecimal getTotalPendingAmount() {
        BigDecimal total = expenseClaimRepository.totalPendingAmount();
        return total != null ? total : BigDecimal.ZERO;
    }

    public long countByStatus(ExpenseStatus status) {
        return expenseClaimRepository.findByStatus(status).size();
    }
}
