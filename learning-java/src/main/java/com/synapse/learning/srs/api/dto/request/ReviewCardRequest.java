package com.synapse.learning.srs.api.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ReviewCardRequest(
    @NotNull Long cardId,
    @NotNull Long userId,
    @Min(0) @Max(5) int quality
) {
}
