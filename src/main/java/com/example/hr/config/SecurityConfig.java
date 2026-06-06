package com.example.hr.config;



import com.example.hr.security.LoginVerificationCodeFilter;
import com.example.hr.service.CustomOAuth2UserService;
import com.example.hr.service.SystemSettingService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomOAuth2UserService oAuth2UserService;
    private final SystemSettingService settingService;
    private final ObjectProvider<ClientRegistrationRepository> clientRegistrationRepository;

    public SecurityConfig(CustomOAuth2UserService oAuth2UserService,
                          SystemSettingService settingService,
                          ObjectProvider<ClientRegistrationRepository> clientRegistrationRepository) {
        this.oAuth2UserService = oAuth2UserService;
        this.settingService = settingService;
        this.clientRegistrationRepository = clientRegistrationRepository;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .ignoringRequestMatchers(
                        "/admin/payments/ipn/momo", "/admin/payment/ipn/momo",
                        "/admin/payments/ipn/vnpay", "/admin/payment/ipn/vnpay",
                        "/admin/course/save",
                        "/api/lifestyle/health-insights"))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/", "/index.html", "/login", "/login/**",
                    "/forgot-password", "/reset-password",
                    "/css/**", "/js/**", "/images/**",
                    "/oauth2/**", "/login/oauth2/code/**",
                    "/admin/payments/callback/**", "/admin/payments/ipn/**",
                    "/admin/payment/callback/**", "/admin/payment/ipn/**"
                ).permitAll()
                .requestMatchers(
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/swagger-ui.html"
                ).permitAll()
                .requestMatchers(
                    "/admin/departments/**",
                    "/admin/leaves/**",
                    "/admin/attendance/**",
                    "/admin/tasks/**",
                    "/admin/assignments/**",
                    "/admin/reviews/**",
                    "/admin/contracts/**",
                    "/admin/kpi/**",
                    "/admin/expenses/**",
                    "/admin/skills/**",
                    "/admin/documents/**",
                    "/api/kpi/**",
                    "/api/expenses/**",
                    "/api/skills/**",
                    "/api/shifts/**"
                ).hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers("/manager/**").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers("/hiring/**").hasAnyRole("ADMIN", "HIRING", "MANAGER")
                .requestMatchers("/admin/videos/**").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers("/admin/cache/**").hasRole("ADMIN")
                .requestMatchers("/admin/cloud/**").hasRole("ADMIN")
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/videos/**").permitAll()
                .requestMatchers("/user1/**", "/user/**", "/notifications/**").authenticated()
                .anyRequest().authenticated()
            )
            .formLogin(login -> login
                .loginPage("/login")
                .passwordParameter("password")
                .defaultSuccessUrl("/home", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .addFilterBefore(
                new LoginVerificationCodeFilter(settingService),
                UsernamePasswordAuthenticationFilter.class
            );

        if (clientRegistrationRepository.getIfAvailable() != null) {
            http.oauth2Login(oauth -> oauth
                .loginPage("/login")
                .userInfoEndpoint(u -> u.userService(oAuth2UserService))
                .defaultSuccessUrl("/user1/dashboard", true)
            );
        }

        return http.build();
    }
}
