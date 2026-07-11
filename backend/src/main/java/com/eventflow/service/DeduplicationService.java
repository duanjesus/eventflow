package com.eventflow.service;

import com.eventflow.config.DedupProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Idempotency guard for genuinely duplicate deliveries of the same eventId
 * (a second upstream publish, or a broker redelivery after a crash) — not
 * for Spring's in-process retry-with-backoff, which calls the listener
 * method again for the *same* delivery attempt. That's why the key is only
 * marked {@link #markProcessed(String)} once processing actually succeeds,
 * rather than claimed up front: claiming it before dispatch would make every
 * retry of a currently-failing delivery look like a duplicate of itself and
 * get silently skipped instead of retried.
 */
@Service
public class DeduplicationService {

    private static final String KEY_PREFIX = "dedup:";

    private final StringRedisTemplate redisTemplate;
    private final DedupProperties properties;

    public DeduplicationService(StringRedisTemplate redisTemplate, DedupProperties properties) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
    }

    public boolean isDuplicate(String eventId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_PREFIX + eventId));
    }

    public void markProcessed(String eventId) {
        redisTemplate.opsForValue().set(KEY_PREFIX + eventId, "1", Duration.ofSeconds(properties.ttlSeconds()));
    }
}
