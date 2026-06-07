package com.example.hr.controllers;

import com.example.hr.models.*;
import com.example.hr.service.AuthUserHelper;
import com.example.hr.service.NewAssetManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/assets/new")
@RequiredArgsConstructor
public class AssetNewController {
    
    private final NewAssetManagementService assetService;
    private final AuthUserHelper authUserHelper;
    
    // ===== User Views =====
    
    @GetMapping("/my-assets")
    @PreAuthorize("isAuthenticated()")
    public String myAssets(Authentication auth, Model model) {
        User user = authUserHelper.getCurrentUser(auth);
        model.addAttribute("assignments", assetService.getUserActiveAssignments(user));
        return "assets/my-assets";
    }
    
    // ===== Admin Views =====
    
    @GetMapping("/admin/list")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public String assetList(@RequestParam(required = false) String category,
                           @RequestParam(required = false) String search,
                           Model model) {
        if (search != null && !search.isBlank()) {
            model.addAttribute("assets", assetService.searchAssets(search));
        } else if (category != null && !category.isBlank()) {
            model.addAttribute("assets", assetService.getAssetsByCategory(category));
        } else {
            model.addAttribute("assets", assetService.getAllAssets());
        }
        
        model.addAttribute("availableAssets", assetService.getAvailableAssets());
        
        return "assets/admin/asset-list";
    }
    
    @GetMapping("/admin/new")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public String newAssetForm(Model model) {
        model.addAttribute("asset", new Asset());
        return "assets/admin/asset-form";
    }
    
    @PostMapping("/admin/save")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public String saveAsset(@ModelAttribute Asset asset, RedirectAttributes ra) {
        try {
            assetService.createAsset(asset);
            ra.addFlashAttribute("success", "Táº¡o tÃ i sáº£n thÃ nh cÃ´ng!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/assets/new/admin/list";
    }
    
    @PostMapping("/admin/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public String assignAsset(@RequestParam Integer assetId,
                             @RequestParam Integer userId,
                             @RequestParam String assignedDate,
                             @RequestParam(required = false) String notes,
                             Authentication auth,
                             RedirectAttributes ra) {
        try {
            User assignedBy = authUserHelper.getCurrentUser(auth);
            Asset asset = assetService.getAllAssets().stream()
                .filter(a -> a.getId().equals(assetId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Asset not found"));
            
            User user = new User();
            user.setId(userId);
            
            assetService.assignAsset(asset, user, assignedBy, LocalDate.parse(assignedDate), notes);
            ra.addFlashAttribute("success", "GÃ¡n tÃ i sáº£n thÃ nh cÃ´ng!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/assets/new/admin/list";
    }
    
    @PostMapping("/admin/return/{assignmentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public String returnAsset(@PathVariable Integer assignmentId,
                             @RequestParam String condition,
                             @RequestParam(required = false) String notes,
                             RedirectAttributes ra) {
        try {
            assetService.returnAsset(assignmentId, condition, notes);
            ra.addFlashAttribute("success", "Tráº£ tÃ i sáº£n thÃ nh cÃ´ng!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/assets/new/admin/list";
    }
    
    // ===== Maintenance =====
    
    @GetMapping("/admin/maintenance")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public String maintenanceList(Model model) {
        LocalDate nextMonth = LocalDate.now().plusMonths(1);
        model.addAttribute("upcoming", assetService.getUpcomingMaintenance(nextMonth));
        return "assets/admin/maintenance-list";
    }
    
    @PostMapping("/admin/maintenance/schedule")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public String scheduleMaintenance(@RequestParam Integer assetId,
                                     @RequestParam String type,
                                     @RequestParam String description,
                                     @RequestParam String maintenanceDate,
                                     @RequestParam(required = false) String nextMaintenanceDate,
                                     RedirectAttributes ra) {
        try {
            Asset asset = assetService.getAllAssets().stream()
                .filter(a -> a.getId().equals(assetId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Asset not found"));
            
            LocalDate nextDate = nextMaintenanceDate != null ? LocalDate.parse(nextMaintenanceDate) : null;
            assetService.scheduleMaintenance(asset, type, description, 
                                           LocalDate.parse(maintenanceDate), nextDate);
            ra.addFlashAttribute("success", "LÃªn lá»‹ch báº£o trÃ¬ thÃ nh cÃ´ng!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/assets/new/admin/maintenance";
    }
}


