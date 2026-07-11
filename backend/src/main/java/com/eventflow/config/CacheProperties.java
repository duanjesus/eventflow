package com.eventflow.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "eventflow.cache")
public record CacheProperties(long dashboardStatsTtlSeconds) {
}
