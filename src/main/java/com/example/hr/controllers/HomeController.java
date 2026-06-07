package com.example.hr.controllers;

import com.example.hr.security.LoginVerificationCodeFilter;
import com.example.hr.system.service.SystemSettingService;
import jakarta.servlet.http.HttpSession;
import java.security.SecureRandom;
import java.util.Set;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final SystemSettingService settingService;

    public HomeController(SystemSettingService settingService) {
        this.settingService = settingService;
    }

    @GetMapping("/login")
    public String login(
            @RequestParam(value = "refreshCode", required = false) String refreshCode,
            HttpSession session,
            Model model
    ) {
        boolean verificationEnabled = settingService.getBoolean("LOGIN_VERIFICATION_ENABLED", true);
        model.addAttribute("verificationEnabled", verificationEnabled);

        if (verificationEnabled) {
            String verificationCode = (String) session.getAttribute(LoginVerificationCodeFilter.SESSION_ATTRIBUTE_NAME);
            if (refreshCode != null || verificationCode == null || verificationCode.isBlank()) {
                verificationCode = generateVerificationCode();
                session.setAttribute(LoginVerificationCodeFilter.SESSION_ATTRIBUTE_NAME, verificationCode);
            }
            model.addAttribute("verificationCode", verificationCode);
        } else {
            session.removeAttribute(LoginVerificationCodeFilter.SESSION_ATTRIBUTE_NAME);
        }
        return "login";
    }

    @GetMapping("/")
    public String index() {
        // Tráº£ vá» file index.html trong thÆ° má»¥c static
        // Spring Boot tá»± Ä‘á»™ng nháº­n diá»‡n file index trong static lÃ  trang chá»§
        return "forward:/index.html";
    }
    @GetMapping("/home")
    public String dashboard(Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login";
        }

        Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());

        if (roles.contains("ROLE_ADMIN")) {
            return "redirect:/admin/dashboard";
        } else if (roles.contains("ROLE_MANAGER")) {
            return "redirect:/manager/dashboard";
        } else if (roles.contains("ROLE_HIRING")) {
            return "redirect:/hiring/dashboard";
        } else {
            return "redirect:/user1/dashboard";
        }
    }

    private String generateVerificationCode() {
        String characters = settingService.getValue(
                "LOGIN_VERIFICATION_CHARACTERS",
                "ABCDEFGHJKLMNPQRSTUVWXYZ23456789");
        if (characters == null || characters.isBlank()) {
            characters = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        }

        int codeLength = settingService.getInt("LOGIN_VERIFICATION_CODE_LENGTH", 5, 4, 8);
        StringBuilder code = new StringBuilder(codeLength);
        for (int i = 0; i < codeLength; i++) {
            code.append(characters.charAt(SECURE_RANDOM.nextInt(characters.length())));
        }
        return code.toString();
    }
}


