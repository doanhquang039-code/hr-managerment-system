package com.example.hr.service;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Email service dÃ¹ng SendGrid â€” active khi sendgrid.enabled=true.
 * Fallback vá» JavaMailSender náº¿u khÃ´ng cÃ³ SendGrid.
 */
@ConditionalOnBean(SendGrid.class)
public class SendGridEmailService {

    private static final Logger log = LoggerFactory.getLogger(SendGridEmailService.class);

    private final SendGrid sendGrid;

    @Value("${sendgrid.from-email:noreply@hrms.com}")
    private String fromEmail;

    @Value("${sendgrid.from-name:HRMS System}")
    private String fromName;

    public SendGridEmailService(SendGrid sendGrid) {
        this.sendGrid = sendGrid;
    }

    // ==================== PUBLIC METHODS ====================

    /** Gá»­i email chÃ o má»«ng nhÃ¢n viÃªn má»›i */
    public void sendWelcomeEmail(String toEmail, String fullName,
                                  String username, String tempPassword) {
        String subject = "ðŸŽ‰ ChÃ o má»«ng báº¡n Ä‘áº¿n vá»›i HRMS!";
        String html = buildWelcomeHtml(fullName, username, tempPassword);
        send(toEmail, fullName, subject, html);
    }

    /** Gá»­i payslip thÃ¡ng */
    public void sendPayslipEmail(String toEmail, String fullName,
                                  int month, int year,
                                  BigDecimal baseSalary, BigDecimal netSalary,
                                  BigDecimal deductions, BigDecimal bonus) {
        String subject = String.format("ðŸ’° Phiáº¿u lÆ°Æ¡ng thÃ¡ng %d/%d", month, year);
        String html = buildPayslipHtml(fullName, month, year, baseSalary, netSalary, deductions, bonus);
        send(toEmail, fullName, subject, html);
    }

    /** ThÃ´ng bÃ¡o Ä‘Æ¡n nghá»‰ phÃ©p Ä‘Æ°á»£c duyá»‡t/tá»« chá»‘i */
    public void sendLeaveStatusEmail(String toEmail, String fullName,
                                      String leaveType, String startDate, String endDate,
                                      boolean approved, String reason) {
        String status = approved ? "âœ… ÄÃ£ duyá»‡t" : "âŒ Tá»« chá»‘i";
        String subject = status + " â€” ÄÆ¡n nghá»‰ phÃ©p cá»§a báº¡n";
        String html = buildLeaveStatusHtml(fullName, leaveType, startDate, endDate, approved, reason);
        send(toEmail, fullName, subject, html);
    }

    /** ThÃ´ng bÃ¡o há»£p Ä‘á»“ng sáº¯p háº¿t háº¡n */
    public void sendContractExpiryEmail(String toEmail, String fullName,
                                         String expiryDate, int daysLeft) {
        String subject = "âš ï¸ Há»£p Ä‘á»“ng sáº¯p háº¿t háº¡n â€” cÃ²n " + daysLeft + " ngÃ y";
        String html = buildContractExpiryHtml(fullName, expiryDate, daysLeft);
        send(toEmail, fullName, subject, html);
    }

    /** ThÃ´ng bÃ¡o yÃªu cáº§u chi phÃ­ Ä‘Æ°á»£c duyá»‡t/tá»« chá»‘i */
    public void sendExpenseStatusEmail(String toEmail, String fullName,
                                        String claimTitle, BigDecimal amount,
                                        boolean approved, String reason) {
        String subject = (approved ? "âœ… Duyá»‡t" : "âŒ Tá»« chá»‘i") + " â€” YÃªu cáº§u chi phÃ­: " + claimTitle;
        String html = buildExpenseStatusHtml(fullName, claimTitle, amount, approved, reason);
        send(toEmail, fullName, subject, html);
    }

    /** ThÃ´ng bÃ¡o KPI Goal má»›i Ä‘Æ°á»£c giao */
    public void sendKpiAssignedEmail(String toEmail, String fullName,
                                      String goalTitle, String deadline) {
        String subject = "ðŸŽ¯ KPI Goal má»›i Ä‘Æ°á»£c giao cho báº¡n";
        String html = buildKpiAssignedHtml(fullName, goalTitle, deadline);
        send(toEmail, fullName, subject, html);
    }

    /** ThÃ´ng bÃ¡o chung (announcement) */
    public void sendAnnouncementEmail(String toEmail, String fullName,
                                       String title, String content) {
        String subject = "ðŸ“¢ ThÃ´ng bÃ¡o: " + title;
        String html = buildAnnouncementHtml(fullName, title, content);
        send(toEmail, fullName, subject, html);
    }

