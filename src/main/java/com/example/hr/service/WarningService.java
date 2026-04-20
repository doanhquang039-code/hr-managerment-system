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
import com.example.hr.repository.UserRepository;
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
 * Service quản lý cảnh cáo / kỷ luật nhân viên.
 * Bao gồm: issue warning, escalation logic, auto-terminate.
 */
@Service
@Transactional
public class WarningService {

    private static final Logger log = LoggerFactory.getLogger(WarningService.class);
    private static final int DEFAULT_EXPIRY_DAYS = 180; // Cảnh cáo hết hiệu lực sau 6 tháng
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
     * Lấy tất cả cảnh cáo.
     */
    @Transactional(readOnly = true)
    public List<EmployeeWarning> getAllWarnings() {
        return warningRepository.findAll();
    }

    /**
     * Lấy cảnh cáo theo ID.
     */
    @Transactional(readOnly = true)
    public EmployeeWarning getWarningById(Integer id) {
        return warningRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cảnh cáo", id));
    }

    /**
     * Lấy cảnh cáo theo nhân viên.
     */
    @Transactional(readOnly = true)
    public List<EmployeeWarning> getWarningsByUser(Integer userId) {
        return warningRepository.findByUserIdOrderByIssuedDateDesc(userId);
    }

    /**
     * Lấy cảnh cáo đang hiệu lực của nhân viên.
     */
    @Transactional(readOnly = true)
    public List<EmployeeWarning> getActiveWarnings(Integer userId) {
        return warningRepository.findActiveWarnings(userId, LocalDate.now());
    }

    /**
     * Ban hành cảnh cáo mới.
     * Tự động escalation nếu đã có nhiều cảnh cáo cùng level.
     */
    public EmployeeWarning issueWarning(EmployeeWarningDTO dto) {
        User employee = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Nhân viên", dto.getUserId()));
        User issuer = userRepository.findById(dto.getIssuedById())
                .orElseThrow(() -> new ResourceNotFoundException("Người ban hành", dto.getIssuedById()));

        // Xác định warning level (auto-escalation nếu cần)
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
     * Nhân viên xác nhận đã đọc cảnh cáo.
     */
    public EmployeeWarning acknowledgeWarning(Integer warningId) {
        EmployeeWarning warning = getWarningById(warningId);
        warning.acknowledge();
        return warningRepository.save(warning);
    }

    /**
     * Xóa cảnh cáo (chỉ admin).
     */
    public void deleteWarning(Integer id) {
        if (!warningRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cảnh cáo", id);
        }
        warningRepository.deleteById(id);
    }

    /**
     * Thống kê cảnh cáo theo mức độ.
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
     * Thống kê cảnh cáo theo phòng ban.
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
     * Đếm nhân viên có cảnh cáo nghiêm trọng.
     */
    @Transactional(readOnly = true)
    public long countEmployeesWithSevereWarnings() {
        return warningRepository.countEmployeesWithSevereWarnings(LocalDate.now());
    }

    /**
     * Kiểm tra và xử lý cảnh cáo chưa acknowledge sau 30 ngày.
     */
    public List<EmployeeWarning> processUnacknowledgedWarnings() {
        LocalDate cutoff = LocalDate.now().minusDays(30);
        List<EmployeeWarning> unacknowledged = warningRepository.findUnacknowledgedBefore(cutoff);

        for (EmployeeWarning warning : unacknowledged) {
            if (warning.needsEscalation()) {
                log.warn("Warning {} needs escalation: user={}, level={}",
                        warning.getId(), warning.getUser().getUsername(), warning.getWarningLevel());
                // Tạo thông báo nhắc nhở
                try {
                    notificationService.createNotification(
                            warning.getUser(),
                            "Bạn có cảnh cáo chưa xác nhận từ " + warning.getIssuedDate(),
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
     * Auto-escalation: nếu đã có >= 3 cảnh cáo cùng level, tự động nâng level.
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
     * Xử lý hậu cảnh cáo: nếu TERMINATION → deactivate user.
     */
    private void handlePostWarningActions(EmployeeWarning warning) {
        if (warning.getWarningLevel() == WarningLevel.TERMINATION) {
            User employee = warning.getUser();
            employee.setStatus(UserStatus.INACTIVE);
            userRepository.save(employee);
            log.warn("Employee {} has been deactivated due to TERMINATION warning", employee.getUsername());
        }

        // Tạo thông báo cho nhân viên
        try {
            String message = String.format("Bạn nhận được cảnh cáo mức %s: %s",
                    warning.getWarningLevel().getDisplayName(), warning.getReason());
            notificationService.createNotification(warning.getUser(), message, NotificationType.WARNING, "/user1/warnings");
        } catch (Exception e) {
            log.error("Failed to notify user about warning: {}", e.getMessage());
        }
    }
}
