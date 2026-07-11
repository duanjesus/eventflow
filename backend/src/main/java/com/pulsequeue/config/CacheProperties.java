package com.pulsequeue.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pulsequeue.cache")
public record CacheProperties(long dashboardStatsTtlSeconds) {
}
