package com.example.hr.service;

import com.example.hr.models.HrAuditLog;
import com.example.hr.repository.HrAuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class HrAuditLogService {

    @Autowired
    private HrAuditLogRepository hrAuditLogRepository;

    @Transactional
    public void log(Authentication auth, String action, String entityType, String entityId, String detail, String ipAddress) {
        String actor = "system";
        if (auth != null && auth.getName() != null && !auth.getName().isBlank()) {
            actor = auth.getName();
        }
        log(actor, action, entityType, entityId, detail, ipAddress);
    }

    @Transactional
    public void log(String actorUsername, String action, String entityType, String entityId, String detail, String ipAddress) {
        HrAuditLog row = new HrAuditLog();
        row.setActorUsername(actorUsername != null && !actorUsername.isBlank() ? actorUsername : "system");
        row.setAction(action);
        row.setEntityType(entityType);
        row.setEntityId(entityId);
        row.setDetail(detail);
        row.setIpAddress(ipAddress);
        row.setCreatedAt(LocalDateTime.now());
        hrAuditLogRepository.save(row);
    }
}
