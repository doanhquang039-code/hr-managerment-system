// src/main/java/com/example/hr/service/EmailService.java
package com.example.hr.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // ✅ 1. Chào mừng nhân viên mới
    public void sendWelcomeEmail(String toEmail, String fullName, String username, String password) throws MessagingException {
        String subject = "🎉 Chào mừng bạn đến với công ty!";
        String content = """
            <h2>Xin chào %s!</h2>
            <p>Tài khoản của bạn đã được tạo thành công.</p>
            <p><b>Username:</b> %s</p>
            <p><b>Password:</b> %s</p>
            <p>Vui lòng đổi mật khẩu sau khi đăng nhập lần đầu.</p>
            <br/>
            <p>Trân trọng,<br/>HR Team</p>
        """.formatted(fullName, username, password);

        sendHtmlEmail(toEmail, subject, content);
    }

    // ✅ 2. Reset Password
    public void sendResetPasswordEmail(String toEmail, String fullName, String resetLink) throws MessagingException {
        String subject = "🔐 Yêu cầu đặt lại mật khẩu";
        String content = """
            <h2>Xin chào %s!</h2>
            <p>Chúng tôi nhận được yêu cầu đặt lại mật khẩu của bạn.</p>
            <a href="%s" style="padding:10px 20px;background:#4CAF50;color:white;text-decoration:none;border-radius:5px;">
                Đặt lại mật khẩu
            </a>
            <p>Link có hiệu lực trong 30 phút.</p>
            <p>Nếu bạn không yêu cầu, hãy bỏ qua email này.</p>
        """.formatted(fullName, resetLink);

        sendHtmlEmail(toEmail, subject, content);
    }

    // ✅ 3. Thông báo hợp đồng sắp hết hạn
    public void sendContractExpiryEmail(String toEmail, String fullName, String contractEndDate) throws MessagingException {
        String subject = "⚠️ Hợp đồng sắp hết hạn";
        String content = """
            <h2>Xin chào %s!</h2>
            <p>Hợp đồng của bạn sẽ hết hạn vào ngày <b>%s</b>.</p>
            <p>Vui lòng liên hệ phòng HR để gia hạn hợp đồng.</p>
            <br/>
            <p>Trân trọng,<br/>HR Team</p>
        """.formatted(fullName, contractEndDate);

        sendHtmlEmail(toEmail, subject, content);
    }

    // ✅ 4. Gửi Payslip
    public void sendPayslipEmail(String toEmail, String fullName, String month, double salary) throws MessagingException {
        String subject = "💰 Bảng lương tháng " + month;
        String content = """
            <h2>Xin chào %s!</h2>
            <p>Bảng lương tháng <b>%s</b> của bạn:</p>
            <table border="1" cellpadding="8" style="border-collapse:collapse;">
                <tr><td><b>Họ tên</b></td><td>%s</td></tr>
                <tr><td><b>Tháng</b></td><td>%s</td></tr>
                <tr><td><b>Lương thực nhận</b></td><td>%,.0f VNĐ</td></tr>
            </table>
            <br/>
            <p>Trân trọng,<br/>HR Team</p>
        """.formatted(fullName, month, fullName, month, salary);

        sendHtmlEmail(toEmail, subject, content);
    }

    // ✅ Helper gửi HTML email
    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true); 
        mailSender.send(message);
    }
}