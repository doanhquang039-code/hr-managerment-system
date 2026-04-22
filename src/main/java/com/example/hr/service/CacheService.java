package com.example.hr.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Service quản lý cache Redis — evict, stats, manual operations.
 */
@Service
public class CacheService {

    private static final Logger log = LoggerFactory.getLogger(CacheService.class);

    private final RedisTemplate<String, Object> redisTemplate;

    public CacheService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /** Xóa toàn bộ cache dashboard */
    @CacheEvict(value = "dashboard", allEntries = true)
    public void evictDashboard() {
        log.info("Dashboard cache evicted");
    }

    /** Xóa cache users */
    @CacheEvict(value = "users", allEntries = true)
    public void evictUsers() {
        log.info("Users cache evicted");
    }

    /** Xóa cache departments */
    @CacheEvict(value = "departments", allEntries = true)
    public void evictDepartments() {
        log.info("Departments cache evicted");
    }

    /** Xóa tất cả cache HR */
    @Caching(evict = {
        @CacheEvict(value = "dashboard",    allEntries = true),
        @CacheEvict(value = "users",        allEntries = true),
        @CacheEvict(value = "departments",  allEntries = true),
        @CacheEvict(value = "positions",    allEntries = true),
        @CacheEvict(value = "payrolls",     allEntries = true),
        @CacheEvict(value = "kpiGoals",     allEntries = true),
        @CacheEvict(value = "videoLibrary", allEntries = true),
        @CacheEvict(value = "announcements",allEntries = true),
    })
    public void evictAll() {
        log.info("All HR caches evicted");
    }

    /** Lưu giá trị tùy ý vào Redis với TTL */
    public void set(String key, Object value, long ttlSeconds) {
        redisTemplate.opsForValue().set(key, value, ttlSeconds, TimeUnit.SECONDS);
    }

    /** Lấy giá trị từ Redis */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /** Xóa key */
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    /** Kiểm tra key tồn tại */
    public boolean exists(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /** Lấy tất cả keys theo pattern */
    public Set<String> keys(String pattern) {
        return redisTemplate.keys(pattern);
    }

    /** Đếm số keys đang cache */
    public long countKeys(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        return keys != null ? keys.size() : 0;
    }

    /** Increment counter (dùng cho rate limiting, view count...) */
    public Long increment(String key, long ttlSeconds) {
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            redisTemplate.expire(key, ttlSeconds, TimeUnit.SECONDS);
        }
        return count;
    }
}
