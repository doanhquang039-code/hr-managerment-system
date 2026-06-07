package com.example.hr.api;

import com.example.hr.service.CacheService;
import com.example.hr.service.CloudStorageFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * REST API cho Admin â€” System Health & Metrics.
 * YÃªu cáº§u role ADMIN.
 * Xem táº¡i: /swagger-ui.html â†’ Admin Metrics
 */
@RestController
@RequestMapping("/api/admin/metrics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Metrics", description = "System health, memory, thread & cache metrics cho Admin")
public class AdminMetricsRestController {

    private final CacheService cacheService;
    private final CloudStorageFacade cloudStorageFacade;

    /** Tá»•ng quan há»‡ thá»‘ng */
    @GetMapping("/health")
    @Operation(summary = "System health overview", description = "Tráº£ vá» tráº¡ng thÃ¡i tá»•ng quan: JVM, Redis, Cloud services")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "UP");
        result.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        result.put("application", "HR Management System v2.0");

        // JVM Info
        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        result.put("jvm", Map.of(
                "uptime_minutes", runtime.getUptime() / 60_000,
                "java_version", System.getProperty("java.version"),
                "jvm_name", runtime.getVmName()
        ));

        // Memory
        MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
        long heapUsed   = memory.getHeapMemoryUsage().getUsed() / (1024 * 1024);
        long heapMax    = memory.getHeapMemoryUsage().getMax()  / (1024 * 1024);
        long nonHeapUsed = memory.getNonHeapMemoryUsage().getUsed() / (1024 * 1024);
        result.put("memory", Map.of(
                "heap_used_mb", heapUsed,
                "heap_max_mb", heapMax,
                "heap_usage_pct", heapMax > 0 ? (heapUsed * 100 / heapMax) : 0,
                "non_heap_mb", nonHeapUsed
        ));

        // Threads
        ThreadMXBean threads = ManagementFactory.getThreadMXBean();
        result.put("threads", Map.of(
                "total", threads.getThreadCount(),
                "peak", threads.getPeakThreadCount(),
                "daemon", threads.getDaemonThreadCount()
        ));

        // Redis status
        Map<String, Object> redisInfo = new LinkedHashMap<>();
        try {
            long totalKeys = Optional.ofNullable(cacheService.keys("*"))
                    .map(Set::size).orElse(0);
            redisInfo.put("status", "ONLINE");
            redisInfo.put("total_keys", totalKeys);
        } catch (Exception e) {
            redisInfo.put("status", "OFFLINE");
            redisInfo.put("error", e.getMessage());
        }
        result.put("redis", redisInfo);

        // Cloud services
        result.put("cloud_services", cloudStorageFacade.getHealthStatus());

        return ResponseEntity.ok(result);
    }

    /** Chi tiáº¿t memory */
    @GetMapping("/memory")
    @Operation(summary = "Memory details", description = "Heap vÃ  non-heap memory usage chi tiáº¿t")
    public ResponseEntity<Map<String, Object>> memoryDetails() {
        MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
        Map<String, Object> result = new LinkedHashMap<>();

        var heap = memory.getHeapMemoryUsage();
        result.put("heap", Map.of(
                "init_mb",      heap.getInit()      / (1024 * 1024),
                "used_mb",      heap.getUsed()       / (1024 * 1024),
                "committed_mb", heap.getCommitted()  / (1024 * 1024),
                "max_mb",       heap.getMax()        / (1024 * 1024)
        ));

        var nonHeap = memory.getNonHeapMemoryUsage();
        result.put("non_heap", Map.of(
                "init_mb",      nonHeap.getInit()      / (1024 * 1024),
                "used_mb",      nonHeap.getUsed()       / (1024 * 1024),
                "committed_mb", nonHeap.getCommitted()  / (1024 * 1024)
        ));

        return ResponseEntity.ok(result);
    }

    /** Cache statistics */
    @GetMapping("/cache")
    @Operation(summary = "Cache statistics", description = "Sá»‘ lÆ°á»£ng Redis keys theo tá»«ng cache bucket")
    public ResponseEntity<Map<String, Object>> cacheStats() {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            result.put("redis_status", "ONLINE");
            result.put("total_keys",     cacheService.countKeys("*"));
            result.put("dashboard_keys", cacheService.countKeys("dashboard*"));
            result.put("user_keys",      cacheService.countKeys("users*"));
            result.put("dept_keys",      cacheService.countKeys("departments*"));
            result.put("video_keys",     cacheService.countKeys("videoLibrary*"));
            result.put("kpi_keys",       cacheService.countKeys("kpiGoals*"));
            result.put("payroll_keys",   cacheService.countKeys("payrolls*"));
        } catch (Exception e) {
            result.put("redis_status", "OFFLINE");
            result.put("error", e.getMessage());
        }
        return ResponseEntity.ok(result);
    }

    /** Evict cache theo tÃªn */
    @DeleteMapping("/cache/{name}")
    @Operation(summary = "Evict cache", description = "XÃ³a cache theo tÃªn: dashboard, users, departments, all")
    public ResponseEntity<Map<String, String>> evictCache(@PathVariable String name) {
        try {
            switch (name) {
                case "dashboard"   -> cacheService.evictDashboard();
                case "users"       -> cacheService.evictUsers();
                case "departments" -> cacheService.evictDepartments();
                case "all"         -> cacheService.evictAll();
                default            -> cacheService.delete(name);
            }
            return ResponseEntity.ok(Map.of("status", "OK", "message", "ÄÃ£ xÃ³a cache: " + name));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("status", "ERROR", "message", e.getMessage()));
        }
    }
}


