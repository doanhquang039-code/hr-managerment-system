package com.example.hr.repository;

import com.example.hr.enums.ExpenseStatus;
import com.example.hr.models.ExpenseClaim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseClaimRepository extends JpaRepository<ExpenseClaim, Integer> {

    List<ExpenseClaim> findByUserId(Integer userId);

    List<ExpenseClaim> findByUserIdAndStatus(Integer userId, ExpenseStatus status);

    List<ExpenseClaim> findByStatus(ExpenseStatus status);

    List<ExpenseClaim> findByExpenseDateBetween(LocalDate from, LocalDate to);

    @Query("SELECT SUM(e.amount) FROM ExpenseClaim e WHERE e.user.id = :userId AND e.status = 'PAID'")
    BigDecimal totalPaidByUser(@Param("userId") Integer userId);

    @Query("SELECT SUM(e.amount) FROM ExpenseClaim e WHERE e.status = 'PENDING'")
    BigDecimal totalPendingAmount();

    List<ExpenseClaim> findByUserIdAndExpenseDateBetween(Integer userId, LocalDate from, LocalDate to);
}
