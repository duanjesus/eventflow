package com.pulsequeue.service;

import com.pulsequeue.config.CacheProperties;
import com.pulsequeue.config.RabbitMqProperties;
import com.pulsequeue.dto.response.DashboardStatsResponse;
import com.pulsequeue.entity.ProcessedEventStatus;
import com.pulsequeue.repository.ProcessedEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.boot.actuate.amqp.RabbitHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Dashboard stats are cheap individually but the dashboard polls this
 * endpoint frequently, so the computed result is cached in Redis for a few
 * seconds ({@code pulsequeue.cache.dashboard-stats-ttl-seconds}) rather than
 * re-querying Postgres and RabbitMQ on every poll.
 */
@Service
public class DashboardStatsService {

    private static final String CACHE_KEY = "dashboard:stats";

    private final ProcessedEventRepository repository;
    private final AmqpAdmin amqpAdmin;
    private final RabbitMqProperties rabbitMqProperties;
    private final RabbitHealthIndicator rabbitHealthIndicator;
    private final StringRedisTemplate redisTemplate;
    private final CacheProperties cacheProperties;
    private final ObjectMapper objectMapper;

    public DashboardStatsService(ProcessedEventRepository repository,
                                  AmqpAdmin amqpAdmin,
                                  RabbitMqProperties rabbitMqProperties,
                                  RabbitHealthIndicator rabbitHealthIndicator,
                                  StringRedisTemplate redisTemplate,
                                  CacheProperties cacheProperties,
                                  ObjectMapper objectMapper) {
        this.repository = repository;
        this.amqpAdmin = amqpAdmin;
        this.rabbitMqProperties = rabbitMqProperties;
        this.rabbitHealthIndicator = rabbitHealthIndicator;
        this.redisTemplate = redisTemplate;
        this.cacheProperties = cacheProperties;
        this.objectMapper = objectMapper;
    }

    public DashboardStatsResponse getStats() {
        String cached = redisTemplate.opsForValue().get(CACHE_KEY);
        if (cached != null) {
            try {
                return objectMapper.readValue(cached, DashboardStatsResponse.class);
            } catch (Exception ignored) {
                // fall through and recompute on any cache corruption
            }
        }

        DashboardStatsResponse stats = computeStats();
        try {
            redisTemplate.opsForValue().set(CACHE_KEY, objectMapper.writeValueAsString(stats),
                    Duration.ofSeconds(cacheProperties.dashboardStatsTtlSeconds()));
        } catch (Exception ignored) {
            // caching is a best-effort optimization, never fail the request over it
        }
        return stats;
    }

    private DashboardStatsResponse computeStats() {
        QueueInformation queueInfo = amqpAdmin.getQueueInfo(rabbitMqProperties.queue());
        long queueDepth = queueInfo != null ? queueInfo.getMessageCount() : 0;
        int consumerCount = queueInfo != null ? queueInfo.getConsumerCount() : 0;

        long totalReceived = repository.count();
        long processed = repository.countByStatus(ProcessedEventStatus.PROCESSED);
        long failed = repository.countByStatus(ProcessedEventStatus.FAILED);
        long deadLettered = repository.countByStatus(ProcessedEventStatus.DEAD_LETTERED);
        long duplicate = repository.countByStatus(ProcessedEventStatus.DUPLICATE);
        long totalRetries = repository.sumRetryCount();

        return new DashboardStatsResponse(queueDepth, totalReceived, processed, failed, deadLettered, duplicate,
                totalRetries, consumerCount, getRabbitMqStatus());
    }

    private String getRabbitMqStatus() {
        try {
            Health health = rabbitHealthIndicator.health();
            return health.getStatus().getCode();
        } catch (Exception ex) {
            return "DOWN";
        }
    }
}
