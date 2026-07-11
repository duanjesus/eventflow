package com.eventflow.event;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record DomainEvent(
        String eventId,
        String eventType,
        String sourceService,
        Map<String, Object> payload,
        Instant occurredAt
) {

    public static DomainEvent of(String eventType, String sourceService, Map<String, Object> payload) {
        return new DomainEvent(UUID.randomUUID().toString(), eventType, sourceService, payload, Instant.now());
    }
}
