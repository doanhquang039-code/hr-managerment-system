package com.example.hr.service;

import com.example.hr.dto.EmployeeWarningDTO;
import com.example.hr.enums.NotificationType;
import com.example.hr.enums.UserStatus;
import com.example.hr.enums.WarningLevel;
import com.example.hr.exception.BusinessValidationException;
import com.example.hr.exception.ResourceNotFoundException;
import com.example.hr.models.EmployeeWarning;
import com.example.hr.models.User;
import com.example.hr.repository.EmployeeWarningRepository;
import com.example.hr.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service quáº£n lÃ½ cáº£nh cÃ¡o / ká»· luáº­t nhÃ¢n viÃªn.
 * Bao gá»“m: issue warning, escalation logic, auto-terminate.
 */
@Service
@Transactional
public class WarningService {

    private static final Logger log = LoggerFactory.getLogger(WarningService.class);
    private static final int DEFAULT_EXPIRY_DAYS = 180; // Cáº£nh cÃ¡o háº¿t hiá»‡u lá»±c sau 6 thÃ¡ng
    private static final int MAX_WARNINGS_BEFORE_ESCALATION = 3;

    private final EmployeeWarningRepository warningRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public WarningService(EmployeeWarningRepository warningRepository,
                           UserRepository userRepository,
                           NotificationService notificationService) {
        this.warningRepository = warningRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    /**
     * Láº¥y táº¥t cáº£ cáº£nh cÃ¡o.
     */
    @Transactional(readOnly = true)
    public List<EmployeeWarning> getAllWarnings() {
        return warningRepository.findAll();
    }

    /**
     * Láº¥y cáº£nh cÃ¡o theo ID.
     */
    @Transactional(readOnly = true)
    public EmployeeWarning getWarningById(Integer id) {
        return warningRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cáº£nh cÃ¡o", id));
    }

    /**
     * Láº¥y cáº£nh cÃ¡o theo nhÃ¢n viÃªn.
     */
    @Transactional(readOnly = true)
    public List<EmployeeWarning> getWarningsByUser(Integer userId) {
        return warningRepository.findByUserIdOrderByIssuedDateDesc(userId);
    }

    /**
     * Láº¥y cáº£nh cÃ¡o Ä‘ang hiá»‡u lá»±c cá»§a nhÃ¢n viÃªn.
     */
    @Transactional(readOnly = true)
    public List<EmployeeWarning> getActiveWarnings(Integer userId) {
        return warningRepository.findActiveWarnings(userId, LocalDate.now());
    }

    /**
     * Ban hÃ nh cáº£nh cÃ¡o má»›i.
     * Tá»± Ä‘á»™ng escalation náº¿u Ä‘Ã£ cÃ³ nhiá»u cáº£nh cÃ¡o cÃ¹ng level.
     */
    public EmployeeWarning issueWarning(EmployeeWarningDTO dto) {
        User employee = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("NhÃ¢n viÃªn", dto.getUserId()));
        User issuer = userRepository.findById(dto.getIssuedById())
                .orElseThrow(() -> new ResourceNotFoundException("NgÆ°á»i ban hÃ nh", dto.getIssuedById()));

        // XÃ¡c Ä‘á»‹nh warning level (auto-escalation náº¿u cáº§n)
        WarningLevel level = dto.getWarningLevel();
        WarningLevel effectiveLevel = determineEffectiveLevel(dto.getUserId(), level);

        EmployeeWarning warning = new EmployeeWarning();
        warning.setUser(employee);
        warning.setIssuedBy(issuer);
        warning.setWarningLevel(effectiveLevel);
        warning.setReason(dto.getReason());
        warning.setDescription(dto.getDescription());
        warning.setIssuedDate(dto.getIssuedDate() != null ? dto.getIssuedDate() : LocalDate.now());
        warning.setExpiryDate(dto.getExpiryDate() != null ? dto.getExpiryDate()
                : LocalDate.now().plusDays(DEFAULT_EXPIRY_DAYS));
        warning.setAttachmentUrl(dto.getAttachmentUrl());
        warning.setIsAcknowledged(false);
        warning.setCreatedAt(LocalDateTime.now());

        EmployeeWarning saved = warningRepository.save(warning);

        // Auto-actions based on level
        handlePostWarningActions(saved);

        log.info("Warning issued: user={}, level={}, issuer={}",
                employee.getUsername(), effectiveLevel, issuer.getUsername());

        return saved;
    }

    /**
     * NhÃ¢n viÃªn xÃ¡c nháº­n Ä‘Ã£ Ä‘á»c cáº£nh cÃ¡o.
     */
    public EmployeeWarning acknowledgeWarning(Integer warningId) {
        EmployeeWarning warning = getWarningById(warningId);
        warning.acknowledge();
        return warningRepository.save(warning);
    }

