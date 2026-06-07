package com.example.hr.service;

import com.example.hr.models.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class InternalMessageDeliveryService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${hr.notifications.slack.webhook-url:${SLACK_WEBHOOK_URL:}}")
    private String slackWebhookUrl;

    @Value("${hr.notifications.zalo.webhook-url:${ZALO_WEBHOOK_URL:}}")
    private String zaloWebhookUrl;

    public boolean sendSlack(String subject, String message) {
        if (isBlank(slackWebhookUrl)) {
            log.info("Slack webhook not configured. subject={}", subject);
            return false;
        }
        Map<String, Object> body = Map.of("text", "*" + subject + "*\n" + message);
        return postWebhook(slackWebhookUrl, body, "Slack");
    }

    public boolean sendZalo(User user, String subject, String message) {
        if (isBlank(zaloWebhookUrl)) {
            log.info("Zalo webhook not configured. userId={}, subject={}", user != null ? user.getId() : null, subject);
            return false;
        }
        Map<String, Object> body = new HashMap<>();
        body.put("recipient", user != null ? user.getPhoneNumber() : null);
        body.put("subject", subject);
        body.put("message", message);
        return postWebhook(zaloWebhookUrl, body, "Zalo");
    }

    private boolean postWebhook(String url, Map<String, Object> body, String channel) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            restTemplate.postForEntity(url, new HttpEntity<>(body, headers), String.class);
            return true;
        } catch (Exception e) {
            log.error("{} webhook delivery failed", channel, e);
            return false;
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}


