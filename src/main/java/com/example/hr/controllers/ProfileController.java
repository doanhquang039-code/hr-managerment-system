package com.example.hr.controllers;

import com.example.hr.enums.Role;
import com.example.hr.models.User;
import com.example.hr.user.repository.UserRepository;
import com.example.hr.service.AuthUserHelper;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
public class ProfileController {

    private final AuthUserHelper authUserHelper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ProfileController(AuthUserHelper authUserHelper,
                             UserRepository userRepository,
                             PasswordEncoder passwordEncoder) {
        this.authUserHelper = authUserHelper;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/profile")
    public String profile(Authentication authentication, Model model) {
        User user = authUserHelper.getCurrentUser(authentication);
        if (user == null) {
            return "redirect:/login?error=user_not_found";
        }

        model.addAttribute("user", user);
        model.addAttribute("isAdmin", user.getRole() == Role.ADMIN);
        model.addAttribute("dashboardUrl", dashboardUrl(user.getRole()));
        model.addAttribute("profileBaseUrl", "/profile");
        return "profile";
    }

    @PostMapping("/profile/update-info")
    public String updateInfo(@RequestParam(required = false) String fullName,
                             @RequestParam(required = false) String email,
                             @RequestParam(required = false) String phoneNumber,
                             @RequestParam(required = false) String address,
                             @RequestParam(required = false) String dateOfBirth,
                             @RequestParam(required = false) String gender,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        User user = authUserHelper.getCurrentUser(authentication);
        if (user == null) {
            return "redirect:/login";
        }

        boolean isAdmin = user.getRole() == Role.ADMIN;
        if (isAdmin) {
            if (fullName != null && !fullName.isBlank()) {
                user.setFullName(fullName.trim());
            }
            if (email != null && !email.isBlank()) {
                String cleanEmail = email.trim();
                if (userRepository.existsByEmailAndIdNot(cleanEmail, user.getId())) {
                    redirectAttributes.addFlashAttribute("errorMsg", "Email nÃ y Ä‘Ã£ Ä‘Æ°á»£c tÃ i khoáº£n khÃ¡c sá»­ dá»¥ng.");
                    return "redirect:/profile";
                }
                user.setEmail(cleanEmail);
            }
            if (gender != null) {
                user.setGender(gender.trim());
            }
        }

        if (phoneNumber != null) {
            user.setPhoneNumber(phoneNumber.trim());
        }
        if (address != null) {
            user.setAddress(address.trim());
        }
        if (dateOfBirth != null && !dateOfBirth.isBlank()) {
            try {
                user.setDateOfBirth(LocalDate.parse(dateOfBirth));
            } catch (Exception ex) {
                redirectAttributes.addFlashAttribute("errorMsg", "NgÃ y sinh khÃ´ng há»£p lá»‡.");
                return "redirect:/profile";
            }
        }

        userRepository.save(user);
        redirectAttributes.addFlashAttribute("successMsg", "Cáº­p nháº­t há»“ sÆ¡ thÃ nh cÃ´ng.");
        return "redirect:/profile";
    }

    @PostMapping("/profile/change-password")
    public String changePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        User user = authUserHelper.getCurrentUser(authentication);
        if (user == null) {
            return "redirect:/login";
        }

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            redirectAttributes.addFlashAttribute("passwordError", "Máº­t kháº©u hiá»‡n táº¡i khÃ´ng Ä‘Ãºng.");
            return "redirect:/profile";
        }
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("passwordError", "Máº­t kháº©u má»›i vÃ  xÃ¡c nháº­n khÃ´ng khá»›p.");
            return "redirect:/profile";
        }
        if (newPassword.length() < 6) {
            redirectAttributes.addFlashAttribute("passwordError", "Máº­t kháº©u má»›i pháº£i cÃ³ Ã­t nháº¥t 6 kÃ½ tá»±.");
            return "redirect:/profile";
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("successMsg", "Äá»•i máº­t kháº©u thÃ nh cÃ´ng.");
        return "redirect:/profile";
    }

    private String dashboardUrl(Role role) {
        if (role == Role.ADMIN) return "/admin/dashboard";
        if (role == Role.MANAGER) return "/manager/dashboard";
        if (role == Role.HIRING) return "/hiring/dashboard";
        return "/user1/dashboard";
    }
}


