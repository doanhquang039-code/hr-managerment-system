package com.example.hr.interceptor;

import com.example.hr.kafka.events.AuditEvent;
import com.example.hr.kafka.producer.HREventProducer;
import com.example.hr.service.HrAuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class AdminMutationAuditInterceptor implements HandlerInterceptor {

    private static final Set<String> MUTATION_METHODS = Set.of("POST", "PUT", "PATCH", "DELETE");
    private static final Set<String> AUDITED_ROLES = Set.of("ROLE_ADMIN", "ROLE_MANAGER", "ROLE_HIRING");
    private static final Set<String> SENSITIVE_PARAMS = Set.of(
            "password", "confirmPassword", "_csrf", "csrf", "token", "accessToken", "refreshToken",
            "apiKey", "secret", "clientSecret"
    ).stream().map(value -> value.toLowerCase(Locale.ROOT)).collect(Collectors.toUnmodifiableSet());

    private final HrAuditLogService auditLogService;
    private final ObjectProvider<HREventProducer> eventProducerProvider;

    public AdminMutationAuditInterceptor(HrAuditLogService auditLogService,
                                         ObjectProvider<HREventProducer> eventProducerProvider) {
        this.auditLogService = auditLogService;
        this.eventProducerProvider = eventProducerProvider;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        String method = request.getMethod();
        if (!MUTATION_METHODS.contains(method)) {
            return;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !hasAuditedRole(auth)) {
            return;
        }

        String uri = request.getRequestURI();
        if (uri.startsWith("/css/") || uri.startsWith("/js/") || uri.startsWith("/images/")
                || uri.startsWith("/static/") || uri.startsWith("/admin/audit-log")) {
            return;
        }

        String action = method + "_" + response.getStatus();
        String entityType = resolveEntityType(uri);
        String entityId = resolveEntityId(uri);
        String detail = buildDetail(request, response, ex);

        String actorUsername = auth.getName();
        String ipAddress = getClientIp(request);
        AuditEvent event = new AuditEvent(
                actorUsername,
                action,
                entityType,
                entityId,
                detail,
                ipAddress,
                LocalDateTime.now()
        );

        HREventProducer eventProducer = eventProducerProvider.getIfAvailable();
        if (eventProducer != null) {
            try {
                eventProducer.publishAuditEvent(event);
                return;
            } catch (Exception ignored) {
                // Fall back to direct DB logging when Kafka is unavailable.
            }
        }

        auditLogService.log(actorUsername, action, entityType, entityId, detail, ipAddress);
    }

    private boolean hasAuditedRole(Authentication auth) {
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(AUDITED_ROLES::contains);
    }

    private String resolveEntityType(String uri) {
        String[] segments = Arrays.stream(uri.split("/"))
                .filter(segment -> !segment.isBlank())
                .toArray(String[]::new);
        if (segments.length == 0) {
            return "Request";
        }
        if (segments.length > 1 && ("admin".equals(segments[0]) || "manager".equals(segments[0]) || "hiring".equals(segments[0]))) {
            return normalizeSegment(segments[1]);
        }
        return normalizeSegment(segments[0]);
    }

    private String resolveEntityId(String uri) {
        String[] segments = uri.split("/");
        for (int i = segments.length - 1; i >= 0; i--) {
            String segment = segments[i];
            if (segment.matches("\\d+")) {
                return segment;
            }
        }
        return null;
    }

    private String normalizeSegment(String segment) {
        if (segment == null || segment.isBlank()) {
            return "Request";
        }
        return segment.replace('-', '_').toUpperCase(Locale.ROOT);
    }

    private String buildDetail(HttpServletRequest request, HttpServletResponse response, Exception ex) {
        String params = request.getParameterMap().entrySet().stream()
                .filter(entry -> !isSensitive(entry.getKey()))
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> quote(entry.getKey()) + ":" + quote(formatValues(entry.getValue())))
                .collect(Collectors.joining(","));

        String roles = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.joining(","));

        return "{"
                + "\"method\":" + quote(request.getMethod()) + ","
                + "\"uri\":" + quote(request.getRequestURI()) + ","
                + "\"query\":" + quote(request.getQueryString()) + ","
                + "\"status\":" + response.getStatus() + ","
                + "\"roles\":" + quote(roles) + ","
                + "\"params\":{" + params + "},"
                + "\"exception\":" + quote(ex == null ? null : ex.getClass().getSimpleName())
                + "}";
    }

    private boolean isSensitive(String key) {
        return key != null && SENSITIVE_PARAMS.contains(key.toLowerCase(Locale.ROOT));
    }

    private String formatValues(String[] values) {
        if (values == null || values.length == 0) {
            return "";
        }
        return Arrays.stream(values)
                .limit(5)
                .collect(Collectors.joining(","));
    }

    private String quote(String value) {
        if (value == null) {
            return "null";
        }
        return "\"" + value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                + "\"";
    }

    private String getClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
