package com.pulsequeue.dto.request;

import jakarta.validation.constraints.NotNull;

public record InstitutionMappingRequest(
        @NotNull Long institutionId,
        @NotNull Long pulsehubUserId
) {
}
