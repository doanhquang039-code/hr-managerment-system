package com.example.hr.config;

import com.sendgrid.SendGrid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SendGridConfig {

    @Value("${sendgrid.api-key:}")
    private String apiKey;

    /**
     * Chỉ tạo bean khi sendgrid.enabled=true và có API key.
     * Nếu không có SendGrid, hệ thống fallback về JavaMailSender (Gmail SMTP).
     */
    @Bean
    @ConditionalOnProperty(name = "sendgrid.enabled", havingValue = "true")
    public SendGrid sendGrid() {
        return new SendGrid(apiKey);
    }
}
