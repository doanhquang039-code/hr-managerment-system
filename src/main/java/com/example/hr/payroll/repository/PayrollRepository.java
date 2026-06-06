package com.example.hr.payroll.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.hr.payroll.entity.Payroll;
import com.example.hr.models.User;

public interface PayrollRepository extends JpaRepository<Payroll, Integer> {

    Optional<Payroll> findByUserIdAndMonthAndYear(Integer userId, Integer month, Integer year);

    List<Payroll> findByUser(User user);

    @Query("SELECT p FROM Payroll p LEFT JOIN FETCH p.user WHERE (:keyword IS NULL OR p.user.fullName LIKE %:keyword%)")
    List<Payroll> findAllWithUser(@Param("keyword") String keyword);
    
    // Advanced Analytics methods
    @Query("SELECT p FROM Payroll p WHERE (p.year > :startYear OR (p.year = :startYear AND p.month >= :startMonth)) AND (p.year < :endYear OR (p.year = :endYear AND p.month <= :endMonth))")
    List<Payroll> findByYearMonthBetween(@Param("startYear") int startYear, @Param("startMonth") int startMonth, @Param("endYear") int endYear, @Param("endMonth") int endMonth);
    
    List<Payroll> findTop100ByOrderByCreatedAtDesc();
    
    @Query("SELECT p FROM Payroll p WHERE p.user = :user AND ((p.year > :startYear OR (p.year = :startYear AND p.month >= :startMonth)) AND (p.year < :endYear OR (p.year = :endYear AND p.month <= :endMonth))) ORDER BY p.year DESC, p.month DESC")
    List<Payroll> findByUserAndYearMonthBetween(@Param("user") User user, @Param("startYear") int startYear, @Param("startMonth") int startMonth, @Param("endYear") int endYear, @Param("endMonth") int endMonth);
}