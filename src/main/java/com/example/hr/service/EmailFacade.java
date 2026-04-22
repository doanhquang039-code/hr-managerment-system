package com.example.hr.service;

import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Facade thống nhất email — tự động dùng SendGrid nếu có,
 * fallback về JavaMailSender (Gmail SMTP) nếu không.
 */
@Service
public class EmailFacade {

    private static final Logger log = LoggerFactory.getLogger(EmailFacade.class);

    @Autowired(required = false)
    private SendGridEmailService sendGridService;

    @Autowired
    private EmailService gmailService;

    private boolean hasSendGrid() {
        return sendGridService != null;
    }

    // ---- Welcome ----
    public void sendWelcome(String email, String name, String username, String password) {
        if (hasSendGrid()) {
            sendGridService.sendWelcomeEmail(email, name, username, password);
        } else {
            try { gmailService.sendWelcomeEmail(email, name, username, password); }
            catch (MessagingException e) { log.error("Gmail welcome failed: {}", e.getMessage()); }
        }
    }

    // ---- Payslip ----
    public void sendPayslip(String email, String name, int month, int year,
                             BigDecimal base, BigDecimal net,
                             BigDecimal deductions, BigDecimal bonus) {
        if (hasSendGrid()) {
            sendGridService.sendPayslipEmail(email, name, month, year, base, net, deductions, bonus);
        } else {
            try { gmailService.sendPayslipEmail(email, name, month + "/" + year, net.doubleValue()); }
            catch (MessagingException e) { log.error("Gmail payslip failed: {}", e.getMessage()); }
        }
    }

    // ---- Leave status ----
    public void sendLeaveStatus(String email, String name, String type,
                                 String start, String end, boolean approved, String reason) {
        if (hasSendGrid()) {
            sendGridService.sendLeaveStatusEmail(email, name, type, start, end, approved, reason);
        } else {
            log.info("Leave status email (Gmail fallback) to {}", email);
        }
    }

    // ---- Contract expiry ----
    public void sendContractExpiry(String email, String name, String expiryDate, int daysLeft) {
        if (hasSendGrid()) {
            sendGridService.sendContractExpiryEmail(email, name, expiryDate, daysLeft);
        } else {
            try { gmailService.sendContractExpiryEmail(email, name, expiryDate); }
            catch (MessagingException e) { log.error("Gmail contract expiry failed: {}", e.getMessage()); }
        }
    }

    // ---- Expense status ----
    public void sendExpenseStatus(String email, String name, String title,
                                   BigDecimal amount, boolean approved, String reason) {
        if (hasSendGrid()) {
            sendGridService.sendExpenseStatusEmail(email, name, title, amount, approved, reason);
        } else {
            log.info("Expense status email (Gmail fallback) to {}", email);
        }
    }

    // ---- KPI assigned ----
    public void sendKpiAssigned(String email, String name, String goalTitle, String deadline) {
        if (hasSendGrid()) {
            sendGridService.sendKpiAssignedEmail(email, name, goalTitle, deadline);
        } else {
            log.info("KPI assigned email (Gmail fallback) to {}", email);
        }
    }

    // ---- Announcement ----
    public void sendAnnouncement(String email, String name, String title, String content) {
        if (hasSendGrid()) {
            sendGridService.sendAnnouncementEmail(email, name, title, content);
        } else {
            log.info("Announcement email (Gmail fallback) to {}", email);
        }
    }

    public String getProvider() {
        return hasSendGrid() ? "SendGrid" : "Gmail SMTP";
    }
}
