package com.synapse.knowledge.graph.api.dto.response;

public record EdgeResponse(Long id, Long fromNodeId, Long toNodeId, String relation) {
}
