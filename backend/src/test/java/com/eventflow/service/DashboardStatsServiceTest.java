package com.eventflow.service;

import com.eventflow.config.CacheProperties;
import com.eventflow.config.RabbitMqProperties;
import com.eventflow.dto.response.DashboardStatsResponse;
import com.eventflow.entity.ProcessedEventStatus;
import com.eventflow.repository.ProcessedEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardStatsServiceTest {

    @Mock
    private ProcessedEventRepository repository;
    @Mock
    private AmqpAdmin amqpAdmin;
    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    private final RabbitMqProperties rabbitMqProperties =
            new RabbitMqProperties("eventflow.exchange", "eventflow.events.queue", "eventflow.exchange.dlx", "eventflow.events.dlq", "#");
    private final CacheProperties cacheProperties = new CacheProperties(5);
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private DashboardStatsService service;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        service = new DashboardStatsService(repository, amqpAdmin, rabbitMqProperties, redisTemplate, cacheProperties, objectMapper);
    }

    @Test
    void computesStatsFromRepositoryAndQueueWhenCacheEmpty() {
        when(valueOperations.get("dashboard:stats")).thenReturn(null);
        when(amqpAdmin.getQueueInfo("eventflow.events.queue"))
                .thenReturn(new QueueInformation("eventflow.events.queue", 7, 0));
        when(repository.count()).thenReturn(10L);
        when(repository.countByStatus(ProcessedEventStatus.PROCESSED)).thenReturn(6L);
        when(repository.countByStatus(ProcessedEventStatus.FAILED)).thenReturn(2L);
        when(repository.countByStatus(ProcessedEventStatus.DEAD_LETTERED)).thenReturn(1L);
        when(repository.countByStatus(ProcessedEventStatus.DUPLICATE)).thenReturn(1L);
        when(repository.sumRetryCount()).thenReturn(3L);

        DashboardStatsResponse stats = service.getStats();

        assertEquals(7, stats.queueDepth());
        assertEquals(10, stats.totalReceived());
        assertEquals(6, stats.processed());
        assertEquals(2, stats.failed());
        assertEquals(1, stats.deadLettered());
        assertEquals(1, stats.duplicate());
        assertEquals(3, stats.totalRetries());
        verify(valueOperations).set(eq("dashboard:stats"), anyString(), eq(Duration.ofSeconds(5)));
    }

    @Test
    void returnsCachedStatsWithoutHittingRepositoryWhenCachePresent() throws Exception {
        DashboardStatsResponse cached = new DashboardStatsResponse(1, 2, 3, 4, 5, 6, 7);
        when(valueOperations.get("dashboard:stats")).thenReturn(objectMapper.writeValueAsString(cached));

        DashboardStatsResponse result = service.getStats();

        assertEquals(cached, result);
        verifyNoInteractions(repository);
        verifyNoInteractions(amqpAdmin);
        verify(valueOperations, never()).set(anyString(), anyString(), eq(Duration.ofSeconds(5)));
    }
}
