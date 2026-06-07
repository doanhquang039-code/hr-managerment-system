package com.example.hr.repository;

import com.example.hr.models.TeamBudget;
import com.example.hr.department.entity.Department;
import com.example.hr.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface TeamBudgetRepository extends JpaRepository<TeamBudget, Integer> {

    List<TeamBudget> findByDepartmentOrderByYearDescMonthDesc(Department department);

    List<TeamBudget> findByManagerOrderByYearDescMonthDesc(User manager);

    Optional<TeamBudget> findByDepartmentAndYearAndMonthAndCategory(Department department, Integer year, Integer month, String category);

    List<TeamBudget> findByYearAndMonth(Integer year, Integer month);

    List<TeamBudget> findByStatus(String status);

    @Query("SELECT SUM(b.allocatedBudget) FROM TeamBudget b WHERE b.department = :department AND b.year = :year")
    BigDecimal getTotalAllocatedBudgetByDepartmentAndYear(@Param("department") Department department, @Param("year") Integer year);

    @Query("SELECT SUM(b.spentBudget) FROM TeamBudget b WHERE b.department = :department AND b.year = :year")
    BigDecimal getTotalSpentBudgetByDepartmentAndYear(@Param("department") Department department, @Param("year") Integer year);

    @Query("SELECT b FROM TeamBudget b WHERE b.manager = :manager AND b.year = :year AND b.month = :month")
    List<TeamBudget> findByManagerAndYearAndMonth(@Param("manager") User manager, @Param("year") Integer year, @Param("month") Integer month);
}


