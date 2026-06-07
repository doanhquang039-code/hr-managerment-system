package com.example.hr.repository;

import com.example.hr.models.Asset;
import com.example.hr.models.AssetMaintenance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AssetMaintenanceRepository extends JpaRepository<AssetMaintenance, Integer> {
    
    List<AssetMaintenance> findByAsset(Asset asset);
    
    List<AssetMaintenance> findByStatus(String status);
    
    List<AssetMaintenance> findByType(String type);
    
    @Query("SELECT m FROM AssetMaintenance m WHERE m.nextMaintenanceDate <= :date AND m.status = 'COMPLETED'")
    List<AssetMaintenance> findUpcomingMaintenance(@Param("date") LocalDate date);
    
    List<AssetMaintenance> findByMaintenanceDateBetween(LocalDate startDate, LocalDate endDate);
}


