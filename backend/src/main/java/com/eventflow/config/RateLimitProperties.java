package com.eventflow.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "eventflow.rate-limit")
public record RateLimitProperties(int maxEventsPerWindow, long windowSeconds) {
}
