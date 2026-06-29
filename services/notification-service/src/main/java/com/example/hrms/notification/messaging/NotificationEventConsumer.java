package com.example.hrms.notification.messaging;

import com.example.hrms.notification.domain.NotificationRecord;
import com.example.hrms.notification.repository.NotificationRecordRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventConsumer {
    private final NotificationRecordRepository repository;
    private final ObjectMapper objectMapper;

    public NotificationEventConsumer(NotificationRecordRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${hrms.kafka.topics.notifications:hr-notifications}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(String payload) throws Exception {
        JsonNode root = objectMapper.readTree(payload);
        JsonNode recipients = root.path("recipientUserIds");
        if (!recipients.isArray()) {
            recipients = root.path("recipients");
        }
        if (!recipients.isArray()) {
            Integer singleRecipient = readInt(root, "recipientUserId");
            if (singleRecipient != null) {
                saveRecord(root, singleRecipient);
            }
            return;
        }
        for (JsonNode recipient : recipients) {
            if (recipient.canConvertToInt()) {
                saveRecord(root, recipient.asInt());
            }
        }
    }

    private void saveRecord(JsonNode root, Integer recipientUserId) {
        NotificationRecord record = new NotificationRecord();
        record.setRecipientUserId(recipientUserId);
        record.setTitle(readText(root, "title", "HRMS notification"));
        record.setMessage(readText(root, "message", readText(root, "body", "You have a new notification.")));
        record.setSeverity(readText(root, "severity", "INFO"));
        record.setSource(readText(root, "source", "HRMS"));
        record.setEventType(readText(root, "eventType", readText(root, "type", "GENERAL")));
        record.setLink(readText(root, "link", null));
        repository.save(record);
    }

    private String readText(JsonNode root, String field, String defaultValue) {
        JsonNode node = root.path(field);
        return node.isMissingNode() || node.isNull() || node.asText().isBlank() ? defaultValue : node.asText();
    }

    private Integer readInt(JsonNode root, String field) {
        JsonNode node = root.path(field);
        return node.canConvertToInt() ? node.asInt() : null;
    }
}
