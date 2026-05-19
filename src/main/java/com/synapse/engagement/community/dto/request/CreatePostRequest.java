package com.synapse.engagement.community.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreatePostRequest(
    @NotNull Long authorId,
    @NotBlank String title,
    String body
) {
}
