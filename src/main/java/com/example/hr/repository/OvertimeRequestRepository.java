package com.example.hr.repository;

import com.example.hr.models.OvertimeRequest;
import com.example.hr.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface OvertimeRequestRepository extends JpaRepository<OvertimeRequest, Integer> {
    
    List<OvertimeRequest> findByUser(User user);

    List<OvertimeRequest> findByUserOrderByCreatedAtDesc(User user);
    
    List<OvertimeRequest> findByStatus(String status);

    List<OvertimeRequest> findByStatusOrderByCreatedAtDesc(String status);
    
    List<OvertimeRequest> findByUserAndStatus(User user, String status);
    
    List<OvertimeRequest> findByOvertimeDateBetween(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT SUM(o.totalHours) FROM OvertimeRequest o WHERE o.user = :user AND o.status = 'APPROVED' AND o.overtimeDate BETWEEN :startDate AND :endDate")
    Double getTotalApprovedHours(@Param("user") User user, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    long countByStatus(String status);
}


