package com.pulsequeue.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pulsequeue.rate-limit")
public record RateLimitProperties(int maxEventsPerWindow, long windowSeconds) {
}
