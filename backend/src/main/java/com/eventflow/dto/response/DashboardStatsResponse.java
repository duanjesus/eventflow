package com.eventflow.dto.response;

public record DashboardStatsResponse(
        long queueDepth,
        long totalReceived,
        long processed,
        long failed,
        long deadLettered,
        long duplicate,
        long totalRetries
) {
}
