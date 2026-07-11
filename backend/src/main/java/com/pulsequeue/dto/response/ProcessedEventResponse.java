package com.pulsequeue.dto.response;

import com.pulsequeue.entity.ProcessedEvent;

import java.time.Instant;

public record ProcessedEventResponse(
        Long id,
        String eventId,
        String eventType,
        String sourceService,
        String status,
        int retryCount,
        String errorMessage,
        Instant receivedAt,
        Instant processedAt
) {

    public static ProcessedEventResponse from(ProcessedEvent event) {
        return new ProcessedEventResponse(
                event.getId(),
                event.getEventId(),
                event.getEventType(),
                event.getSourceService(),
                event.getStatus().name(),
                event.getRetryCount(),
                event.getErrorMessage(),
                event.getReceivedAt(),
                event.getProcessedAt());
    }
}
