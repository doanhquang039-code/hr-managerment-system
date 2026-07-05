package com.example.hr.rabbitmq.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Email Queue Producer
 * Gửi email tasks vào RabbitMQ queue
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailQueueProducer {

    private final ObjectProvider<RabbitTemplate> rabbitTemplateProvider;

    @Value("${rabbitmq.exchange.hr}")
    private String hrExchange;

    /**
     * Send email task to queue
     */
    public void sendEmailTask(Map<String, Object> emailData) {
        RabbitTemplate rabbitTemplate = rabbitTemplateProvider.getIfAvailable();
        if (rabbitTemplate != null) {
            try {
                rabbitTemplate.convertAndSend(hrExchange, "hr.email.send", emailData);
                log.info("Email task sent to queue: {}", emailData.get("to"));
            } catch (Exception e) {
                log.error("Error sending email task to queue", e);
            }
        } else {
            log.info("RabbitMQ is disabled, skipping email task: {}", emailData.get("to"));
        }
    }

    /**
     * Send bulk email task
     */
    public void sendBulkEmailTask(Map<String, Object> bulkEmailData) {
        RabbitTemplate rabbitTemplate = rabbitTemplateProvider.getIfAvailable();
        if (rabbitTemplate != null) {
            try {
                rabbitTemplate.convertAndSend(hrExchange, "hr.email.bulk", bulkEmailData);
                log.info("Bulk email task sent to queue");
            } catch (Exception e) {
                log.error("Error sending bulk email task to queue", e);
            }
        } else {
            log.info("RabbitMQ is disabled, skipping bulk email task");
        }
    }
}
