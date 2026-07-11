package com.pulsequeue.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record PublishEventRequest(
        @NotBlank String eventType,
        @NotBlank String sourceService,
        @NotNull Map<String, Object> payload
) {
}
