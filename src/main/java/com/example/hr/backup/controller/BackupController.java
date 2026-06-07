package com.example.hr.backup.controller;

import com.example.hr.backup.service.BackupService;
import com.example.hr.backup.service.RestoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;

@Controller
@RequestMapping("/admin/backup")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class BackupController {

    private final BackupService backupService;
    private final RestoreService restoreService;

    /**
     * Backup & restore page
     */
    @GetMapping
    public String backupRestore(Model model) {
        var backups = backupService.getAllBackups();
        var backupStats = backupService.getBackupStatistics();
        
        model.addAttribute("backups", backups);
        model.addAttribute("backupStats", backupStats);

        return "admin/backup-restore";
    }

    /**
     * Create full backup
     */
    @PostMapping("/create-full")
    public String createFullBackup(
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        
        try {
            String username = authentication.getName();
            // Get user ID from username
            var user = backupService.getUserByUsername(username);
            var backup = backupService.createFullBackup(user.getId());
            
            redirectAttributes.addFlashAttribute("successMessage",
                    "Full backup created successfully: " + backup.getBackupName());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Failed to create backup: " + e.getMessage());
        }

        return "redirect:/admin/backup";
    }

    /**
     * Create database backup
     */
    @PostMapping("/create-database")
    public String createDatabaseBackup(
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        
        try {
            String username = authentication.getName();
            // Get user ID from username
            var user = backupService.getUserByUsername(username);
            var backup = backupService.createDatabaseBackup(user.getId());
            
            redirectAttributes.addFlashAttribute("successMessage",
                    "Database backup created successfully: " + backup.getBackupName());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Failed to create database backup: " + e.getMessage());
        }

        return "redirect:/admin/backup";
    }

    /**
     * Download backup file
     */
    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadBackup(@PathVariable Integer id) {
        try {
            File backupFile = backupService.getBackupFile(id);
            Resource resource = new FileSystemResource(backupFile);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + backupFile.getName() + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(backupFile.length())
                    .body(resource);
                    
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete backup
     */
    @PostMapping("/delete/{id}")
    public String deleteBackup(
            @PathVariable Integer id,
            RedirectAttributes redirectAttributes) {
        
        try {
            backupService.deleteBackup(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Backup deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Failed to delete backup: " + e.getMessage());
        }

        return "redirect:/admin/backup";
    }

    /**
     * Restore from backup
     */
    @PostMapping("/restore/{id}")
    public String restoreFromBackup(
            @PathVariable Integer id,
            @RequestParam(defaultValue = "false") boolean confirmed,
            RedirectAttributes redirectAttributes) {
        
        if (!confirmed) {
            redirectAttributes.addFlashAttribute("warningMessage",
                    "Please confirm that you want to restore from this backup. This action cannot be undone.");
            redirectAttributes.addFlashAttribute("backupIdToRestore", id);
            return "redirect:/admin/backup";
        }

        try {
            // Validate backup first
            boolean isValid = restoreService.validateBackup(id);
            
            if (!isValid) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Backup validation failed. Cannot restore from this backup.");
                return "redirect:/admin/backup";
            }

            // Perform restore
            boolean success = restoreService.restoreFromBackup(id);
            
            if (success) {
                redirectAttributes.addFlashAttribute("successMessage",
                        "System restored successfully from backup");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Failed to restore from backup");
            }
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Failed to restore from backup: " + e.getMessage());
        }

        return "redirect:/admin/backup";
    }

    /**
     * Validate backup
     */
    @GetMapping("/validate/{id}")
    @ResponseBody
    public ResponseEntity<String> validateBackup(@PathVariable Integer id) {
        try {
            boolean isValid = restoreService.validateBackup(id);
            
            if (isValid) {
                return ResponseEntity.ok("Backup is valid and can be restored");
            } else {
                return ResponseEntity.badRequest().body("Backup validation failed");
            }
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error validating backup: " + e.getMessage());
        }
    }
}


