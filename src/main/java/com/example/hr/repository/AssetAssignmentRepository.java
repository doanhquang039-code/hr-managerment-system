package com.example.hr.repository;

import com.example.hr.models.Asset;
import com.example.hr.models.AssetAssignment;
import com.example.hr.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssetAssignmentRepository extends JpaRepository<AssetAssignment, Integer> {
    
    List<AssetAssignment> findByUser(User user);
    
    List<AssetAssignment> findByAsset(Asset asset);
    
    List<AssetAssignment> findByStatus(String status);
    
    List<AssetAssignment> findByUserAndStatus(User user, String status);
    
    Optional<AssetAssignment> findByAssetAndStatus(Asset asset, String status);
    
    @Query("SELECT a FROM AssetAssignment a WHERE a.user = :user AND a.status = 'ACTIVE' ORDER BY a.assignedDate DESC")
    List<AssetAssignment> findActiveAssignmentsByUser(User user);
    
    long countByUserAndStatus(User user, String status);
}


