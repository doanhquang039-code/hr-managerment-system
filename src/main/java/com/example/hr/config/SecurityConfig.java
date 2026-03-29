package com.example.hr.config;

import com.example.hr.service.CustomOAuth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomOAuth2UserService oAuth2UserService;

    public SecurityConfig(CustomOAuth2UserService oAuth2UserService) {
        this.oAuth2UserService = oAuth2UserService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
    .requestMatchers(
        "/", "/index.html", "/login", "/login/**",
        "/css/**", "/js/**", "/images/**",
        "/oauth2/**",                    // ✅ Thêm dòng này
        "/login/oauth2/code/**"          // ✅ Thêm dòng này
    ).permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/manager/**").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers("/hiring/**").hasAnyRole("ADMIN", "HIRING")
                .requestMatchers("/videos/**").permitAll()
                .requestMatchers("/user1/**", "/user/**").authenticated()
                .anyRequest().permitAll()
            )
            .formLogin(login -> login
                .loginPage("/login")
                .passwordParameter("password")
                .defaultSuccessUrl("/home", true)
                .permitAll()
            )
            // ✅ Thêm OAuth2 vào đây
          .oauth2Login(oauth -> oauth
    .loginPage("/login")
    .userInfoEndpoint(u -> u.userService(oAuth2UserService))
    .defaultSuccessUrl("/user1/dashboard", true) // Nhảy thẳng vào dashboard nhân viên
)
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        return http.build();
    }
}
