package com.example.hr.api;

import com.example.hr.models.*;
import com.example.hr.service.AuthUserHelper;
import com.example.hr.service.NewAssetManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
public class AssetApiController {
    
    private final NewAssetManagementService assetService;
    private final AuthUserHelper authUserHelper;
    
    @GetMapping("/my-assets")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMyAssets(Authentication auth) {
        User user = authUserHelper.getCurrentUser(auth);
        List<AssetAssignment> assignments = assetService.getUserActiveAssignments(user);
        
        return ResponseEntity.ok(assignments);
    }
    
    @GetMapping("/available")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<?> getAvailableAssets() {
        List<Asset> assets = assetService.getAvailableAssets();
        return ResponseEntity.ok(assets);
    }
    
    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<?> createAsset(@RequestBody Asset asset) {
        try {
            Asset created = assetService.createAsset(asset);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Táº¡o tÃ i sáº£n thÃ nh cÃ´ng");
            response.put("asset", created);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PostMapping("/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<?> assignAsset(@RequestBody Map<String, Object> request,
                                        Authentication auth) {
        try {
            User assignedBy = authUserHelper.getCurrentUser(auth);
            
            Integer assetId = Integer.parseInt(request.get("assetId").toString());
            Integer userId = Integer.parseInt(request.get("userId").toString());
            String notes = request.getOrDefault("notes", "").toString();
            
            Asset asset = new Asset();
            asset.setId(assetId);
            
            User user = new User();
            user.setId(userId);
            
            AssetAssignment assignment = assetService.assignAsset(asset, user, assignedBy, LocalDate.now(), notes);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Giao tÃ i sáº£n thÃ nh cÃ´ng");
            response.put("assignment", assignment);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PostMapping("/return/{assignmentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<?> returnAsset(@PathVariable Integer assignmentId,
                                        @RequestBody Map<String, Object> request) {
        try {
            String condition = request.get("condition").toString();
            String notes = request.getOrDefault("notes", "").toString();
            
            AssetAssignment assignment = assetService.returnAsset(assignmentId, condition, notes);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Thu há»“i tÃ i sáº£n thÃ nh cÃ´ng");
            response.put("assignment", assignment);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PostMapping("/maintenance/schedule")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<?> scheduleMaintenance(@RequestBody Map<String, Object> request) {
        try {
            Integer assetId = Integer.parseInt(request.get("assetId").toString());
            String type = request.get("type").toString();
            LocalDate scheduledDate = LocalDate.parse(request.get("scheduledDate").toString());
            String description = request.getOrDefault("description", "").toString();
            
            Asset asset = new Asset();
            asset.setId(assetId);
            
            LocalDate nextMaintenanceDate = scheduledDate.plusMonths(6); // Default 6 months
            AssetMaintenance maintenance = assetService.scheduleMaintenance(asset, type, description, scheduledDate, nextMaintenanceDate);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "LÃªn lá»‹ch báº£o trÃ¬ thÃ nh cÃ´ng");
            response.put("maintenance", maintenance);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PostMapping("/maintenance/{maintenanceId}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<?> completeMaintenance(@PathVariable Integer maintenanceId,
                                                @RequestBody Map<String, Object> request) {
        try {
            String notes = request.getOrDefault("notes", "").toString();
            
            AssetMaintenance maintenance = assetService.completeMaintenance(maintenanceId, notes);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "HoÃ n thÃ nh báº£o trÃ¬");
            response.put("maintenance", maintenance);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @GetMapping("/maintenance/upcoming")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<?> getUpcomingMaintenance() {
        LocalDate thirtyDaysFromNow = LocalDate.now().plusDays(30);
        List<AssetMaintenance> maintenances = assetService.getUpcomingMaintenance(thirtyDaysFromNow);
        return ResponseEntity.ok(maintenances);
    }
}


