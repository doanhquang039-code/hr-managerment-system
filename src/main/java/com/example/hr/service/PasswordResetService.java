package com.example.hr.service;

import com.example.hr.models.PasswordResetToken;
import com.example.hr.models.User;
import com.example.hr.models.PasswordResetRequest;
import com.example.hr.repository.PasswordResetRequestRepository;
import com.example.hr.repository.PasswordResetTokenRepository;
import com.example.hr.user.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PasswordResetService {

    private static final int TOKEN_BYTES = 32;
    private static final int EXPIRATION_MINUTES = 15;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordResetRequestRepository requestRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailFacade emailFacade;
    private final AuthUserHelper authUserHelper;
    private final SecureRandom secureRandom = new SecureRandom();

    public PasswordResetService(UserRepository userRepository,
                                PasswordResetTokenRepository tokenRepository,
                                PasswordResetRequestRepository requestRepository,
                                PasswordEncoder passwordEncoder,
                                EmailFacade emailFacade,
                                AuthUserHelper authUserHelper) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.requestRepository = requestRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailFacade = emailFacade;
        this.authUserHelper = authUserHelper;
    }

    public void requestReset(String accountIdentifier, String resetBaseUrl) {
        if (accountIdentifier == null || accountIdentifier.isBlank()) {
            return;
        }

        String normalizedIdentifier = accountIdentifier.strip();
        Optional<User> userOptional = findUser(normalizedIdentifier);
        if (userOptional.isEmpty()) {
            if (!normalizedIdentifier.contains("@")) {
                createManualRequest(normalizedIdentifier, null, "USERNAME", null);
            }
            return;
        }

        User user = userOptional.get();
        if (!canSendResetEmail(user)) {
            createManualRequest(normalizedIdentifier, user, "NO_EMAIL", "Account has no usable email for self-service reset.");
            return;
        }

        if (!normalizedIdentifier.contains("@")) {
            createManualRequest(normalizedIdentifier, user, "USERNAME", "User requested reset by username. Verify identity before resetting.");
            return;
        }

        tokenRepository.findByUserAndUsedAtIsNull(user).forEach(token -> token.setUsedAt(LocalDateTime.now()));

        String rawToken = generateToken();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUser(user);
        resetToken.setTokenHash(hashToken(rawToken));
        resetToken.setExpiresAt(LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES));
        tokenRepository.save(resetToken);

        String resetLink = resetBaseUrl + "?token=" + rawToken;
        boolean sent = emailFacade.sendPasswordReset(user.getEmail(), user.getFullName(), resetLink);
        if (!sent) {
            createManualRequest(normalizedIdentifier, user, "EMAIL_FAILED",
                    "Email reset link could not be sent. Verify the employee manually or fix mail settings.");
        }
    }

    @Transactional(readOnly = true)
    public List<PasswordResetRequest> getResetRequests() {
        return requestRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public long countPendingRequests() {
        return requestRepository.countByStatus("PENDING");
    }

    public String approveManualRequest(Integer requestId, String adminNote) {
        PasswordResetRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Password reset request not found."));
        if (!"PENDING".equals(request.getStatus())) {
            throw new IllegalStateException("Request is already resolved.");
        }
        User user = request.getUser();
        if (user == null) {
            throw new IllegalStateException("No matching user account was found for this request.");
        }

        String temporaryPassword = generateTemporaryPassword();
        user.setPassword(passwordEncoder.encode(temporaryPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        request.setStatus("APPROVED");
        request.setAdminNote(adminNote);
        request.setResolvedAt(LocalDateTime.now());
        request.setResolvedBy(getCurrentUser());
        requestRepository.save(request);

        if (canSendResetEmail(user)) {
            emailFacade.sendPasswordChanged(user.getEmail(), user.getFullName());
        }
        return temporaryPassword;
    }

    public void rejectManualRequest(Integer requestId, String adminNote) {
        PasswordResetRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Password reset request not found."));
        if (!"PENDING".equals(request.getStatus())) {
            throw new IllegalStateException("Request is already resolved.");
        }
        request.setStatus("REJECTED");
        request.setAdminNote(adminNote);
        request.setResolvedAt(LocalDateTime.now());
        request.setResolvedBy(getCurrentUser());
        requestRepository.save(request);
    }

    @Transactional(readOnly = true)
    public boolean isValidToken(String rawToken) {
        return findUsableToken(rawToken).isPresent();
    }

    public boolean resetPassword(String rawToken, String newPassword) {
        if (newPassword == null || newPassword.length() < 6) {
            return false;
        }

        Optional<PasswordResetToken> tokenOptional = findUsableToken(rawToken);
        if (tokenOptional.isEmpty()) {
            return false;
        }

        PasswordResetToken resetToken = tokenOptional.get();
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        resetToken.setUsedAt(LocalDateTime.now());
        userRepository.save(user);
        tokenRepository.save(resetToken);

        if (canSendResetEmail(user)) {
            emailFacade.sendPasswordChanged(user.getEmail(), user.getFullName());
        }
        return true;
    }

    private Optional<User> findUser(String accountIdentifier) {
        return userRepository.findByUsername(accountIdentifier)
                .or(() -> userRepository.findByEmail(accountIdentifier));
    }

    private void createManualRequest(String accountIdentifier, User user, String source, String message) {
        PasswordResetRequest request = new PasswordResetRequest();
        request.setAccountIdentifier(accountIdentifier);
        request.setUser(user);
        request.setRequestSource(source);
        request.setRequesterMessage(message);
        requestRepository.save(request);
    }

    private Optional<PasswordResetToken> findUsableToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return Optional.empty();
        }
        return tokenRepository.findByTokenHash(hashToken(rawToken.strip()))
                .filter(token -> token.getUsedAt() == null)
                .filter(token -> token.getExpiresAt().isAfter(LocalDateTime.now()));
    }

    private boolean canSendResetEmail(User user) {
        String email = user.getEmail();
        return email != null
                && email.contains("@")
                && !email.endsWith("@zalo.com")
                && !email.endsWith("@tiktok.com");
    }

    private String generateToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String generateTemporaryPassword() {
        String alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";
        StringBuilder password = new StringBuilder(12);
        for (int i = 0; i < 12; i++) {
            password.append(alphabet.charAt(secureRandom.nextInt(alphabet.length())));
        }
        return password.toString();
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authUserHelper.getCurrentUser(authentication);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available.", ex);
        }
    }
}
