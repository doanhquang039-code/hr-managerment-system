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
 * Service quáº£n lÃ½ cache Redis â€” evict, stats, manual operations.
 */
@Service
public class CacheService {

    private static final Logger log = LoggerFactory.getLogger(CacheService.class);

    private final RedisTemplate<String, Object> redisTemplate;

    public CacheService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /** XÃ³a toÃ n bá»™ cache dashboard */
    @CacheEvict(value = "dashboard", allEntries = true)
    public void evictDashboard() {
        log.info("Dashboard cache evicted");
    }

    /** XÃ³a cache users */
    @CacheEvict(value = "users", allEntries = true)
    public void evictUsers() {
        log.info("Users cache evicted");
    }

    /** XÃ³a cache departments */
    @CacheEvict(value = "departments", allEntries = true)
    public void evictDepartments() {
        log.info("Departments cache evicted");
    }

    /** XÃ³a táº¥t cáº£ cache HR */
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

    /** LÆ°u giÃ¡ trá»‹ tÃ¹y Ã½ vÃ o Redis vá»›i TTL */
    public void set(String key, Object value, long ttlSeconds) {
        redisTemplate.opsForValue().set(key, value, ttlSeconds, TimeUnit.SECONDS);
    }

    /** Láº¥y giÃ¡ trá»‹ tá»« Redis */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /** XÃ³a key */
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    /** Kiá»ƒm tra key tá»“n táº¡i */
    public boolean exists(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /** Láº¥y táº¥t cáº£ keys theo pattern */
    public Set<String> keys(String pattern) {
        return redisTemplate.keys(pattern);
    }

    /** Äáº¿m sá»‘ keys Ä‘ang cache */
    public long countKeys(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        return keys != null ? keys.size() : 0;
    }

    /** Increment counter (dÃ¹ng cho rate limiting, view count...) */
    public Long increment(String key, long ttlSeconds) {
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            redisTemplate.expire(key, ttlSeconds, TimeUnit.SECONDS);
        }
        return count;
    }
}


