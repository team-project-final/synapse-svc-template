package com.synapse.knowledge.graph.dto.request;

public record LinkNodesRequest(Long fromNodeId, Long toNodeId, String relation) {
}
