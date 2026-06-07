package com.example.hr.service;

import com.example.hr.models.User;
import com.example.hr.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

/**
 * Helper dÃ¹ng chung Ä‘á»ƒ resolve User tá»« Authentication,
 * há»— trá»£ cáº£ form login láº«n OAuth2 (Google, Facebook, ...).
 *
 * Váº¥n Ä‘á»: khi login báº±ng Google, auth.getName() tráº£ vá» sub ID sá»‘
 * (vÃ­ dá»¥ "111980037291323291428"), khÃ´ng pháº£i username hay email.
 * Cáº§n láº¥y email tá»« OAuth2User attributes Ä‘á»ƒ tÃ¬m trong DB.
 */
@Component
public class AuthUserHelper {

    @Autowired
    private UserRepository userRepository;

    /**
     * Láº¥y User tá»« Authentication.
     * - OAuth2 login â†’ láº¥y email tá»« OAuth2 attributes
     * - Form login â†’ tÃ¬m theo username, fallback sang email
     *
     * @return User hoáº·c null náº¿u khÃ´ng tÃ¬m tháº¥y
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
            // Fallback: dÃ¹ng sub ID náº¿u khÃ´ng cÃ³ email (Zalo, TikTok)
            String sub = oAuth2User.getName();
            String syntheticEmail = sub + "@" + ((OAuth2AuthenticationToken) auth).getAuthorizedClientRegistrationId() + ".com";
            return userRepository.findByEmail(syntheticEmail).orElse(null);
        }

        // Form login thÆ°á»ng â†’ tÃ¬m theo username trÆ°á»›c, sau Ä‘Ã³ theo email
        String principal = auth.getName();
        return userRepository.findByUsername(principal)
                .orElseGet(() -> userRepository.findByEmail(principal).orElse(null));
    }
}


