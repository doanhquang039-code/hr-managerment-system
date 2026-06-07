package com.example.hr.repository;


import com.example.hr.department.entity.Department;
import com.example.hr.enums.BenefitStatus;
import com.example.hr.enums.BenefitType;
import com.example.hr.models.EmployeeBenefit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface EmployeeBenefitRepository extends JpaRepository<EmployeeBenefit, Integer> {

    List<EmployeeBenefit> findByUserId(Integer userId);

    List<EmployeeBenefit> findByBenefitType(BenefitType type);

    List<EmployeeBenefit> findByStatus(BenefitStatus status);

    List<EmployeeBenefit> findByUserIdAndStatus(Integer userId, BenefitStatus status);

    @Query("SELECT b FROM EmployeeBenefit b WHERE b.endDate IS NOT NULL " +
           "AND b.endDate BETWEEN :start AND :end AND b.status = 'ACTIVE'")
    List<EmployeeBenefit> findExpiringSoon(@Param("start") LocalDate start,
                                            @Param("end") LocalDate end);

    @Query("SELECT b FROM EmployeeBenefit b WHERE b.endDate IS NOT NULL " +
           "AND b.endDate < :today AND b.status = 'ACTIVE'")
    List<EmployeeBenefit> findExpiredButActive(@Param("today") LocalDate today);

    @Query("SELECT b.benefitType, COUNT(b) FROM EmployeeBenefit b " +
           "WHERE b.status = 'ACTIVE' GROUP BY b.benefitType")
    List<Object[]> countActiveBenefitsByType();

    @Query("SELECT COALESCE(SUM(b.monetaryValue), 0) FROM EmployeeBenefit b " +
           "WHERE b.user.id = :userId AND b.status = 'ACTIVE'")
    BigDecimal sumActiveValueByUser(@Param("userId") Integer userId);

    @Query("SELECT COALESCE(SUM(b.monetaryValue), 0) FROM EmployeeBenefit b " +
           "WHERE b.status = 'ACTIVE'")
    BigDecimal sumTotalActiveBenefitCost();

    @Query("SELECT b.user.department.departmentName, SUM(b.monetaryValue) " +
           "FROM EmployeeBenefit b WHERE b.status = 'ACTIVE' " +
           "GROUP BY b.user.department.departmentName")
    List<Object[]> sumCostByDepartment();

    long countByStatus(BenefitStatus status);
}


