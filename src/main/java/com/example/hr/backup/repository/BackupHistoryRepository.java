package com.example.hr.backup.repository;

import com.example.hr.backup.entity.BackupHistory;
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
public interface BackupHistoryRepository extends JpaRepository<BackupHistory, Integer> {

    List<BackupHistory> findAllByOrderByCreatedAtDesc();

    Page<BackupHistory> findByIsDeletedFalseOrderByCreatedAtDesc(Pageable pageable);

    List<BackupHistory> findByStatusAndIsDeletedFalseOrderByCreatedAtDesc(String status);

    List<BackupHistory> findByCreatedByAndIsDeletedFalseOrderByCreatedAtDesc(User createdBy);

    @Query("SELECT b FROM BackupHistory b WHERE b.isDeleted = false AND b.createdAt BETWEEN :startDate AND :endDate ORDER BY b.createdAt DESC")
    List<BackupHistory> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                        @Param("endDate") LocalDateTime endDate);

    @Query("SELECT b FROM BackupHistory b WHERE b.status = 'COMPLETED' AND b.isDeleted = false ORDER BY b.createdAt DESC")
    List<BackupHistory> findSuccessfulBackups();

    @Query("SELECT b FROM BackupHistory b WHERE b.status = 'FAILED' AND b.isDeleted = false ORDER BY b.createdAt DESC")
    List<BackupHistory> findFailedBackups();

    @Query("SELECT b FROM BackupHistory b WHERE b.isScheduled = true AND b.isDeleted = false ORDER BY b.createdAt DESC")
    List<BackupHistory> findScheduledBackups();

    @Query("SELECT SUM(b.fileSize) FROM BackupHistory b WHERE b.status = 'COMPLETED' AND b.isDeleted = false")
    Long getTotalBackupSize();

    @Query("SELECT COUNT(b) FROM BackupHistory b WHERE b.status = 'COMPLETED' AND b.isDeleted = false")
    long countSuccessfulBackups();

    @Query("SELECT COUNT(b) FROM BackupHistory b WHERE b.status = 'FAILED' AND b.isDeleted = false")
    long countFailedBackups();

    @Query("SELECT b FROM BackupHistory b WHERE b.createdAt < :expiryDate AND b.isDeleted = false")
    List<BackupHistory> findExpiredBackups(@Param("expiryDate") LocalDateTime expiryDate);

    List<BackupHistory> findByBackupTypeAndIsDeletedFalseOrderByCreatedAtDesc(String backupType);

    List<BackupHistory> findByStorageLocationAndIsDeletedFalseOrderByCreatedAtDesc(String storageLocation);
}


