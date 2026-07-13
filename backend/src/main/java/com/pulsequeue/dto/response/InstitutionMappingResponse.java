package com.pulsequeue.dto.response;

import java.time.Instant;

public record InstitutionMappingResponse(Long institutionId, Long pulsehubUserId, Instant createdAt) {
}
