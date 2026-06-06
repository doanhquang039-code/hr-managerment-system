package com.example.hr.service;

import com.example.hr.models.User;
import com.example.hr.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

/**
 * Helper dùng chung để resolve User từ Authentication,
 * hỗ trợ cả form login lẫn OAuth2 (Google, Facebook, ...).
 *
 * Vấn đề: khi login bằng Google, auth.getName() trả về sub ID số
 * (ví dụ "111980037291323291428"), không phải username hay email.
 * Cần lấy email từ OAuth2User attributes để tìm trong DB.
 */
@Component
public class AuthUserHelper {

    @Autowired
    private UserRepository userRepository;

    /**
     * Lấy User từ Authentication.
     * - OAuth2 login → lấy email từ OAuth2 attributes
     * - Form login → tìm theo username, fallback sang email
     *
     * @return User hoặc null nếu không tìm thấy
     */
    public User getCurrentUser(Authentication auth) {
        if (auth == null) return null;

        // OAuth2 login (Google, Facebook, ...)
        if (auth instanceof OAuth2AuthenticationToken) {
            OAuth2User oAuth2User = ((OAuth2AuthenticationToken) auth).getPrincipal();
            String email = oAuth2User.getAttribute("email");
            if (email != null) {
                return userRepository.findByEmail(email).orElse(null);
            }
            // Fallback: dùng sub ID nếu không có email (Zalo, TikTok)
            String sub = oAuth2User.getName();
            String syntheticEmail = sub + "@" + ((OAuth2AuthenticationToken) auth).getAuthorizedClientRegistrationId() + ".com";
            return userRepository.findByEmail(syntheticEmail).orElse(null);
        }

        // Form login thường → tìm theo username trước, sau đó theo email
        String principal = auth.getName();
        return userRepository.findByUsername(principal)
                .orElseGet(() -> userRepository.findByEmail(principal).orElse(null));
    }
}
