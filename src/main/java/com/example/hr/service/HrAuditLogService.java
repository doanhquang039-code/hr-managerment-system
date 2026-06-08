package com.example.hr.service;

import com.example.hr.models.HrAuditLog;
import com.example.hr.repository.HrAuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class HrAuditLogService {

    private static final DateTimeFormatter CSV_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private HrAuditLogRepository hrAuditLogRepository;

    @Autowired
    private AuditEncryptionService auditEncryptionService;

    @Transactional
    public void log(String actorUsername, String action, String entityType, String entityId, String detail) {
        log(actorUsername, action, entityType, entityId, detail, null);
    }

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
        row.setDetail(auditEncryptionService.encrypt(detail));
        row.setDetailEncrypted(detail != null && !detail.isBlank());
        row.setIpAddress(ipAddress);
        row.setCreatedAt(LocalDateTime.now());
        hrAuditLogRepository.save(row);
    }

    public String decryptDetail(HrAuditLog auditLog) {
        return auditLog == null ? null : auditEncryptionService.decrypt(auditLog.getDetail());
    }

    @Transactional(readOnly = true)
    public Page<HrAuditLog> findLogs(String query, Pageable pageable) {
        Page<HrAuditLog> logs = query == null || query.isBlank()
                ? hrAuditLogRepository.findAllByOrderByCreatedAtDesc(pageable)
                : hrAuditLogRepository.search(query.trim(), pageable);
        return logs.map(this::copyForDisplay);
    }

    @Transactional(readOnly = true)
    public String exportCsv(String query, int maxRows) {
        int safeMaxRows = Math.max(1, Math.min(maxRows, 5000));
        List<HrAuditLog> rows = findLogs(query, PageRequest.of(0, safeMaxRows)).getContent();

        StringBuilder csv = new StringBuilder();
        csv.append("id,created_at,actor_username,action,entity_type,entity_id,ip_address,detail\n");
        for (HrAuditLog row : rows) {
            csv.append(csvValue(row.getId()))
                    .append(',')
                    .append(csvValue(formatDate(row.getCreatedAt())))
                    .append(',')
                    .append(csvValue(row.getActorUsername()))
                    .append(',')
                    .append(csvValue(row.getAction()))
                    .append(',')
                    .append(csvValue(row.getEntityType()))
                    .append(',')
                    .append(csvValue(row.getEntityId()))
                    .append(',')
                    .append(csvValue(row.getIpAddress()))
                    .append(',')
                    .append(csvValue(row.getDetail()))
                    .append('\n');
        }
        return csv.toString();
    }

    private HrAuditLog copyForDisplay(HrAuditLog source) {
        HrAuditLog copy = new HrAuditLog();
        copy.setId(source.getId());
        copy.setActorUsername(source.getActorUsername());
        copy.setAction(source.getAction());
        copy.setEntityType(source.getEntityType());
        copy.setEntityId(source.getEntityId());
        copy.setDetail(auditEncryptionService.decrypt(source.getDetail()));
        copy.setDetailEncrypted(source.isDetailEncrypted());
        copy.setIpAddress(source.getIpAddress());
        copy.setCreatedAt(source.getCreatedAt());
        return copy;
    }

    private String formatDate(LocalDateTime value) {
        return value == null ? "" : value.format(CSV_DATE_FORMAT);
    }

    private String csvValue(Object value) {
        if (value == null) {
            return "\"\"";
        }
        String text = String.valueOf(value);
        return "\"" + text.replace("\"", "\"\"") + "\"";
    }
}


