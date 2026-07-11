package com.eventflow.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "eventflow.retry")
public record RetryProperties(
        int maxAttempts,
        long initialIntervalMs,
        double multiplier,
        long maxIntervalMs
) {
}
