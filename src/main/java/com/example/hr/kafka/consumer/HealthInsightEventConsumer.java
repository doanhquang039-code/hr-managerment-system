package com.example.hr.kafka.consumer;

import com.example.hr.enums.NotificationType;
import com.example.hr.kafka.events.HealthInsightEvent;
import com.example.hr.models.User;
import com.example.hr.repository.UserRepository;
import com.example.hr.service.HrAuditLogService;
import com.example.hr.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class HealthInsightEventConsumer {

    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final HrAuditLogService auditLogService;

    @KafkaListener(topics = "${kafka.topics.health-insights}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeHealthInsightEvent(HealthInsightEvent event) {
        try {
            if (event == null || event.getUserId() == null) {
                throw new IllegalArgumentException("Health insight event has no userId");
            }

            User user = userRepository.findById(event.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + event.getUserId()));

            if (shouldNotify(event)) {
                notificationService.createNotification(
                        user,
                        buildMessage(event),
                        "HIGH".equalsIgnoreCase(event.getRiskLevel()) ? NotificationType.WARNING : NotificationType.INFO,
                        "/user1/dashboard"
                );
            }

            auditLogService.log(
                    "system",
                    "HEALTH_INSIGHT_" + safe(event.getRiskLevel()),
                    "HEALTH_INSIGHT",
                    event.getUserId().toString(),
                    buildAuditDetail(event),
                    null
            );

            log.info("Health insight processed: userId={}, risk={}, score={}",
                    event.getUserId(), event.getRiskLevel(), event.getWellnessScore());
        } catch (Exception e) {
            log.error("Failed to process health insight event: userId={}",
                    event != null ? event.getUserId() : null, e);
            throw new IllegalStateException("Failed to process health insight event", e);
        }
    }

    private boolean shouldNotify(HealthInsightEvent event) {
        return "HIGH".equalsIgnoreCase(event.getRiskLevel())
                || "MEDIUM".equalsIgnoreCase(event.getRiskLevel());
    }

    private String buildMessage(HealthInsightEvent event) {
        String headline = "HIGH".equalsIgnoreCase(event.getRiskLevel())
                ? "Health Insight: chỉ số tải việc đang cao"
                : "Health Insight: có vài chỉ số cần chú ý";
        String flags = event.getFlags() == null || event.getFlags().isEmpty()
                ? "chưa có cờ rủi ro cụ thể"
                : String.join(", ", event.getFlags());
        String firstRecommendation = first(event.getRecommendations());
        return headline + ". Điểm hiện tại: " + event.getWellnessScore()
                + "/100. Dấu hiệu: " + flags
                + (firstRecommendation.isBlank() ? "." : ". Gợi ý: " + firstRecommendation);
    }

    private String buildAuditDetail(HealthInsightEvent event) {
        return "{"
                + "\"userId\":" + event.getUserId() + ","
                + "\"role\":\"" + escape(safe(event.getRole())) + "\","
                + "\"riskLevel\":\"" + escape(safe(event.getRiskLevel())) + "\","
                + "\"wellnessScore\":" + event.getWellnessScore() + ","
                + "\"flags\":\"" + escape(event.getFlags() == null ? "" : String.join(",", event.getFlags())) + "\""
                + "}";
    }

    private String first(List<String> values) {
        return values == null || values.isEmpty() || values.get(0) == null ? "" : values.get(0);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