    /** Bulk gá»­i email cho nhiá»u ngÆ°á»i */
    public void sendPasswordResetEmail(String toEmail, String fullName, String resetLink) {
        String subject = "HRMS password reset";
        String html = baseTemplate("Password reset", """
            <h2 style="color:#6366f1;">Password reset request</h2>
            <p>Hello <b>%s</b>,</p>
            <p>We received a request to reset your HRMS password.</p>
            <p>
              <a href="%s" style="display:inline-block;background:#4f46e5;color:white;padding:12px 24px;border-radius:10px;text-decoration:none;font-weight:700;">
                Reset password
              </a>
            </p>
            <p>This link expires in 15 minutes. If you did not request it, ignore this email.</p>
            """.formatted(fullName, resetLink));
        send(toEmail, fullName, subject, html);
    }

    public void sendBulkEmail(java.util.List<String[]> recipients,
                               String subject, String htmlTemplate) {
        // recipients: List of [email, name]
        Mail mail = new Mail();
        mail.setFrom(new Email(fromEmail, fromName));
        mail.setSubject(subject);

        for (String[] r : recipients) {
            Personalization p = new Personalization();
            p.addTo(new Email(r[0], r[1]));
            mail.addPersonalization(p);
        }
        mail.addContent(new Content("text/html", htmlTemplate));

        try {
            Request req = new Request();
            req.setMethod(Method.POST);
            req.setEndpoint("mail/send");
            req.setBody(mail.build());
            Response resp = sendGrid.api(req);
            log.info("Bulk email sent to {} recipients, status={}", recipients.size(), resp.getStatusCode());
        } catch (IOException e) {
            log.error("Failed to send bulk email: {}", e.getMessage());
        }
    }

    // ==================== PRIVATE HELPERS ====================

