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
 * Email service dùng SendGrid — active khi sendgrid.enabled=true.
 * Fallback về JavaMailSender nếu không có SendGrid.
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

    /** Gửi email chào mừng nhân viên mới */
    public void sendWelcomeEmail(String toEmail, String fullName,
                                  String username, String tempPassword) {
        String subject = "🎉 Chào mừng bạn đến với HRMS!";
        String html = buildWelcomeHtml(fullName, username, tempPassword);
        send(toEmail, fullName, subject, html);
    }

    /** Gửi payslip tháng */
    public void sendPayslipEmail(String toEmail, String fullName,
                                  int month, int year,
                                  BigDecimal baseSalary, BigDecimal netSalary,
                                  BigDecimal deductions, BigDecimal bonus) {
        String subject = String.format("💰 Phiếu lương tháng %d/%d", month, year);
        String html = buildPayslipHtml(fullName, month, year, baseSalary, netSalary, deductions, bonus);
        send(toEmail, fullName, subject, html);
    }

    /** Thông báo đơn nghỉ phép được duyệt/từ chối */
    public void sendLeaveStatusEmail(String toEmail, String fullName,
                                      String leaveType, String startDate, String endDate,
                                      boolean approved, String reason) {
        String status = approved ? "✅ Đã duyệt" : "❌ Từ chối";
        String subject = status + " — Đơn nghỉ phép của bạn";
        String html = buildLeaveStatusHtml(fullName, leaveType, startDate, endDate, approved, reason);
        send(toEmail, fullName, subject, html);
    }

    /** Thông báo hợp đồng sắp hết hạn */
    public void sendContractExpiryEmail(String toEmail, String fullName,
                                         String expiryDate, int daysLeft) {
        String subject = "⚠️ Hợp đồng sắp hết hạn — còn " + daysLeft + " ngày";
        String html = buildContractExpiryHtml(fullName, expiryDate, daysLeft);
        send(toEmail, fullName, subject, html);
    }

    /** Thông báo yêu cầu chi phí được duyệt/từ chối */
    public void sendExpenseStatusEmail(String toEmail, String fullName,
                                        String claimTitle, BigDecimal amount,
                                        boolean approved, String reason) {
        String subject = (approved ? "✅ Duyệt" : "❌ Từ chối") + " — Yêu cầu chi phí: " + claimTitle;
        String html = buildExpenseStatusHtml(fullName, claimTitle, amount, approved, reason);
        send(toEmail, fullName, subject, html);
    }

    /** Thông báo KPI Goal mới được giao */
    public void sendKpiAssignedEmail(String toEmail, String fullName,
                                      String goalTitle, String deadline) {
        String subject = "🎯 KPI Goal mới được giao cho bạn";
        String html = buildKpiAssignedHtml(fullName, goalTitle, deadline);
        send(toEmail, fullName, subject, html);
    }

    /** Thông báo chung (announcement) */
    public void sendAnnouncementEmail(String toEmail, String fullName,
                                       String title, String content) {
        String subject = "📢 Thông báo: " + title;
        String html = buildAnnouncementHtml(fullName, title, content);
        send(toEmail, fullName, subject, html);
    }

    /** Bulk gửi email cho nhiều người */
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
                log.info("Email sent to {} — subject: {}", toEmail, subject);
            } else {
                log.warn("SendGrid returned status {} for {}", resp.getStatusCode(), toEmail);
            }
        } catch (IOException e) {
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage());
        }
    }

    private String formatVnd(BigDecimal amount) {
        if (amount == null) return "0 ₫";
        return NumberFormat.getNumberInstance(new Locale("vi", "VN"))
                .format(amount) + " ₫";
    }

    // ==================== HTML TEMPLATES ====================

    private String buildWelcomeHtml(String name, String username, String password) {
        return baseTemplate("Chào mừng đến với HRMS!", """
            <h2 style="color:#6366f1;">🎉 Xin chào %s!</h2>
            <p>Tài khoản của bạn đã được tạo thành công trên hệ thống HRMS.</p>
            <div style="background:#f8fafc;border-radius:12px;padding:20px;margin:20px 0;border-left:4px solid #6366f1;">
                <p style="margin:6px 0;"><b>👤 Tên đăng nhập:</b> <code style="background:#ede9fe;padding:2px 8px;border-radius:4px;">%s</code></p>
                <p style="margin:6px 0;"><b>🔑 Mật khẩu tạm:</b> <code style="background:#ede9fe;padding:2px 8px;border-radius:4px;">%s</code></p>
            </div>
            <p style="color:#ef4444;font-weight:600;">⚠️ Vui lòng đổi mật khẩu ngay sau khi đăng nhập lần đầu!</p>
            <a href="#" style="display:inline-block;background:linear-gradient(135deg,#6366f1,#8b5cf6);color:white;padding:12px 28px;border-radius:10px;text-decoration:none;font-weight:600;margin-top:12px;">
                Đăng nhập ngay →
            </a>
            """.formatted(name, username, password));
    }

    private String buildPayslipHtml(String name, int month, int year,
                                     BigDecimal base, BigDecimal net,
                                     BigDecimal deductions, BigDecimal bonus) {
        return baseTemplate("Phiếu lương tháng " + month + "/" + year, """
            <h2 style="color:#10b981;">💰 Phiếu Lương Tháng %d/%d</h2>
            <p>Xin chào <b>%s</b>, đây là chi tiết lương của bạn:</p>
            <table style="width:100%%;border-collapse:collapse;margin:20px 0;">
                <tr style="background:#f0fdf4;">
                    <td style="padding:12px 16px;border:1px solid #d1fae5;font-weight:600;">Lương cơ bản</td>
                    <td style="padding:12px 16px;border:1px solid #d1fae5;text-align:right;color:#059669;font-weight:700;">%s</td>
                </tr>
                <tr>
                    <td style="padding:12px 16px;border:1px solid #e5e7eb;">Thưởng & phụ cấp</td>
                    <td style="padding:12px 16px;border:1px solid #e5e7eb;text-align:right;color:#059669;">+%s</td>
                </tr>
                <tr>
                    <td style="padding:12px 16px;border:1px solid #e5e7eb;">Khấu trừ (BHXH, thuế...)</td>
                    <td style="padding:12px 16px;border:1px solid #e5e7eb;text-align:right;color:#ef4444;">-%s</td>
                </tr>
                <tr style="background:#f0fdf4;">
                    <td style="padding:14px 16px;border:2px solid #10b981;font-weight:800;font-size:1.05em;">💵 Thực nhận</td>
                    <td style="padding:14px 16px;border:2px solid #10b981;text-align:right;color:#059669;font-weight:800;font-size:1.1em;">%s</td>
                </tr>
            </table>
            <p style="color:#64748b;font-size:0.9em;">Nếu có thắc mắc, vui lòng liên hệ phòng HR.</p>
            """.formatted(month, year, name,
                formatVnd(base), formatVnd(bonus), formatVnd(deductions), formatVnd(net)));
    }

    private String buildLeaveStatusHtml(String name, String type, String start,
                                         String end, boolean approved, String reason) {
        String color = approved ? "#10b981" : "#ef4444";
        String icon  = approved ? "✅" : "❌";
        String statusText = approved ? "ĐÃ ĐƯỢC DUYỆT" : "BỊ TỪ CHỐI";
        return baseTemplate("Kết quả đơn nghỉ phép", """
            <h2 style="color:%s;">%s Đơn nghỉ phép của bạn %s</h2>
            <p>Xin chào <b>%s</b>,</p>
            <div style="background:#f8fafc;border-radius:12px;padding:20px;margin:16px 0;border-left:4px solid %s;">
                <p style="margin:6px 0;"><b>Loại nghỉ:</b> %s</p>
                <p style="margin:6px 0;"><b>Từ ngày:</b> %s</p>
                <p style="margin:6px 0;"><b>Đến ngày:</b> %s</p>
                %s
            </div>
            """.formatted(color, icon, statusText, name, color, type, start, end,
                reason != null && !reason.isBlank()
                    ? "<p style='margin:6px 0;color:#ef4444;'><b>Lý do:</b> " + reason + "</p>"
                    : ""));
    }

    private String buildContractExpiryHtml(String name, String expiryDate, int daysLeft) {
        String urgency = daysLeft <= 7 ? "#ef4444" : daysLeft <= 30 ? "#f59e0b" : "#6366f1";
        return baseTemplate("Hợp đồng sắp hết hạn", """
            <h2 style="color:%s;">⚠️ Hợp đồng sắp hết hạn</h2>
            <p>Xin chào <b>%s</b>,</p>
            <p>Hợp đồng lao động của bạn sẽ hết hạn vào ngày <b style="color:%s;">%s</b>
               (còn <b style="color:%s;">%d ngày</b>).</p>
            <p>Vui lòng liên hệ phòng HR để được hỗ trợ gia hạn hợp đồng.</p>
            """.formatted(urgency, name, urgency, expiryDate, urgency, daysLeft));
    }

    private String buildExpenseStatusHtml(String name, String title,
                                           BigDecimal amount, boolean approved, String reason) {
        String color = approved ? "#10b981" : "#ef4444";
        String icon  = approved ? "✅" : "❌";
        return baseTemplate("Kết quả yêu cầu chi phí", """
            <h2 style="color:%s;">%s Yêu cầu chi phí %s</h2>
            <p>Xin chào <b>%s</b>,</p>
            <div style="background:#f8fafc;border-radius:12px;padding:20px;margin:16px 0;border-left:4px solid %s;">
                <p style="margin:6px 0;"><b>Tiêu đề:</b> %s</p>
                <p style="margin:6px 0;"><b>Số tiền:</b> <span style="color:%s;font-weight:700;">%s</span></p>
                %s
            </div>
            """.formatted(color, icon, approved ? "đã được duyệt" : "bị từ chối",
                name, color, title, color, formatVnd(amount),
                reason != null && !reason.isBlank()
                    ? "<p style='margin:6px 0;color:#ef4444;'><b>Lý do:</b> " + reason + "</p>"
                    : ""));
    }

    private String buildKpiAssignedHtml(String name, String goalTitle, String deadline) {
        return baseTemplate("KPI Goal mới", """
            <h2 style="color:#6366f1;">🎯 KPI Goal mới được giao</h2>
            <p>Xin chào <b>%s</b>,</p>
            <p>Bạn vừa được giao một mục tiêu KPI mới:</p>
            <div style="background:#ede9fe;border-radius:12px;padding:20px;margin:16px 0;border-left:4px solid #6366f1;">
                <p style="margin:6px 0;font-size:1.05em;font-weight:700;color:#4338ca;">%s</p>
                <p style="margin:6px 0;color:#64748b;"><b>Thời hạn:</b> %s</p>
            </div>
            <p>Đăng nhập vào hệ thống để xem chi tiết và cập nhật tiến độ.</p>
            """.formatted(name, goalTitle, deadline));
    }

    private String buildAnnouncementHtml(String name, String title, String content) {
        return baseTemplate("Thông báo: " + title, """
            <h2 style="color:#0f172a;">📢 %s</h2>
            <p>Xin chào <b>%s</b>,</p>
            <div style="background:#f8fafc;border-radius:12px;padding:20px;margin:16px 0;line-height:1.7;">
                %s
            </div>
            """.formatted(title, name, content));
    }

    /** Base HTML template với header/footer đẹp */
    private String baseTemplate(String preheader, String body) {
        return """
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"></head>
            <body style="margin:0;padding:0;background:#f0f4f8;font-family:'Segoe UI',Arial,sans-serif;">
              <div style="max-width:600px;margin:32px auto;background:white;border-radius:16px;overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,0.08);">
                <!-- Header -->
                <div style="background:linear-gradient(135deg,#6366f1,#8b5cf6);padding:28px 32px;text-align:center;">
                  <div style="font-size:1.6rem;font-weight:800;color:white;letter-spacing:1px;">⚡ HRMS</div>
                  <div style="color:rgba(255,255,255,0.8);font-size:0.85rem;margin-top:4px;">Human Resource Management System</div>
                </div>
                <!-- Body -->
                <div style="padding:32px;color:#1e293b;line-height:1.6;">
                  %s
                </div>
                <!-- Footer -->
                <div style="background:#f8fafc;padding:20px 32px;text-align:center;border-top:1px solid #e2e8f0;">
                  <p style="margin:0;color:#94a3b8;font-size:0.8rem;">
                    Email này được gửi tự động từ hệ thống HRMS. Vui lòng không reply.<br/>
                    © 2026 HRMS — All rights reserved.
                  </p>
                </div>
              </div>
            </body>
            </html>
            """.formatted(body);
    }
}
