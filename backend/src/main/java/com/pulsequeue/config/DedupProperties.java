package com.pulsequeue.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pulsequeue.dedup")
public record DedupProperties(long ttlSeconds) {
}
