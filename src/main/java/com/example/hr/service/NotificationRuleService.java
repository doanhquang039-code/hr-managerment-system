package com.example.hr.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationRuleService {

    private static final List<NotificationRule> RULES = new ArrayList<>();

    static {
        // Leave request notification
        RULES.add(new NotificationRule(
                1,
                "LEAVE_REQUEST_SUBMITTED",
                "Leave Request Submitted",
                "Notify manager when employee submits leave request",
                Arrays.asList("EMAIL", "PUSH"),
                Arrays.asList("MANAGER"),
                true
        ));

        // Leave approval notification
        RULES.add(new NotificationRule(
                2,
                "LEAVE_APPROVED",
                "Leave Request Approved",
                "Notify employee when leave is approved",
                Arrays.asList("EMAIL", "PUSH", "SMS"),
                Arrays.asList("EMPLOYEE"),
                true
        ));

        // Leave rejection notification
        RULES.add(new NotificationRule(
                3,
                "LEAVE_REJECTED",
                "Leave Request Rejected",
                "Notify employee when leave is rejected",
                Arrays.asList("EMAIL", "PUSH"),
                Arrays.asList("EMPLOYEE"),
                true
        ));

        // Payslip notification
        RULES.add(new NotificationRule(
                4,
                "PAYSLIP_GENERATED",
                "Payslip Generated",
                "Notify employee when payslip is generated",
                Arrays.asList("EMAIL", "PUSH"),
                Arrays.asList("EMPLOYEE"),
                true
        ));

        // Performance review notification
        RULES.add(new NotificationRule(
                5,
                "PERFORMANCE_REVIEW_DUE",
                "Performance Review Due",
                "Notify employee and manager when review is due",
                Arrays.asList("EMAIL", "PUSH"),
                Arrays.asList("EMPLOYEE", "MANAGER"),
                true
        ));

        // Birthday notification
        RULES.add(new NotificationRule(
                6,
                "EMPLOYEE_BIRTHDAY",
                "Employee Birthday",
                "Notify team about employee birthday",
                Arrays.asList("EMAIL"),
                Arrays.asList("TEAM"),
                true
        ));

        // Work anniversary notification
        RULES.add(new NotificationRule(
                7,
                "WORK_ANNIVERSARY",
                "Work Anniversary",
                "Notify about employee work anniversary",
                Arrays.asList("EMAIL", "PUSH"),
                Arrays.asList("EMPLOYEE", "MANAGER"),
                true
        ));

        // Late check-in notification
        RULES.add(new NotificationRule(
                8,
                "LATE_CHECK_IN",
                "Late Check-in",
                "Notify manager when employee checks in late",
                Arrays.asList("EMAIL"),
                Arrays.asList("MANAGER"),
                false
        ));

        // Missing check-out notification
        RULES.add(new NotificationRule(
                9,
                "MISSING_CHECK_OUT",
                "Missing Check-out",
                "Notify employee about missing check-out",
                Arrays.asList("PUSH"),
                Arrays.asList("EMPLOYEE"),
                true
        ));

        // Document expiry notification
        RULES.add(new NotificationRule(
                10,
                "DOCUMENT_EXPIRING",
                "Document Expiring Soon",
                "Notify employee about expiring documents",
                Arrays.asList("EMAIL", "PUSH"),
                Arrays.asList("EMPLOYEE", "HR"),
                true
        ));

        // Training reminder notification
        RULES.add(new NotificationRule(
                11,
                "TRAINING_REMINDER",
                "Training Reminder",
                "Remind employee about upcoming training",
                Arrays.asList("EMAIL", "PUSH"),
                Arrays.asList("EMPLOYEE"),
                true
        ));

        // Interview scheduled notification
        RULES.add(new NotificationRule(
                12,
                "INTERVIEW_SCHEDULED",
                "Interview Scheduled",
                "Notify candidate and interviewer about interview",
                Arrays.asList("EMAIL", "SMS"),
                Arrays.asList("CANDIDATE", "INTERVIEWER"),
                true
        ));

        // System maintenance notification
        RULES.add(new NotificationRule(
                13,
                "SYSTEM_MAINTENANCE",
                "System Maintenance",
                "Notify all users about system maintenance",
                Arrays.asList("EMAIL", "PUSH"),
                Arrays.asList("ALL_USERS"),
                true
        ));
    }

    /**
     * Get all notification rules
     */
    public List<NotificationRule> getAllRules() {
        return new ArrayList<>(RULES);
    }

    /**
     * Get active notification rules
     */
    public List<NotificationRule> getActiveRules() {
        return RULES.stream()
                .filter(NotificationRule::isActive)
                .toList();
    }

    /**
     * Get rule by code
     */
    public NotificationRule getRuleByCode(String code) {
        return RULES.stream()
                .filter(rule -> rule.code().equals(code))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Notification rule not found: " + code));
    }

    /**
     * Get rules by recipient type
     */
    public List<NotificationRule> getRulesByRecipient(String recipientType) {
        return RULES.stream()
                .filter(rule -> rule.recipients().contains(recipientType))
                .toList();
    }

    /**
     * Get rules by channel
     */
    public List<NotificationRule> getRulesByChannel(String channel) {
        return RULES.stream()
                .filter(rule -> rule.channels().contains(channel))
                .toList();
    }

    /**
     * Toggle rule status
     */
    public void toggleRule(String code) {
        NotificationRule rule = getRuleByCode(code);
        int index = RULES.indexOf(rule);
        
        NotificationRule updatedRule = new NotificationRule(
                rule.id(),
                rule.code(),
                rule.name(),
                rule.description(),
                rule.channels(),
                rule.recipients(),
                !rule.isActive()
        );
        
        RULES.set(index, updatedRule);
        log.info("Toggled notification rule: {} to {}", code, updatedRule.isActive());
    }

    /**
     * Check if notification should be sent
     */
    public boolean shouldSendNotification(String code, String channel) {
        try {
            NotificationRule rule = getRuleByCode(code);
            return rule.isActive() && rule.channels().contains(channel);
        } catch (Exception e) {
            log.error("Error checking notification rule", e);
            return false;
        }
    }

    /**
     * Get notification channels for event
     */
    public List<String> getChannelsForEvent(String code) {
        try {
            NotificationRule rule = getRuleByCode(code);
            return rule.isActive() ? rule.channels() : Collections.emptyList();
        } catch (Exception e) {
            log.error("Error getting channels for event", e);
            return Collections.emptyList();
        }
    }

    /**
     * Get recipients for event
     */
    public List<String> getRecipientsForEvent(String code) {
        try {
            NotificationRule rule = getRuleByCode(code);
            return rule.isActive() ? rule.recipients() : Collections.emptyList();
        } catch (Exception e) {
            log.error("Error getting recipients for event", e);
            return Collections.emptyList();
        }
    }

    /**
     * Notification rule record
     */
    public record NotificationRule(
            Integer id,
            String code,
            String name,
            String description,
            List<String> channels,
            List<String> recipients,
            boolean isActive
    ) {}
}


