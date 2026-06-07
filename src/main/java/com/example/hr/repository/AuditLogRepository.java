package com.example.hr.repository;

import com.example.hr.models.AuditLog;
import com.example.hr.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Integer> {

    Page<AuditLog> findAllByOrderByTimestampDesc(Pageable pageable);

    Page<AuditLog> findByUserOrderByTimestampDesc(User user, Pageable pageable);

    Page<AuditLog> findByActionContainingIgnoreCaseOrderByTimestampDesc(String action, Pageable pageable);

    Page<AuditLog> findByEntityTypeOrderByTimestampDesc(String entityType, Pageable pageable);

    @Query("SELECT a FROM AuditLog a WHERE a.timestamp BETWEEN :startDate AND :endDate ORDER BY a.timestamp DESC")
    Page<AuditLog> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                    @Param("endDate") LocalDateTime endDate, 
                                    Pageable pageable);

    @Query("SELECT a FROM AuditLog a WHERE a.user = :user AND a.timestamp BETWEEN :startDate AND :endDate ORDER BY a.timestamp DESC")
    Page<AuditLog> findByUserAndDateRange(@Param("user") User user,
                                          @Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate,
                                          Pageable pageable);

    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.status = 'FAILED'")
    long countFailedActions();

    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.timestamp >= :since")
    long countActionsSince(@Param("since") LocalDateTime since);

    @Query("SELECT a.action, COUNT(a) FROM AuditLog a GROUP BY a.action ORDER BY COUNT(a) DESC")
    List<Object[]> getTopActions();

    @Query("SELECT a.user, COUNT(a) FROM AuditLog a WHERE a.user IS NOT NULL GROUP BY a.user ORDER BY COUNT(a) DESC")
    List<Object[]> getTopUsers(Pageable pageable);

    void deleteByTimestampBefore(LocalDateTime date);

    // Additional methods for compatibility
    List<AuditLog> findTop10ByOrderByTimestampDesc();
    
    List<AuditLog> findByUserIdOrderByTimestampDesc(Integer userId);
}


