package com.example.hr.service;

import com.example.hr.models.TeamBudget;
import com.example.hr.department.entity.Department;
import com.example.hr.models.User;
import com.example.hr.repository.TeamBudgetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamBudgetService {

    private final TeamBudgetRepository teamBudgetRepository;

    /**
     * Create new budget
     */
    @Transactional
    public TeamBudget createBudget(TeamBudget budget) {
        budget.setCreatedAt(LocalDateTime.now());
        budget.setStatus("ACTIVE");
        budget.setSpentBudget(BigDecimal.ZERO);
        budget.calculateRemainingBudget();
        return teamBudgetRepository.save(budget);
    }

    /**
     * Update budget spending
     */
    @Transactional
    public TeamBudget updateSpending(Integer id, BigDecimal amount) {
        TeamBudget budget = getBudgetById(id);
        budget.setSpentBudget(budget.getSpentBudget().add(amount));
        budget.calculateRemainingBudget();
        budget.setUpdatedAt(LocalDateTime.now());
        
        // Check if exceeded
        if (budget.getRemainingBudget().compareTo(BigDecimal.ZERO) < 0) {
            budget.setStatus("EXCEEDED");
        }
        
        return teamBudgetRepository.save(budget);
    }

    /**
     * Update budget
     */
    @Transactional
    public TeamBudget updateBudget(Integer id, TeamBudget budgetData) {
        TeamBudget budget = getBudgetById(id);
        
        budget.setAllocatedBudget(budgetData.getAllocatedBudget());
        budget.setCategory(budgetData.getCategory());
        budget.setDescription(budgetData.getDescription());
        budget.setUpdatedAt(LocalDateTime.now());
        
        budget.calculateRemainingBudget();
        
        return teamBudgetRepository.save(budget);
    }

    /**
     * Close budget
     */
    @Transactional
    public void closeBudget(Integer id) {
        TeamBudget budget = getBudgetById(id);
        budget.setStatus("CLOSED");
        budget.setUpdatedAt(LocalDateTime.now());
        teamBudgetRepository.save(budget);
    }

    /**
     * Get budget by ID
     */
    public TeamBudget getBudgetById(Integer id) {
        return teamBudgetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Budget not found with id: " + id));
    }

    /**
     * Get budgets by department
     */
    public List<TeamBudget> getBudgetsByDepartment(Department department) {
        return teamBudgetRepository.findByDepartmentOrderByYearDescMonthDesc(department);
    }

    /**
     * Get budgets by manager
     */
    public List<TeamBudget> getBudgetsByManager(User manager) {
        return teamBudgetRepository.findByManagerOrderByYearDescMonthDesc(manager);
    }

    /**
     * Get current month budget
     */
    public List<TeamBudget> getCurrentMonthBudgets(User manager) {
        LocalDateTime now = LocalDateTime.now();
        return teamBudgetRepository.findByManagerAndYearAndMonth(
                manager, now.getYear(), now.getMonthValue());
    }

    /**
     * Get all budgets
     */
    public List<TeamBudget> getAllBudgets() {
        return teamBudgetRepository.findAll();
    }

    /**
     * Get budget statistics
     */
    public BudgetStatistics getBudgetStatistics(Department department, Integer year) {
        BigDecimal totalAllocated = teamBudgetRepository
                .getTotalAllocatedBudgetByDepartmentAndYear(department, year);
        BigDecimal totalSpent = teamBudgetRepository
                .getTotalSpentBudgetByDepartmentAndYear(department, year);
        
        if (totalAllocated == null) totalAllocated = BigDecimal.ZERO;
        if (totalSpent == null) totalSpent = BigDecimal.ZERO;
        
        BigDecimal remaining = totalAllocated.subtract(totalSpent);
        double utilizationRate = totalAllocated.compareTo(BigDecimal.ZERO) > 0
                ? totalSpent.divide(totalAllocated, 4, BigDecimal.ROUND_HALF_UP)
                        .multiply(BigDecimal.valueOf(100)).doubleValue()
                : 0.0;
        
        return new BudgetStatistics(
                totalAllocated,
                totalSpent,
                remaining,
                utilizationRate
        );
    }

    public record BudgetStatistics(
            BigDecimal totalAllocated,
            BigDecimal totalSpent,
            BigDecimal remaining,
            double utilizationRate
    ) {}
}


