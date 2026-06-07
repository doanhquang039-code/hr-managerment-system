package com.example.hr.repository;

import com.example.hr.models.PerformanceReview;
import com.example.hr.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PerformanceReviewRepository extends JpaRepository<PerformanceReview, Integer> {

    List<PerformanceReview> findByEmployee(User employee);

    List<PerformanceReview> findByReviewer(User reviewer);

    @Query("SELECT p FROM PerformanceReview p JOIN FETCH p.employee LEFT JOIN FETCH p.reviewer ORDER BY p.reviewDate DESC")
    List<PerformanceReview> findAllWithUsers();

    @Query("SELECT p FROM PerformanceReview p JOIN FETCH p.employee LEFT JOIN FETCH p.reviewer " +
           "WHERE p.employee = :user ORDER BY p.reviewDate DESC")
    List<PerformanceReview> findByUserOrdered(@Param("user") User user);

    @Query("SELECT p FROM PerformanceReview p WHERE p.reviewCycle = :cycle")
    List<PerformanceReview> findByCycle(@Param("cycle") String cycle);
    
    // Advanced Analytics methods
    List<PerformanceReview> findTop10ByOrderByOverallScoreDesc();
    java.util.Optional<PerformanceReview> findTopByEmployeeOrderByReviewDateDesc(User employee);
}


