package com.example.hr.controllers;

import com.example.hr.service.PasswordResetService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

@Controller
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @GetMapping("/forgot-password")
    public String forgotPassword() {
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String requestReset(@RequestParam String account,
                               HttpServletRequest request,
                               RedirectAttributes redirectAttributes) {
        passwordResetService.requestReset(account, buildResetBaseUrl(request));
        redirectAttributes.addFlashAttribute("successMsg",
                "If the account exists and has an email, reset instructions have been sent.");
        return "redirect:/forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPassword(@RequestParam(required = false) String token, Model model) {
        if (!passwordResetService.isValidToken(token)) {
            model.addAttribute("invalidToken", true);
            return "auth/reset-password";
        }
        model.addAttribute("token", token);
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String submitReset(@RequestParam String token,
                              @RequestParam String newPassword,
                              @RequestParam String confirmPassword,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("token", token);
            model.addAttribute("errorMsg", "Password confirmation does not match.");
            return "auth/reset-password";
        }

        if (!passwordResetService.resetPassword(token, newPassword)) {
            model.addAttribute("invalidToken", true);
            return "auth/reset-password";
        }

        redirectAttributes.addFlashAttribute("successMsg", "Password updated. You can sign in now.");
        return "redirect:/login";
    }

    private String buildResetBaseUrl(HttpServletRequest request) {
        return UriComponentsBuilder.fromHttpUrl(request.getRequestURL().toString())
                .replacePath(request.getContextPath() + "/reset-password")
                .replaceQuery(null)
                .toUriString();
    }
}


