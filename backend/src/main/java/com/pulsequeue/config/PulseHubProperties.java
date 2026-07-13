package com.pulsequeue.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pulsequeue.pulsehub")
public record PulseHubProperties(String baseUrl, String apiKey) {
}
