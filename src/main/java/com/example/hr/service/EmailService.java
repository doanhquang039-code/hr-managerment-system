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

    // âœ… 1. ChÃ o má»«ng nhÃ¢n viÃªn má»›i
    public void sendWelcomeEmail(String toEmail, String fullName, String username, String password) throws MessagingException {
        String subject = "ðŸŽ‰ ChÃ o má»«ng báº¡n Ä‘áº¿n vá»›i cÃ´ng ty!";
        String content = """
            <h2>Xin chÃ o %s!</h2>
            <p>TÃ i khoáº£n cá»§a báº¡n Ä‘Ã£ Ä‘Æ°á»£c táº¡o thÃ nh cÃ´ng.</p>
            <p><b>Username:</b> %s</p>
            <p><b>Password:</b> %s</p>
            <p>Vui lÃ²ng Ä‘á»•i máº­t kháº©u sau khi Ä‘Äƒng nháº­p láº§n Ä‘áº§u.</p>
            <br/>
            <p>TrÃ¢n trá»ng,<br/>HR Team</p>
        """.formatted(fullName, username, password);

        sendHtmlEmail(toEmail, subject, content);
    }

    // âœ… 2. Reset Password
    public void sendResetPasswordEmail(String toEmail, String fullName, String resetLink) throws MessagingException {
        String subject = "ðŸ” YÃªu cáº§u Ä‘áº·t láº¡i máº­t kháº©u";
        String content = """
            <h2>Xin chÃ o %s!</h2>
            <p>ChÃºng tÃ´i nháº­n Ä‘Æ°á»£c yÃªu cáº§u Ä‘áº·t láº¡i máº­t kháº©u cá»§a báº¡n.</p>
            <a href="%s" style="padding:10px 20px;background:#4CAF50;color:white;text-decoration:none;border-radius:5px;">
                Äáº·t láº¡i máº­t kháº©u
            </a>
            <p>Link cÃ³ hiá»‡u lá»±c trong 30 phÃºt.</p>
            <p>Náº¿u báº¡n khÃ´ng yÃªu cáº§u, hÃ£y bá» qua email nÃ y.</p>
        """.formatted(fullName, resetLink);

        sendHtmlEmail(toEmail, subject, content);
    }

    // âœ… 3. ThÃ´ng bÃ¡o há»£p Ä‘á»“ng sáº¯p háº¿t háº¡n
    public void sendContractExpiryEmail(String toEmail, String fullName, String contractEndDate) throws MessagingException {
        String subject = "âš ï¸ Há»£p Ä‘á»“ng sáº¯p háº¿t háº¡n";
        String content = """
            <h2>Xin chÃ o %s!</h2>
            <p>Há»£p Ä‘á»“ng cá»§a báº¡n sáº½ háº¿t háº¡n vÃ o ngÃ y <b>%s</b>.</p>
            <p>Vui lÃ²ng liÃªn há»‡ phÃ²ng HR Ä‘á»ƒ gia háº¡n há»£p Ä‘á»“ng.</p>
            <br/>
            <p>TrÃ¢n trá»ng,<br/>HR Team</p>
        """.formatted(fullName, contractEndDate);

        sendHtmlEmail(toEmail, subject, content);
    }

    // âœ… 4. Gá»­i Payslip
    public void sendPayslipEmail(String toEmail, String fullName, String month, double salary) throws MessagingException {
        String subject = "ðŸ’° Báº£ng lÆ°Æ¡ng thÃ¡ng " + month;
        String content = """
            <h2>Xin chÃ o %s!</h2>
            <p>Báº£ng lÆ°Æ¡ng thÃ¡ng <b>%s</b> cá»§a báº¡n:</p>
            <table border="1" cellpadding="8" style="border-collapse:collapse;">
                <tr><td><b>Há» tÃªn</b></td><td>%s</td></tr>
                <tr><td><b>ThÃ¡ng</b></td><td>%s</td></tr>
                <tr><td><b>LÆ°Æ¡ng thá»±c nháº­n</b></td><td>%,.0f VNÄ</td></tr>
            </table>
            <br/>
            <p>TrÃ¢n trá»ng,<br/>HR Team</p>
        """.formatted(fullName, month, fullName, month, salary);

        sendHtmlEmail(toEmail, subject, content);
    }

    // âœ… Helper gá»­i HTML email
    public void sendNotificationEmail(String toEmail, String fullName, String subject, String message) throws MessagingException {
        String safeSubject = subject == null || subject.isBlank() ? "HRMS Notification" : subject;
        String safeMessage = message == null ? "" : message;
        String content = """
            <h2>Xin chao %s!</h2>
            <p>%s</p>
            <br/>
            <p>Tran trong,<br/>HR Team</p>
        """.formatted(fullName, safeMessage.replace("\n", "<br/>"));

        sendHtmlEmail(toEmail, safeSubject, content);
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true); 
        mailSender.send(message);
    }
}


