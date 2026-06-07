package com.example.hr.service;

import com.example.hr.models.*;
import com.example.hr.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class NewAssetManagementService {
    
    private final AssetRepository assetRepository;
    private final AssetAssignmentRepository assignmentRepository;
    private final AssetMaintenanceRepository maintenanceRepository;
    
    // ===== Asset Management =====
    
    /**
     * Create asset
     */
    public Asset createAsset(Asset asset) {
        return assetRepository.save(asset);
    }
    
    /**
     * Get all assets
     */
    @Transactional(readOnly = true)
    public List<Asset> getAllAssets() {
        return assetRepository.findAll();
    }
    
    /**
     * Get available assets
     */
    @Transactional(readOnly = true)
    public List<Asset> getAvailableAssets() {
        return assetRepository.findAvailableAssets();
    }
    
    /**
     * Get assets by category
     */
    @Transactional(readOnly = true)
    public List<Asset> getAssetsByCategory(String category) {
        return assetRepository.findByCategory(category);
    }
    
    /**
     * Search assets
     */
    @Transactional(readOnly = true)
    public List<Asset> searchAssets(String keyword) {
        return assetRepository.findByNameContainingIgnoreCaseOrAssetCodeContainingIgnoreCase(keyword, keyword);
    }
    
    /**
     * Update asset status
     */
    public Asset updateAssetStatus(Integer assetId, String status) {
        Optional<Asset> assetOpt = assetRepository.findById(assetId);
        if (assetOpt.isEmpty()) {
            throw new RuntimeException("Asset not found");
        }
        
        Asset asset = assetOpt.get();
        asset.setStatus(status);
        return assetRepository.save(asset);
    }
    
    // ===== Asset Assignment =====
    
    /**
     * Assign asset to user
     */
    public AssetAssignment assignAsset(Asset asset, User user, User assignedBy, 
                                      LocalDate assignedDate, String notes) {
        // Check if asset is available
        if (!"AVAILABLE".equals(asset.getStatus())) {
            throw new RuntimeException("Asset is not available for assignment");
        }
        
        // Check if asset already assigned
        Optional<AssetAssignment> existingAssignment = 
            assignmentRepository.findByAssetAndStatus(asset, "ACTIVE");
        if (existingAssignment.isPresent()) {
            throw new RuntimeException("Asset is already assigned");
        }
        
        AssetAssignment assignment = new AssetAssignment();
        assignment.setAsset(asset);
        assignment.setUser(user);
        assignment.setAssignedBy(assignedBy);
        assignment.setAssignedDate(assignedDate);
        assignment.setAssignmentNotes(notes);
        assignment.setStatus("ACTIVE");
        
        // Update asset status
        asset.setStatus("ASSIGNED");
        assetRepository.save(asset);
        
        return assignmentRepository.save(assignment);
    }
    
    /**
     * Return asset
     */
    public AssetAssignment returnAsset(Integer assignmentId, String condition, String notes) {
        Optional<AssetAssignment> assignmentOpt = assignmentRepository.findById(assignmentId);
        if (assignmentOpt.isEmpty()) {
            throw new RuntimeException("Assignment not found");
        }
        
        AssetAssignment assignment = assignmentOpt.get();
        if (!"ACTIVE".equals(assignment.getStatus())) {
            throw new RuntimeException("Assignment is not active");
        }
        
        assignment.setStatus("RETURNED");
        assignment.setReturnDate(LocalDate.now());
        assignment.setReturnCondition(condition);
        assignment.setReturnNotes(notes);
        
        // Update asset status
        Asset asset = assignment.getAsset();
        asset.setStatus("AVAILABLE");
        asset.setCondition(condition);
        assetRepository.save(asset);
        
        return assignmentRepository.save(assignment);
    }
    
    /**
     * Get user's active assignments
     */
    @Transactional(readOnly = true)
    public List<AssetAssignment> getUserActiveAssignments(User user) {
        return assignmentRepository.findActiveAssignmentsByUser(user);
    }
    
    /**
     * Get all assignments for an asset
     */
    @Transactional(readOnly = true)
    public List<AssetAssignment> getAssetAssignments(Asset asset) {
        return assignmentRepository.findByAsset(asset);
    }
    
    // ===== Asset Maintenance =====
    
    /**
     * Schedule maintenance
     */
    public AssetMaintenance scheduleMaintenance(Asset asset, String type, String description,
                                               LocalDate maintenanceDate, LocalDate nextMaintenanceDate) {
        AssetMaintenance maintenance = new AssetMaintenance();
        maintenance.setAsset(asset);
        maintenance.setType(type);
        maintenance.setDescription(description);
        maintenance.setMaintenanceDate(maintenanceDate);
        maintenance.setNextMaintenanceDate(nextMaintenanceDate);
        maintenance.setStatus("SCHEDULED");
        
        // Update asset status
        asset.setStatus("MAINTENANCE");
        assetRepository.save(asset);
        
        return maintenanceRepository.save(maintenance);
    }
    
    /**
     * Complete maintenance
     */
    public AssetMaintenance completeMaintenance(Integer maintenanceId, String notes) {
        Optional<AssetMaintenance> maintenanceOpt = maintenanceRepository.findById(maintenanceId);
        if (maintenanceOpt.isEmpty()) {
            throw new RuntimeException("Maintenance record not found");
        }
        
        AssetMaintenance maintenance = maintenanceOpt.get();
        maintenance.setStatus("COMPLETED");
        maintenance.setNotes(notes);
        
        // Update asset status back to available
        Asset asset = maintenance.getAsset();
        asset.setStatus("AVAILABLE");
        assetRepository.save(asset);
        
        return maintenanceRepository.save(maintenance);
    }
    
    /**
     * Get upcoming maintenance
     */
    @Transactional(readOnly = true)
    public List<AssetMaintenance> getUpcomingMaintenance(LocalDate beforeDate) {
        return maintenanceRepository.findUpcomingMaintenance(beforeDate);
    }
    
    /**
     * Get maintenance history for asset
     */
    @Transactional(readOnly = true)
    public List<AssetMaintenance> getAssetMaintenanceHistory(Asset asset) {
        return maintenanceRepository.findByAsset(asset);
    }
}


