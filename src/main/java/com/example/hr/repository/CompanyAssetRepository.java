package com.example.hr.repository;

import com.example.hr.enums.AssetStatus;
import com.example.hr.models.CompanyAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface CompanyAssetRepository extends JpaRepository<CompanyAsset, Integer> {
    List<CompanyAsset> findByStatus(AssetStatus status);
    List<CompanyAsset> findByAssetNameContainingIgnoreCaseOrAssetCodeContainingIgnoreCase(String name, String code);

    long countByStatus(AssetStatus status);

    @Query("SELECT a.status, COUNT(a) FROM CompanyAsset a GROUP BY a.status")
    List<Object[]> countByStatus();

    @Query("SELECT a.category, SUM(a.purchasePrice) FROM CompanyAsset a GROUP BY a.category")
    List<Object[]> sumValueByCategory();

    @Query("SELECT a FROM CompanyAsset a WHERE a.warrantyExpiry BETWEEN :startDate AND :endDate")
    List<CompanyAsset> findWarrantyExpiringSoon(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT a FROM CompanyAsset a WHERE a.status = 'AVAILABLE'")
    List<CompanyAsset> findAvailableAssets();

    @Query("SELECT a FROM CompanyAsset a WHERE LOWER(a.assetName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(a.assetCode) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<CompanyAsset> searchByKeyword(@Param("keyword") String keyword);

    boolean existsByAssetCode(String assetCode);

    @Query("SELECT COALESCE(SUM(a.purchasePrice), 0) FROM CompanyAsset a")
    BigDecimal sumTotalAssetValue();

    @Query("SELECT a.category, COUNT(a) FROM CompanyAsset a GROUP BY a.category")
    List<Object[]> countByCategory();
}

