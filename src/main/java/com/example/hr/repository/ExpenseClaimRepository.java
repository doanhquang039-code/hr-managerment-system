package com.example.hr.repository;

import com.example.hr.models.ExpenseClaim;
import com.example.hr.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseClaimRepository extends JpaRepository<ExpenseClaim, Integer> {
    
    List<ExpenseClaim> findByUser(User user);
    
    List<ExpenseClaim> findByStatus(String status);
    
    List<ExpenseClaim> findByUserAndStatus(User user, String status);
    
    List<ExpenseClaim> findByCategory(String category);
    
    List<ExpenseClaim> findByExpenseDateBetween(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT SUM(e.amount) FROM ExpenseClaim e WHERE e.user = :user AND e.status = 'PAID'")
    BigDecimal getTotalPaidAmount(@Param("user") User user);
    
    @Query("SELECT SUM(e.amount) FROM ExpenseClaim e WHERE e.status = 'PENDING'")
    BigDecimal getTotalPendingAmount();
    
    long countByStatus(String status);
}


