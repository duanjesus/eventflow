package com.pulsequeue.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pulsequeue.security")
public record ApiKeyProperties(String apiKey) {
}
