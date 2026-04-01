package com.example.hr.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.Set;

@Controller
public class HomeController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/")
    public String index() {
        // Trả về file index.html trong thư mục static
        // Spring Boot tự động nhận diện file index trong static là trang chủ
        return "forward:/index.html";
    }

    @GetMapping("/home")
    public String dashboard(Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login";
        }

        Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());

        if (roles.contains("ROLE_ADMIN")) {
            return "admin/dashboard";
        } else if (roles.contains("ROLE_MANAGER")) {
            return "manager/dashboard";
        } else if (roles.contains("ROLE_HIRING")) {
            return "hiring/dashboard";
        } else {
            return "user1/profile";
        }
    }
}