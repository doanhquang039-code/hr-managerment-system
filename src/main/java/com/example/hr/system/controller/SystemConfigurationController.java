package com.example.hr.system.controller;

import com.example.hr.system.entity.SystemConfiguration;
import com.example.hr.system.service.SystemConfigurationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/system-config")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class SystemConfigurationController {

    private final SystemConfigurationService configService;

    /**
     * System configuration page
     */
    @GetMapping
    public String systemConfiguration(Model model) {
        var allConfigs = configService.getAllConfigurations();
        
        // Group by category
        var configsByCategory = configService.getConfigurationsByCategory();
        
        model.addAttribute("configsByCategory", configsByCategory);
        model.addAttribute("allConfigs", allConfigs);

        return "admin/system-config";
    }

    /**
     * Get configuration by category
     */
    @GetMapping("/category/{category}")
    public String configurationByCategory(@PathVariable String category, Model model) {
        var configs = configService.getConfigurationsByCategory(category);
        model.addAttribute("category", category);
        model.addAttribute("configs", configs);
        return "admin/system-config-category";
    }

    /**
     * Update configuration
     */
    @PostMapping("/update")
    public String updateConfiguration(
            @RequestParam String key,
            @RequestParam String value,
            RedirectAttributes redirectAttributes) {
        
        try {
            configService.updateConfiguration(key, value);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Configuration updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Failed to update configuration: " + e.getMessage());
        }

        return "redirect:/admin/system-config";
    }

    /**
     * Create new configuration
     */
    @PostMapping("/create")
    public String createConfiguration(
            @RequestParam String category,
            @RequestParam String key,
            @RequestParam String value,
            @RequestParam(defaultValue = "STRING") String dataType,
            @RequestParam(required = false) String description,
            @RequestParam(defaultValue = "false") boolean isEncrypted,
            RedirectAttributes redirectAttributes) {
        
        try {
            SystemConfiguration config = new SystemConfiguration();
            config.setCategory(category);
            config.setKey(key);
            config.setValue(value);
            config.setDataType(dataType);
            config.setDescription(description);
            config.setIsEncrypted(isEncrypted);
            
            configService.createConfiguration(config);
            
            redirectAttributes.addFlashAttribute("successMessage",
                    "Configuration created successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Failed to create configuration: " + e.getMessage());
        }

        return "redirect:/admin/system-config";
    }

    /**
     * Delete configuration
     */
    @PostMapping("/delete/{id}")
    public String deleteConfiguration(
            @PathVariable Integer id,
            RedirectAttributes redirectAttributes) {
        
        try {
            configService.deleteConfiguration(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Configuration deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Failed to delete configuration: " + e.getMessage());
        }

        return "redirect:/admin/system-config";
    }

    /**
     * Bulk update configurations
     */
    @PostMapping("/bulk-update")
    public String bulkUpdateConfigurations(
            @RequestParam("keys[]") String[] keys,
            @RequestParam("values[]") String[] values,
            RedirectAttributes redirectAttributes) {
        
        try {
            for (int i = 0; i < keys.length; i++) {
                configService.updateConfiguration(keys[i], values[i]);
            }
            
            redirectAttributes.addFlashAttribute("successMessage",
                    String.format("Updated %d configurations successfully", keys.length));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Failed to update configurations: " + e.getMessage());
        }

        return "redirect:/admin/system-config";
    }

    /**
     * Reset to default values
     */
    @PostMapping("/reset-defaults")
    public String resetToDefaults(RedirectAttributes redirectAttributes) {
        try {
            configService.resetToDefaults();
            redirectAttributes.addFlashAttribute("successMessage",
                    "Configurations reset to defaults successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Failed to reset configurations: " + e.getMessage());
        }

        return "redirect:/admin/system-config";
    }
}