    /**
     * XÃ³a cáº£nh cÃ¡o (chá»‰ admin).
     */
    public void deleteWarning(Integer id) {
        if (!warningRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cáº£nh cÃ¡o", id);
        }
        warningRepository.deleteById(id);
    }

    /**
     * Thá»‘ng kÃª cáº£nh cÃ¡o theo má»©c Ä‘á»™.
     */
    @Transactional(readOnly = true)
    public Map<WarningLevel, Long> countByWarningLevel() {
        return warningRepository.countByWarningLevel().stream()
                .collect(Collectors.toMap(
                        row -> (WarningLevel) row[0],
                        row -> (Long) row[1]
                ));
    }

    /**
     * Thá»‘ng kÃª cáº£nh cÃ¡o theo phÃ²ng ban.
     */
    @Transactional(readOnly = true)
    public Map<String, Long> countByDepartment() {
        return warningRepository.countByDepartment().stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> (Long) row[1]
                ));
    }

    /**
     * Äáº¿m nhÃ¢n viÃªn cÃ³ cáº£nh cÃ¡o nghiÃªm trá»ng.
     */
    @Transactional(readOnly = true)
    public long countEmployeesWithSevereWarnings() {
        return warningRepository.countEmployeesWithSevereWarnings(LocalDate.now());
    }

    /**
     * Kiá»ƒm tra vÃ  xá»­ lÃ½ cáº£nh cÃ¡o chÆ°a acknowledge sau 30 ngÃ y.
     */
    public List<EmployeeWarning> processUnacknowledgedWarnings() {
        LocalDate cutoff = LocalDate.now().minusDays(30);
        List<EmployeeWarning> unacknowledged = warningRepository.findUnacknowledgedBefore(cutoff);

        for (EmployeeWarning warning : unacknowledged) {
            if (warning.needsEscalation()) {
                log.warn("Warning {} needs escalation: user={}, level={}",
                        warning.getId(), warning.getUser().getUsername(), warning.getWarningLevel());
                // Táº¡o thÃ´ng bÃ¡o nháº¯c nhá»Ÿ
                try {
                    notificationService.createNotification(
                            warning.getUser(),
                            "Báº¡n cÃ³ cáº£nh cÃ¡o chÆ°a xÃ¡c nháº­n tá»« " + warning.getIssuedDate(),
                            NotificationType.WARNING,
                            "/user1/warnings"
                    );
                } catch (Exception e) {
                    log.error("Failed to send notification for warning {}: {}", warning.getId(), e.getMessage());
                }
            }
        }

        return unacknowledged;
    }

    // --- Private helpers ---

    /**
     * Auto-escalation: náº¿u Ä‘Ã£ cÃ³ >= 3 cáº£nh cÃ¡o cÃ¹ng level, tá»± Ä‘á»™ng nÃ¢ng level.
     */
    private WarningLevel determineEffectiveLevel(Integer userId, WarningLevel requestedLevel) {
        long sameLevel = warningRepository
                .countActiveByUserAndLevel(userId, requestedLevel, LocalDate.now());

        if (sameLevel >= MAX_WARNINGS_BEFORE_ESCALATION) {
            WarningLevel escalated = requestedLevel.next();
            log.info("Auto-escalation for user {}: {} -> {} (had {} same-level warnings)",
                    userId, requestedLevel, escalated, sameLevel);
            return escalated;
        }
        return requestedLevel;
    }

    /**
     * Xá»­ lÃ½ háº­u cáº£nh cÃ¡o: náº¿u TERMINATION â†’ deactivate user.
     */
    private void handlePostWarningActions(EmployeeWarning warning) {
        if (warning.getWarningLevel() == WarningLevel.TERMINATION) {
            User employee = warning.getUser();
            employee.setStatus(UserStatus.INACTIVE);
            userRepository.save(employee);
            log.warn("Employee {} has been deactivated due to TERMINATION warning", employee.getUsername());
        }

        // Táº¡o thÃ´ng bÃ¡o cho nhÃ¢n viÃªn
        try {
            String message = String.format("Báº¡n nháº­n Ä‘Æ°á»£c cáº£nh cÃ¡o má»©c %s: %s",
                    warning.getWarningLevel().getDisplayName(), warning.getReason());
            notificationService.createNotification(warning.getUser(), message, NotificationType.WARNING, "/user1/warnings");
        } catch (Exception e) {
            log.error("Failed to notify user about warning: {}", e.getMessage());
        }
    }
}


