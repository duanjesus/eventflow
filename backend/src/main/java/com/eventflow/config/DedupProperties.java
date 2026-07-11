package com.eventflow.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "eventflow.dedup")
public record DedupProperties(long ttlSeconds) {
}
