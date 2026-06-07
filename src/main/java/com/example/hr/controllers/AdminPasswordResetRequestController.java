package com.example.hr.controllers;

import com.example.hr.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/password-reset-requests")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminPasswordResetRequestController {

    private final PasswordResetService passwordResetService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("requests", passwordResetService.getResetRequests());
        model.addAttribute("pendingResetRequests", passwordResetService.countPendingRequests());
        return "admin/password-reset-requests";
    }

    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Integer id,
                          @RequestParam(required = false) String adminNote,
                          RedirectAttributes redirectAttributes) {
        try {
            String temporaryPassword = passwordResetService.approveManualRequest(id, adminNote);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Request approved. Temporary password: " + temporaryPassword);
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/password-reset-requests";
    }

    @PostMapping("/{id}/reject")
    public String reject(@PathVariable Integer id,
                         @RequestParam(required = false) String adminNote,
                         RedirectAttributes redirectAttributes) {
        try {
            passwordResetService.rejectManualRequest(id, adminNote);
            redirectAttributes.addFlashAttribute("successMessage", "Request rejected.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/password-reset-requests";
    }
}


