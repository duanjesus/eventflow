package com.pulsequeue.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pulsequeue.retry")
public record RetryProperties(
        int maxAttempts,
        long initialIntervalMs,
        double multiplier,
        long maxIntervalMs
) {
}
