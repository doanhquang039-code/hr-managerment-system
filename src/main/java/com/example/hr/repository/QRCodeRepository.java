package com.example.hr.repository;

import com.example.hr.enums.QRCodeType;
import com.example.hr.models.QRCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface QRCodeRepository extends JpaRepository<QRCode, Integer> {
    
    Optional<QRCode> findByCode(String code);
    
    List<QRCode> findByType(QRCodeType type);
    
    List<QRCode> findByIsActiveTrue();
    
    List<QRCode> findByTypeAndIsActiveTrue(QRCodeType type);
    
    @Query("SELECT q FROM QRCode q WHERE q.isActive = true AND (q.expiresAt IS NULL OR q.expiresAt > :now)")
    List<QRCode> findAllValid(@Param("now") LocalDateTime now);
    
    @Query("SELECT q FROM QRCode q WHERE q.type = :type AND q.isActive = true AND (q.expiresAt IS NULL OR q.expiresAt > :now)")
    List<QRCode> findValidByType(@Param("type") QRCodeType type, @Param("now") LocalDateTime now);
    
    long countByType(QRCodeType type);
    
    long countByIsActiveTrue();
    
    @Query("SELECT q FROM QRCode q ORDER BY q.scanCount DESC")
    List<QRCode> findTopScanned();
    
    boolean existsByCode(String code);
}


