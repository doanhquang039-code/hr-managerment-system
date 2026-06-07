package com.example.hr.security;

import com.example.hr.system.service.SystemSettingService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.web.filter.OncePerRequestFilter;

public class LoginVerificationCodeFilter extends OncePerRequestFilter {

    public static final String PARAMETER_NAME = "verificationCode";
    public static final String SESSION_ATTRIBUTE_NAME = "LOGIN_VERIFICATION_CODE";

    private final SystemSettingService settingService;

    public LoginVerificationCodeFilter(SystemSettingService settingService) {
        this.settingService = settingService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !"/login".equals(request.getServletPath())
                || !"POST".equalsIgnoreCase(request.getMethod())
                || !settingService.getBoolean("LOGIN_VERIFICATION_ENABLED", true);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String submittedCode = normalize(request.getParameter(PARAMETER_NAME));
        HttpSession session = request.getSession(false);
        String expectedCode = session == null
                ? ""
                : normalize((String) session.getAttribute(SESSION_ATTRIBUTE_NAME));

        if (submittedCode.isBlank() || expectedCode.isBlank()
                || !constantTimeEquals(submittedCode, expectedCode)) {
            response.sendRedirect(request.getContextPath() + "/login?codeError");
            return;
        }

        session.removeAttribute(SESSION_ATTRIBUTE_NAME);
        filterChain.doFilter(request, response);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase();
    }

    private static boolean constantTimeEquals(String left, String right) {
        return MessageDigest.isEqual(
                left.getBytes(StandardCharsets.UTF_8),
                right.getBytes(StandardCharsets.UTF_8));
    }
}
