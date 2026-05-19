package com.synapse.knowledge.note.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateNoteRequest(
    @NotBlank String title,
    String body,
    @NotNull Long ownerId
) {
}