    private void send(String toEmail, String toName, String subject, String html) {
        try {
            Email from = new Email(fromEmail, fromName);
            Email to   = new Email(toEmail, toName);
            Content content = new Content("text/html", html);
            Mail mail = new Mail(from, subject, to, content);

            Request req = new Request();
            req.setMethod(Method.POST);
            req.setEndpoint("mail/send");
            req.setBody(mail.build());

            Response resp = sendGrid.api(req);
            if (resp.getStatusCode() >= 200 && resp.getStatusCode() < 300) {
                log.info("Email sent to {} â€” subject: {}", toEmail, subject);
            } else {
                log.warn("SendGrid returned status {} for {}", resp.getStatusCode(), toEmail);
            }
        } catch (IOException e) {
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage());
        }
    }

    private String formatVnd(BigDecimal amount) {
        if (amount == null) return "0 â‚«";
        return NumberFormat.getNumberInstance(new Locale("vi", "VN"))
                .format(amount) + " â‚«";
    }

    // ==================== HTML TEMPLATES ====================

    private String buildWelcomeHtml(String name, String username, String password) {
        return baseTemplate("ChÃ o má»«ng Ä‘áº¿n vá»›i HRMS!", """
            <h2 style="color:#6366f1;">ðŸŽ‰ Xin chÃ o %s!</h2>
            <p>TÃ i khoáº£n cá»§a báº¡n Ä‘Ã£ Ä‘Æ°á»£c táº¡o thÃ nh cÃ´ng trÃªn há»‡ thá»‘ng HRMS.</p>
            <div style="background:#f8fafc;border-radius:12px;padding:20px;margin:20px 0;border-left:4px solid #6366f1;">
                <p style="margin:6px 0;"><b>ðŸ‘¤ TÃªn Ä‘Äƒng nháº­p:</b> <code style="background:#ede9fe;padding:2px 8px;border-radius:4px;">%s</code></p>
                <p style="margin:6px 0;"><b>ðŸ”‘ Máº­t kháº©u táº¡m:</b> <code style="background:#ede9fe;padding:2px 8px;border-radius:4px;">%s</code></p>
            </div>
            <p style="color:#ef4444;font-weight:600;">âš ï¸ Vui lÃ²ng Ä‘á»•i máº­t kháº©u ngay sau khi Ä‘Äƒng nháº­p láº§n Ä‘áº§u!</p>
            <a href="#" style="display:inline-block;background:linear-gradient(135deg,#6366f1,#8b5cf6);color:white;padding:12px 28px;border-radius:10px;text-decoration:none;font-weight:600;margin-top:12px;">
                ÄÄƒng nháº­p ngay â†’
            </a>
            """.formatted(name, username, password));
    }

    private String buildPayslipHtml(String name, int month, int year,
                                     BigDecimal base, BigDecimal net,
                                     BigDecimal deductions, BigDecimal bonus) {
        return baseTemplate("Phiáº¿u lÆ°Æ¡ng thÃ¡ng " + month + "/" + year, """
            <h2 style="color:#10b981;">ðŸ’° Phiáº¿u LÆ°Æ¡ng ThÃ¡ng %d/%d</h2>
            <p>Xin chÃ o <b>%s</b>, Ä‘Ã¢y lÃ  chi tiáº¿t lÆ°Æ¡ng cá»§a báº¡n:</p>
            <table style="width:100%%;border-collapse:collapse;margin:20px 0;">
                <tr style="background:#f0fdf4;">
                    <td style="padding:12px 16px;border:1px solid #d1fae5;font-weight:600;">LÆ°Æ¡ng cÆ¡ báº£n</td>
                    <td style="padding:12px 16px;border:1px solid #d1fae5;text-align:right;color:#059669;font-weight:700;">%s</td>
                </tr>
                <tr>
                    <td style="padding:12px 16px;border:1px solid #e5e7eb;">ThÆ°á»Ÿng & phá»¥ cáº¥p</td>
                    <td style="padding:12px 16px;border:1px solid #e5e7eb;text-align:right;color:#059669;">+%s</td>
                </tr>
                <tr>
                    <td style="padding:12px 16px;border:1px solid #e5e7eb;">Kháº¥u trá»« (BHXH, thuáº¿...)</td>
                    <td style="padding:12px 16px;border:1px solid #e5e7eb;text-align:right;color:#ef4444;">-%s</td>
                </tr>
                <tr style="background:#f0fdf4;">
                    <td style="padding:14px 16px;border:2px solid #10b981;font-weight:800;font-size:1.05em;">ðŸ’µ Thá»±c nháº­n</td>
                    <td style="padding:14px 16px;border:2px solid #10b981;text-align:right;color:#059669;font-weight:800;font-size:1.1em;">%s</td>
                </tr>
            </table>
            <p style="color:#64748b;font-size:0.9em;">Náº¿u cÃ³ tháº¯c máº¯c, vui lÃ²ng liÃªn há»‡ phÃ²ng HR.</p>
            """.formatted(month, year, name,
                formatVnd(base), formatVnd(bonus), formatVnd(deductions), formatVnd(net)));
    }

    private String buildLeaveStatusHtml(String name, String type, String start,
                                         String end, boolean approved, String reason) {
        String color = approved ? "#10b981" : "#ef4444";
        String icon  = approved ? "âœ…" : "âŒ";
        String statusText = approved ? "ÄÃƒ ÄÆ¯á»¢C DUYá»†T" : "Bá»Š Tá»ª CHá»I";
        return baseTemplate("Káº¿t quáº£ Ä‘Æ¡n nghá»‰ phÃ©p", """
            <h2 style="color:%s;">%s ÄÆ¡n nghá»‰ phÃ©p cá»§a báº¡n %s</h2>
            <p>Xin chÃ o <b>%s</b>,</p>
            <div style="background:#f8fafc;border-radius:12px;padding:20px;margin:16px 0;border-left:4px solid %s;">
                <p style="margin:6px 0;"><b>Loáº¡i nghá»‰:</b> %s</p>
                <p style="margin:6px 0;"><b>Tá»« ngÃ y:</b> %s</p>
                <p style="margin:6px 0;"><b>Äáº¿n ngÃ y:</b> %s</p>
                %s
            </div>
            """.formatted(color, icon, statusText, name, color, type, start, end,
                reason != null && !reason.isBlank()
                    ? "<p style='margin:6px 0;color:#ef4444;'><b>LÃ½ do:</b> " + reason + "</p>"
                    : ""));
    }

    private String buildContractExpiryHtml(String name, String expiryDate, int daysLeft) {
        String urgency = daysLeft <= 7 ? "#ef4444" : daysLeft <= 30 ? "#f59e0b" : "#6366f1";
        return baseTemplate("Há»£p Ä‘á»“ng sáº¯p háº¿t háº¡n", """
            <h2 style="color:%s;">âš ï¸ Há»£p Ä‘á»“ng sáº¯p háº¿t háº¡n</h2>
            <p>Xin chÃ o <b>%s</b>,</p>
            <p>Há»£p Ä‘á»“ng lao Ä‘á»™ng cá»§a báº¡n sáº½ háº¿t háº¡n vÃ o ngÃ y <b style="color:%s;">%s</b>
               (cÃ²n <b style="color:%s;">%d ngÃ y</b>).</p>
            <p>Vui lÃ²ng liÃªn há»‡ phÃ²ng HR Ä‘á»ƒ Ä‘Æ°á»£c há»— trá»£ gia háº¡n há»£p Ä‘á»“ng.</p>
            """.formatted(urgency, name, urgency, expiryDate, urgency, daysLeft));
    }

    private String buildExpenseStatusHtml(String name, String title,
                                           BigDecimal amount, boolean approved, String reason) {
        String color = approved ? "#10b981" : "#ef4444";
        String icon  = approved ? "âœ…" : "âŒ";
        return baseTemplate("Káº¿t quáº£ yÃªu cáº§u chi phÃ­", """
            <h2 style="color:%s;">%s YÃªu cáº§u chi phÃ­ %s</h2>
            <p>Xin chÃ o <b>%s</b>,</p>
            <div style="background:#f8fafc;border-radius:12px;padding:20px;margin:16px 0;border-left:4px solid %s;">
                <p style="margin:6px 0;"><b>TiÃªu Ä‘á»:</b> %s</p>
                <p style="margin:6px 0;"><b>Sá»‘ tiá»n:</b> <span style="color:%s;font-weight:700;">%s</span></p>
                %s
            </div>
            """.formatted(color, icon, approved ? "Ä‘Ã£ Ä‘Æ°á»£c duyá»‡t" : "bá»‹ tá»« chá»‘i",
                name, color, title, color, formatVnd(amount),
                reason != null && !reason.isBlank()
                    ? "<p style='margin:6px 0;color:#ef4444;'><b>LÃ½ do:</b> " + reason + "</p>"
                    : ""));
    }

    private String buildKpiAssignedHtml(String name, String goalTitle, String deadline) {
        return baseTemplate("KPI Goal má»›i", """
            <h2 style="color:#6366f1;">ðŸŽ¯ KPI Goal má»›i Ä‘Æ°á»£c giao</h2>
            <p>Xin chÃ o <b>%s</b>,</p>
            <p>Báº¡n vá»«a Ä‘Æ°á»£c giao má»™t má»¥c tiÃªu KPI má»›i:</p>
            <div style="background:#ede9fe;border-radius:12px;padding:20px;margin:16px 0;border-left:4px solid #6366f1;">
                <p style="margin:6px 0;font-size:1.05em;font-weight:700;color:#4338ca;">%s</p>
                <p style="margin:6px 0;color:#64748b;"><b>Thá»i háº¡n:</b> %s</p>
            </div>
            <p>ÄÄƒng nháº­p vÃ o há»‡ thá»‘ng Ä‘á»ƒ xem chi tiáº¿t vÃ  cáº­p nháº­t tiáº¿n Ä‘á»™.</p>
            """.formatted(name, goalTitle, deadline));
    }

    private String buildAnnouncementHtml(String name, String title, String content) {
        return baseTemplate("ThÃ´ng bÃ¡o: " + title, """
            <h2 style="color:#0f172a;">ðŸ“¢ %s</h2>
            <p>Xin chÃ o <b>%s</b>,</p>
            <div style="background:#f8fafc;border-radius:12px;padding:20px;margin:16px 0;line-height:1.7;">
                %s
            </div>
            """.formatted(title, name, content));
    }

    /** Base HTML template vá»›i header/footer Ä‘áº¹p */
    private String baseTemplate(String preheader, String body) {
        return """
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"></head>
            <body style="margin:0;padding:0;background:#f0f4f8;font-family:'Segoe UI',Arial,sans-serif;">
              <div style="max-width:600px;margin:32px auto;background:white;border-radius:16px;overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,0.08);">
                <!-- Header -->
                <div style="background:linear-gradient(135deg,#6366f1,#8b5cf6);padding:28px 32px;text-align:center;">
                  <div style="font-size:1.6rem;font-weight:800;color:white;letter-spacing:1px;">âš¡ HRMS</div>
                  <div style="color:rgba(255,255,255,0.8);font-size:0.85rem;margin-top:4px;">Human Resource Management System</div>
                </div>
                <!-- Body -->
                <div style="padding:32px;color:#1e293b;line-height:1.6;">
                  %s
                </div>
                <!-- Footer -->
                <div style="background:#f8fafc;padding:20px 32px;text-align:center;border-top:1px solid #e2e8f0;">
                  <p style="margin:0;color:#94a3b8;font-size:0.8rem;">
                    Email nÃ y Ä‘Æ°á»£c gá»­i tá»± Ä‘á»™ng tá»« há»‡ thá»‘ng HRMS. Vui lÃ²ng khÃ´ng reply.<br/>
                    Â© 2026 HRMS â€” All rights reserved.
                  </p>
                </div>
              </div>
            </body>
            </html>
            """.formatted(body);
    }
}


