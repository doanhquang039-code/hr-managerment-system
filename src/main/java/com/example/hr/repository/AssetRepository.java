package com.example.hr.repository;

import com.example.hr.models.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Integer> {
    
    Optional<Asset> findByAssetCode(String assetCode);
    
    List<Asset> findByCategory(String category);
    
    List<Asset> findByStatus(String status);
    
    List<Asset> findByCondition(String condition);
    
    List<Asset> findByNameContainingIgnoreCaseOrAssetCodeContainingIgnoreCase(String name, String code);
    
    @Query("SELECT a FROM Asset a WHERE a.status = 'AVAILABLE' ORDER BY a.createdAt DESC")
    List<Asset> findAvailableAssets();
    
    long countByStatus(String status);
    
    long countByCategory(String category);
}


