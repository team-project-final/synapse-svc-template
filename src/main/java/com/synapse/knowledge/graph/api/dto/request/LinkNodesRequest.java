package com.synapse.knowledge.graph.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LinkNodesRequest(
    @NotNull Long fromNodeId,
    @NotNull Long toNodeId,
    @NotBlank String relation
) {
}
