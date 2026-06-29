package com.example.hr.rabbitmq.consumer;

import com.example.hr.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Email Queue Consumer
 * Xử lý email tasks từ RabbitMQ queue
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailQueueConsumer {

    private final EmailService emailService;

    @RabbitListener(queues = "${rabbitmq.queue.email}")
    public void consumeEmailTask(Map<String, Object> emailData) {
        try {
            log.info("Processing email task: {}", emailData.get("to"));
            
            String to = (String) emailData.get("to");
            String subject = (String) emailData.get("subject");
            String body = (String) emailData.get("body");
            
            emailService.sendNotificationEmail(to, to, subject, body);
            
            log.info("Email sent successfully to: {}", to);
            
        } catch (Exception e) {
            log.error("Error processing email task", e);
        }
    }
}

