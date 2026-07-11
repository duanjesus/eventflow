package com.eventflow.service;

import com.eventflow.config.RateLimitProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

/**
 * Fixed-window rate limiter, keyed per source service so one noisy producer
 * can't starve another. The window key rolls over automatically (the
 * timestamp is baked into the key), so there's nothing to reset explicitly.
 */
@Service
public class RateLimitService {

    private static final String KEY_PREFIX = "ratelimit:";

    private final StringRedisTemplate redisTemplate;
    private final RateLimitProperties properties;

    public RateLimitService(StringRedisTemplate redisTemplate, RateLimitProperties properties) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
    }

    public boolean tryAcquire(String sourceService) {
        long window = Instant.now().getEpochSecond() / properties.windowSeconds();
        String key = KEY_PREFIX + sourceService + ":" + window;

        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redisTemplate.expire(key, Duration.ofSeconds(properties.windowSeconds()));
        }
        return count != null && count <= properties.maxEventsPerWindow();
    }
}
