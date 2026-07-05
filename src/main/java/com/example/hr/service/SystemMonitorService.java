package com.example.hr.service;


import com.example.hr.payroll.entity.Payroll;
import com.example.hr.models.HrAuditLog;
import com.example.hr.repository.HrAuditLogRepository;
import com.example.hr.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.cache.CacheManager;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.sql.Connection;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class SystemMonitorService {

    private final DataSource dataSource;
    private final RedisTemplate<String, Object> redisTemplate;
    private final CacheManager cacheManager;
    private final ObjectProvider<KafkaProperties> kafkaPropertiesProvider;
    private final Environment environment;
    private final HrAuditLogRepository hrAuditLogRepository;
    private final HrAuditLogService hrAuditLogService;
    private final NotificationRepository notificationRepository;
    private final ObjectProvider<CloudStorageFacade> cloudStorageFacadeProvider;
    private final ObjectProvider<EmailFacade> emailFacadeProvider;

    /**
     * Láº¥y system health metrics
     */
    public Map<String, Object> getSystemHealth() {
        Map<String, Object> health = new HashMap<>();
        
        health.put("server", getServerMetrics());
        health.put("database", getDatabaseMetrics());
        health.put("cache", getCacheMetrics());
        health.put("memory", getMemoryMetrics());
        health.put("timestamp", System.currentTimeMillis());
        
        return health;
    }

    /**
     * Server metrics
     */
    private Map<String, Object> getServerMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        try {
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            
            metrics.put("cpuLoad", osBean.getSystemLoadAverage());
            metrics.put("availableProcessors", osBean.getAvailableProcessors());
            metrics.put("osName", osBean.getName());
            metrics.put("osVersion", osBean.getVersion());
            metrics.put("osArch", osBean.getArch());
            metrics.put("status", "UP");
        } catch (Exception e) {
            log.error("Error getting server metrics", e);
            metrics.put("status", "ERROR");
            metrics.put("error", e.getMessage());
        }
        
        return metrics;
    }

    /**
     * Database metrics
     */
    private Map<String, Object> getDatabaseMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        try (Connection conn = dataSource.getConnection()) {
            metrics.put("status", "UP");
            metrics.put("database", conn.getMetaData().getDatabaseProductName());
            metrics.put("version", conn.getMetaData().getDatabaseProductVersion());
            metrics.put("url", conn.getMetaData().getURL());
            metrics.put("connectionValid", conn.isValid(5));
        } catch (Exception e) {
            log.error("Error getting database metrics", e);
            metrics.put("status", "DOWN");
            metrics.put("error", e.getMessage());
        }
        
        return metrics;
    }

    /**
     * Cache metrics
     */
    private Map<String, Object> getCacheMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        try {
            // Redis metrics
            RedisConnection connection = Objects.requireNonNull(redisTemplate.getConnectionFactory()).getConnection();
            metrics.put("redis", Map.of(
                "status", connection.ping() != null ? "UP" : "DOWN",
                "dbSize", connection.dbSize()
            ));
            connection.close();
            
            // Cache names
            metrics.put("cacheNames", cacheManager.getCacheNames());
            metrics.put("status", "UP");
        } catch (Exception e) {
            log.error("Error getting cache metrics", e);
            metrics.put("status", "DOWN");
            metrics.put("error", e.getMessage());
        }
        
        return metrics;
    }

    /**
     * Memory metrics
     */
    private Map<String, Object> getMemoryMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        try {
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            Runtime runtime = Runtime.getRuntime();
            
            long maxMemory = runtime.maxMemory();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            
            metrics.put("maxMemory", formatBytes(maxMemory));
            metrics.put("totalMemory", formatBytes(totalMemory));
            metrics.put("usedMemory", formatBytes(usedMemory));
            metrics.put("freeMemory", formatBytes(freeMemory));
            metrics.put("usagePercent", (usedMemory * 100.0 / maxMemory));
            
            metrics.put("heap", Map.of(
                "used", formatBytes(memoryBean.getHeapMemoryUsage().getUsed()),
                "max", formatBytes(memoryBean.getHeapMemoryUsage().getMax()),
                "committed", formatBytes(memoryBean.getHeapMemoryUsage().getCommitted())
            ));
            
            metrics.put("status", "UP");
        } catch (Exception e) {
            log.error("Error getting memory metrics", e);
            metrics.put("status", "ERROR");
            metrics.put("error", e.getMessage());
        }
        
        return metrics;
    }

    /**
     * Format bytes to human readable
     */
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp-1) + "";
        return String.format("%.2f %sB", bytes / Math.pow(1024, exp), pre);
    }

    /**
     * Get quick stats for dashboard
     */
    public Map<String, Object> getQuickStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            Runtime runtime = Runtime.getRuntime();
            long usedMemory = runtime.totalMemory() - runtime.freeMemory();
            long maxMemory = runtime.maxMemory();
            
            stats.put("memoryUsagePercent", (usedMemory * 100.0 / maxMemory));
            stats.put("databaseStatus", isDatabaseHealthy() ? "UP" : "DOWN");
            stats.put("cacheStatus", isCacheHealthy() ? "UP" : "DOWN");
            stats.put("overallStatus", "UP");
        } catch (Exception e) {
            log.error("Error getting quick stats", e);
            stats.put("overallStatus", "ERROR");
        }
        
        return stats;
    }

    private boolean isDatabaseHealthy() {
        try (Connection conn = dataSource.getConnection()) {
            return conn.isValid(5);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isCacheHealthy() {
        try {
            RedisConnection connection = Objects.requireNonNull(redisTemplate.getConnectionFactory()).getConnection();
            boolean healthy = connection.ping() != null;
            connection.close();
            return healthy;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get performance metrics for admin dashboard
     */
    public Map<String, Object> getPerformanceMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        try {
            Runtime runtime = Runtime.getRuntime();
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            
            // CPU metrics
            metrics.put("cpuLoad", osBean.getSystemLoadAverage());
            metrics.put("availableProcessors", osBean.getAvailableProcessors());
            
            // Memory metrics
            long maxMemory = runtime.maxMemory();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            
            metrics.put("memoryUsagePercent", (usedMemory * 100.0 / maxMemory));
            metrics.put("heapUsed", memoryBean.getHeapMemoryUsage().getUsed());
            metrics.put("heapMax", memoryBean.getHeapMemoryUsage().getMax());
            
            // System status
            metrics.put("databaseHealthy", isDatabaseHealthy());
            metrics.put("cacheHealthy", isCacheHealthy());
            
            // Uptime
            metrics.put("uptime", ManagementFactory.getRuntimeMXBean().getUptime());
            
        } catch (Exception e) {
            log.error("Error getting performance metrics", e);
            metrics.put("error", e.getMessage());
        }
        
        return metrics;
    }

    public Map<String, Object> getOperationsMonitor() {
        Map<String, Object> monitor = new LinkedHashMap<>();
        monitor.put("kafka", getKafkaMetrics());
        monitor.put("email", getEmailMetrics());
        monitor.put("cloud", getCloudMetrics());
        monitor.put("audit", getAuditMetrics());
        monitor.put("notifications", getNotificationMetrics());
        monitor.put("readiness", getReadinessChecklist());
        return monitor;
    }

    private Map<String, Object> getKafkaMetrics() {
        Map<String, Object> metrics = new LinkedHashMap<>();
        List<String> configuredTopics = configuredKafkaTopics();
        List<String> dltTopics = configuredTopics.stream().map(topic -> topic + ".DLT").toList();

        KafkaProperties kafkaProperties = kafkaPropertiesProvider.getIfAvailable();
        if (kafkaProperties == null || kafkaProperties.getBootstrapServers() == null || kafkaProperties.getBootstrapServers().isEmpty()) {
            metrics.put("status", "DISABLED");
            metrics.put("message", "Chưa cấu hình Kafka hoặc bootstrap server.");
            return metrics;
        }

        metrics.put("bootstrapServers", kafkaProperties.getBootstrapServers());
        metrics.put("configuredTopics", configuredTopics);
        metrics.put("dltTopics", dltTopics);
        metrics.put("configuredTopicCount", configuredTopics.size());
        metrics.put("dltTopicCount", dltTopics.size());

        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, String.join(",", kafkaProperties.getBootstrapServers()));
        props.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, "1500");
        props.put(AdminClientConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, "1500");

        try (AdminClient adminClient = AdminClient.create(props)) {
            Set<String> existingTopics = adminClient.listTopics().names().get(2, TimeUnit.SECONDS);
            int present = 0;
            List<String> missingTopics = new ArrayList<>();
            for (String topic : configuredTopics) {
                if (existingTopics.contains(topic)) {
                    present++;
                } else {
                    missingTopics.add(topic);
                }
            }

            int dltPresent = 0;
            for (String topic : dltTopics) {
                if (existingTopics.contains(topic)) {
                    dltPresent++;
                }
            }

            metrics.put("status", "UP");
            metrics.put("clusterId", adminClient.describeCluster().clusterId().get(2, TimeUnit.SECONDS));
            metrics.put("nodeCount", adminClient.describeCluster().nodes().get(2, TimeUnit.SECONDS).size());
            metrics.put("brokerTopicCount", existingTopics.size());
            metrics.put("presentConfiguredTopics", present);
            metrics.put("presentDltTopics", dltPresent);
            metrics.put("missingTopics", missingTopics);
        } catch (Exception e) {
            metrics.put("status", "DOWN");
            metrics.put("message", "KhÃ´ng káº¿t ná»‘i Ä‘Æ°á»£c Kafka broker: " + e.getMessage());
            metrics.put("presentConfiguredTopics", 0);
            metrics.put("presentDltTopics", 0);
            metrics.put("missingTopics", configuredTopics);
        }
        return metrics;
    }

    private List<String> configuredKafkaTopics() {
        List<String> topics = new ArrayList<>();
        String[] keys = {
                "attendance", "leave", "payroll", "notifications", "performance-reviews",
                "recruitment", "training", "employee-lifecycle", "audit-events", "health-insights"
        };
        for (String key : keys) {
            String topic = environment.getProperty("kafka.topics." + key);
            if (topic != null && !topic.isBlank()) {
                topics.add(topic);
            }
        }
        return topics;
    }

    private Map<String, Object> getEmailMetrics() {
        Map<String, Object> metrics = new LinkedHashMap<>();
        try {
            EmailFacade emailFacade = emailFacadeProvider.getIfAvailable();
            metrics.put("status", emailFacade != null ? "READY" : "DISABLED");
            metrics.put("provider", emailFacade != null ? emailFacade.getProvider() : "KhÃ´ng cÃ³ provider");
        } catch (Exception e) {
            metrics.put("status", "ERROR");
            metrics.put("provider", "KhÃ´ng xÃ¡c Ä‘á»‹nh");
            metrics.put("message", e.getMessage());
        }
        return metrics;
    }

    private Map<String, Object> getCloudMetrics() {
        Map<String, Object> metrics = new LinkedHashMap<>();
        try {
            CloudStorageFacade cloudStorageFacade = cloudStorageFacadeProvider.getIfAvailable();
            if (cloudStorageFacade == null) {
                metrics.put("status", "DISABLED");
                metrics.put("services", Map.of());
                metrics.put("enabledServices", List.of());
                return metrics;
            }
            Map<String, Object> services = cloudStorageFacade.getHealthStatus();
            metrics.put("status", services.values().stream().anyMatch("online"::equals) ? "READY" : "LIMITED");
            metrics.put("services", services);
            metrics.put("enabledServices", cloudStorageFacade.getEnabledServices());
        } catch (Exception e) {
            metrics.put("status", "ERROR");
            metrics.put("services", Map.of());
            metrics.put("enabledServices", List.of());
            metrics.put("message", e.getMessage());
        }
        return metrics;
    }

    private Map<String, Object> getAuditMetrics() {
        Map<String, Object> metrics = new LinkedHashMap<>();
        try {
            List<Map<String, Object>> recent = hrAuditLogService.findLogs(null, PageRequest.of(0, 8))
                    .getContent()
                    .stream()
                    .map(this::auditRow)
                    .toList();
            metrics.put("status", "READY");
            metrics.put("total", hrAuditLogRepository.count());
            metrics.put("recent", recent);
        } catch (Exception e) {
            metrics.put("status", "ERROR");
            metrics.put("total", 0L);
            metrics.put("recent", List.of());
            metrics.put("message", e.getMessage());
        }
        return metrics;
    }

    private Map<String, Object> auditRow(HrAuditLog log) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("time", log.getCreatedAt() != null
                ? log.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                : "");
        row.put("actor", log.getActorUsername());
        row.put("action", log.getAction());
        row.put("entityType", log.getEntityType());
        row.put("entityId", log.getEntityId());
        return row;
    }

    private Map<String, Object> getNotificationMetrics() {
        Map<String, Object> metrics = new LinkedHashMap<>();
        try {
            metrics.put("status", "READY");
            metrics.put("total", notificationRepository.count());
        } catch (Exception e) {
            metrics.put("status", "ERROR");
            metrics.put("total", 0L);
            metrics.put("message", e.getMessage());
        }
        return metrics;
    }

    private List<Map<String, Object>> getReadinessChecklist() {
        List<Map<String, Object>> items = new ArrayList<>();
        items.add(readiness("Menu vÃ  font", "IN_PROGRESS", "Admin sidebar Ä‘Ã£ gom vá» fragment chung, cÃ¡c trang cÅ© tiáº¿p tá»¥c Ä‘Æ°á»£c chuyá»ƒn dáº§n vá» cÃ¹ng layout."));
        items.add(readiness("Workflow HR chÃ­nh", "IN_PROGRESS", "NhÃ¢n viÃªn, nghá»‰ phÃ©p, cháº¥m cÃ´ng, lÆ°Æ¡ng, tuyá»ƒn dá»¥ng vÃ  onboarding Ä‘Ã£ cÃ³ luá»“ng ná»n."));
        items.add(readiness("LMS vÃ  React islands", "IN_PROGRESS", "Quáº£n trá»‹ khÃ³a há»c Ä‘Ã£ dÃ¹ng React island, chi tiáº¿t khÃ³a há»c cÃ³ video Cloudinary vÃ  cáº­p nháº­t tiáº¿n Ä‘á»™."));
        items.add(readiness("AI vÃ  Health Insight", "READY", "API tráº£ káº¿t quáº£ Ä‘á»“ng bá»™, cáº£nh bÃ¡o vÃ  audit cháº¡y qua event pipeline."));
        items.add(readiness("Kafka/Event-driven", "READY", "Audit vÃ  Health Insight Ä‘Ã£ cÃ³ topic, retry vÃ  dead-letter topic."));
        return items;
    }

    private Map<String, Object> readiness(String name, String status, String note) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("name", name);
        item.put("status", status);
        item.put("note", note);
        return item;
    }
}


