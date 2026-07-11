package com.pulsequeue.service;

import com.pulsequeue.config.RateLimitProperties;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RateLimitServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    private RateLimitService service;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        service = new RateLimitService(redisTemplate, new RateLimitProperties(3, 60));
    }

    @Test
    void allowsRequestsAtOrUnderTheLimit() {
        when(valueOperations.increment(anyString())).thenReturn(1L, 2L, 3L);

        assertTrue(service.tryAcquire("cashpilot"));
        assertTrue(service.tryAcquire("cashpilot"));
        assertTrue(service.tryAcquire("cashpilot"));
    }

    @Test
    void rejectsRequestsOverTheLimit() {
        when(valueOperations.increment(anyString())).thenReturn(4L);

        assertFalse(service.tryAcquire("cashpilot"));
    }

    @Test
    void setsExpiryOnlyOnFirstIncrementInAWindow() {
        when(valueOperations.increment(anyString())).thenReturn(1L);

        service.tryAcquire("cashpilot");

        verify(redisTemplate).expire(anyString(), eq(Duration.ofSeconds(60)));
    }
}
