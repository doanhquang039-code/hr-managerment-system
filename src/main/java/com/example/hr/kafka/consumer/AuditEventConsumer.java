package com.example.hr.kafka.consumer;

import com.example.hr.kafka.events.AuditEvent;
import com.example.hr.service.HrAuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditEventConsumer {

    private final HrAuditLogService auditLogService;

    @KafkaListener(topics = "${kafka.topics.audit-events}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeAuditEvent(AuditEvent event) {
        try {
            auditLogService.log(
                    event.getActorUsername(),
                    event.getAction(),
                    event.getEntityType(),
                    event.getEntityId(),
                    event.getDetail(),
                    event.getIpAddress()
            );
            log.debug("Audit event persisted: actor={}, action={}, entity={}",
                    event.getActorUsername(), event.getAction(), event.getEntityType());
        } catch (Exception e) {
            log.error("Failed to persist audit event: actor={}, action={}",
                    event != null ? event.getActorUsername() : null,
                    event != null ? event.getAction() : null,
                    e);
            throw new IllegalStateException("Failed to persist audit event", e);
        }
    }
}
