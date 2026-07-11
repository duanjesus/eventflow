package com.pulsequeue.service;

import com.pulsequeue.config.DedupProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeduplicationServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    private DeduplicationService service;

    @BeforeEach
    void setUp() {
        service = new DeduplicationService(redisTemplate, new DedupProperties(86400));
    }

    @Test
    void isDuplicateReturnsFalseWhenKeyAbsent() {
        when(redisTemplate.hasKey("dedup:evt-1")).thenReturn(false);

        assertFalse(service.isDuplicate("evt-1"));
    }

    @Test
    void isDuplicateReturnsTrueWhenKeyPresent() {
        when(redisTemplate.hasKey("dedup:evt-1")).thenReturn(true);

        assertTrue(service.isDuplicate("evt-1"));
    }

    @Test
    void markProcessedSetsKeyWithConfiguredTtl() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        service.markProcessed("evt-1");

        verify(valueOperations).set("dedup:evt-1", "1", Duration.ofSeconds(86400));
    }
}
