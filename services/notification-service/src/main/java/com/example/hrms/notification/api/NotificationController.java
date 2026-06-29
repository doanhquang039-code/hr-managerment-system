package com.example.hrms.notification.api;

import com.example.hrms.notification.domain.NotificationRecord;
import com.example.hrms.notification.repository.NotificationRecordRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationRecordRepository repository;

    public NotificationController(NotificationRecordRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/users/{userId}")
    public List<NotificationRecord> byUser(@PathVariable Integer userId) {
        return repository.findByRecipientUserIdOrderByCreatedAtDesc(userId);
    }

    @GetMapping("/users/{userId}/unread-count")
    public Map<String, Long> unreadCount(@PathVariable Integer userId) {
        return Map.of("unreadCount", repository.countByRecipientUserIdAndReadFlagFalse(userId));
    }

    @PostMapping
    public ResponseEntity<NotificationRecord> create(@Valid @RequestBody CreateNotificationRequest request) {
        NotificationRecord record = new NotificationRecord();
        record.setRecipientUserId(request.recipientUserId());
        record.setTitle(request.title());
        record.setMessage(request.message());
        record.setSeverity(request.severity() == null ? "INFO" : request.severity());
        record.setSource(request.source() == null ? "HRMS" : request.source());
        record.setEventType(request.eventType());
        record.setLink(request.link());
        return ResponseEntity.ok(repository.save(record));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markRead(@PathVariable Long id) {
        return repository.findById(id)
                .map(record -> {
                    record.setReadFlag(true);
                    repository.save(record);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    public record CreateNotificationRequest(
            @NotNull Integer recipientUserId,
            @NotBlank String title,
            @NotBlank String message,
            String severity,
            String source,
            String eventType,
            String link
    ) {}
}
