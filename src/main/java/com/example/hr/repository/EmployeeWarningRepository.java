package com.example.hr.repository;


import com.example.hr.department.entity.Department;
import com.example.hr.enums.WarningLevel;
import com.example.hr.models.EmployeeWarning;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EmployeeWarningRepository extends JpaRepository<EmployeeWarning, Integer> {

    List<EmployeeWarning> findByUserId(Integer userId);

    List<EmployeeWarning> findByWarningLevel(WarningLevel level);

    List<EmployeeWarning> findByUserIdOrderByIssuedDateDesc(Integer userId);

    @Query("SELECT w FROM EmployeeWarning w WHERE w.user.id = :userId " +
           "AND (w.expiryDate IS NULL OR w.expiryDate >= :today)")
    List<EmployeeWarning> findActiveWarnings(@Param("userId") Integer userId,
                                              @Param("today") LocalDate today);

    @Query("SELECT COUNT(w) FROM EmployeeWarning w WHERE w.user.id = :userId " +
           "AND w.warningLevel = :level " +
           "AND (w.expiryDate IS NULL OR w.expiryDate >= :today)")
    long countActiveByUserAndLevel(@Param("userId") Integer userId,
                                    @Param("level") WarningLevel level,
                                    @Param("today") LocalDate today);

    @Query("SELECT w FROM EmployeeWarning w WHERE w.isAcknowledged = false " +
           "AND w.issuedDate < :cutoffDate ORDER BY w.issuedDate ASC")
    List<EmployeeWarning> findUnacknowledgedBefore(@Param("cutoffDate") LocalDate cutoffDate);

    @Query("SELECT w.warningLevel, COUNT(w) FROM EmployeeWarning w GROUP BY w.warningLevel")
    List<Object[]> countByWarningLevel();

    @Query("SELECT w.user.department.departmentName, COUNT(w) FROM EmployeeWarning w " +
           "GROUP BY w.user.department.departmentName ORDER BY COUNT(w) DESC")
    List<Object[]> countByDepartment();

    long countByIsAcknowledged(boolean acknowledged);

    @Query("SELECT COUNT(DISTINCT w.user.id) FROM EmployeeWarning w " +
           "WHERE w.warningLevel IN ('FINAL', 'TERMINATION') " +
           "AND (w.expiryDate IS NULL OR w.expiryDate >= :today)")
    long countEmployeesWithSevereWarnings(@Param("today") LocalDate today);

    List<EmployeeWarning> findAllByOrderByIssuedDateDesc(Pageable pageable);
}


