package com.synapse.learning.card.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCardRequest(
    @NotNull Long ownerId,
    @NotBlank String frontText,
    @NotBlank String backText
) {
}
