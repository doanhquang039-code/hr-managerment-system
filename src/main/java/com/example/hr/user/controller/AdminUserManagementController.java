package com.example.hr.user.controller;

import com.example.hr.service.BulkOperationService;
import com.example.hr.user.service.UserService;
import com.example.hr.enums.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Controller
@RequestMapping("/admin/users-advanced")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserManagementController {

    private final UserService userService;
    private final BulkOperationService bulkOperationService;

    /**
     * Advanced user management page
     */
    @GetMapping
    public String advancedUserManagement(Model model) {
        var users = userService.getAllUsers();
        model.addAttribute("users", users);
        
        // User statistics
        long totalUsers = users.size();
        long activeUsers = users.stream().filter(u -> u.getStatus() == UserStatus.ACTIVE).count();
        long inactiveUsers = users.stream().filter(u -> u.getStatus() == UserStatus.INACTIVE).count();
        
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("activeUsers", activeUsers);
        model.addAttribute("inactiveUsers", inactiveUsers);

        return "admin/users-advanced";
    }

    /**
     * Import users from Excel
     */
    @PostMapping("/import")
    public String importUsers(
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes) {
        
        try {
            var result = bulkOperationService.importUsersFromExcel(file);
            
            redirectAttributes.addFlashAttribute("successMessage",
                    String.format("Imported %d users successfully. %d errors.",
                            result.getSuccessCount(), result.getErrorCount()));
            
            if (result.getErrorCount() > 0) {
                redirectAttributes.addFlashAttribute("errors", result.getErrors());
            }
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Failed to import users: " + e.getMessage());
        }

        return "redirect:/admin/users-advanced";
    }

    /**
     * Export users to Excel
     */
    @GetMapping("/export")
    public ResponseEntity<ByteArrayResource> exportUsers() {
        try {
            var users = userService.getAllUsers();
            ByteArrayOutputStream out = bulkOperationService.exportUsersToExcel(users);
            
            ByteArrayResource resource = new ByteArrayResource(out.toByteArray());
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=users_export.xlsx")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(resource.contentLength())
                    .body(resource);
                    
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Bulk update users
     */
    @PostMapping("/bulk-update")
    public String bulkUpdate(
            @RequestParam("userIds") List<Integer> userIds,
            @RequestParam(required = false) Integer departmentId,
            @RequestParam(required = false) Integer positionId,
            @RequestParam(required = false) UserStatus status,
            RedirectAttributes redirectAttributes) {
        
        try {
            var request = new BulkOperationService.BulkUpdateRequest(
                    departmentId, positionId, status);
            
            var result = bulkOperationService.bulkUpdateUsers(userIds, request);
            
            redirectAttributes.addFlashAttribute("successMessage",
                    String.format("Updated %d users successfully. %d errors.",
                            result.getSuccessCount(), result.getErrorCount()));
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Failed to update users: " + e.getMessage());
        }

        return "redirect:/admin/users-advanced";
    }

    /**
     * Bulk delete users
     */
    @PostMapping("/bulk-delete")
    public String bulkDelete(
            @RequestParam("userIds") List<Integer> userIds,
            RedirectAttributes redirectAttributes) {
        
        try {
            var result = bulkOperationService.bulkDeleteUsers(userIds);
            
            redirectAttributes.addFlashAttribute("successMessage",
                    String.format("Deleted %d users successfully. %d errors.",
                            result.getSuccessCount(), result.getErrorCount()));
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Failed to delete users: " + e.getMessage());
        }

        return "redirect:/admin/users-advanced";
    }

    /**
     * Bulk password reset
     */
    @PostMapping("/bulk-password-reset")
    public String bulkPasswordReset(
            @RequestParam("userIds") List<Integer> userIds,
            @RequestParam(defaultValue = "123456") String defaultPassword,
            RedirectAttributes redirectAttributes) {
        
        try {
            var result = bulkOperationService.bulkPasswordReset(userIds, defaultPassword);
            
            redirectAttributes.addFlashAttribute("successMessage",
                    String.format("Reset passwords for %d users successfully. %d errors.",
                            result.getSuccessCount(), result.getErrorCount()));
            
            redirectAttributes.addFlashAttribute("resetEmails", result.getSuccessfulResets());
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Failed to reset passwords: " + e.getMessage());
        }

        return "redirect:/admin/users-advanced";
    }

    /**
     * Download import template
     */
    @GetMapping("/import-template")
    public ResponseEntity<ByteArrayResource> downloadImportTemplate() {
        try {
            // Create empty user list for template
            List<com.example.hr.models.User> emptyUsers = List.of();
            ByteArrayOutputStream out = bulkOperationService.exportUsersToExcel(emptyUsers);
            
            ByteArrayResource resource = new ByteArrayResource(out.toByteArray());
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=user_import_template.xlsx")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(resource.contentLength())
                    .body(resource);
                    
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
