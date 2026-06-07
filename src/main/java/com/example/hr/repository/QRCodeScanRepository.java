package com.example.hr.repository;

import com.example.hr.models.QRCode;
import com.example.hr.models.QRCodeScan;
import com.example.hr.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface QRCodeScanRepository extends JpaRepository<QRCodeScan, Integer> {
    
    List<QRCodeScan> findByQrCodeOrderByScannedAtDesc(QRCode qrCode);
    
    List<QRCodeScan> findByUserOrderByScannedAtDesc(User user);
    
    List<QRCodeScan> findByQrCodeAndUserOrderByScannedAtDesc(QRCode qrCode, User user);
    
    @Query("SELECT s FROM QRCodeScan s WHERE s.scannedAt BETWEEN :start AND :end ORDER BY s.scannedAt DESC")
    List<QRCodeScan> findByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT s FROM QRCodeScan s WHERE s.user = :user AND s.scannedAt BETWEEN :start AND :end ORDER BY s.scannedAt DESC")
    List<QRCodeScan> findByUserAndDateRange(@Param("user") User user, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT s FROM QRCodeScan s WHERE s.qrCode = :qrCode AND s.scannedAt BETWEEN :start AND :end ORDER BY s.scannedAt DESC")
    List<QRCodeScan> findByQrCodeAndDateRange(@Param("qrCode") QRCode qrCode, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    long countByQrCode(QRCode qrCode);
    
    long countByUser(User user);
    
    @Query("SELECT COUNT(s) FROM QRCodeScan s WHERE s.scannedAt BETWEEN :start AND :end")
    long countByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT s FROM QRCodeScan s WHERE s.user = :user AND DATE(s.scannedAt) = DATE(:date) ORDER BY s.scannedAt DESC")
    List<QRCodeScan> findByUserAndDate(@Param("user") User user, @Param("date") LocalDateTime date);
}


