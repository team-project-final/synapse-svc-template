package com.synapse.engagement.community.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCommentRequest(
    @NotNull Long authorId,
    @NotBlank String body
) {
}
