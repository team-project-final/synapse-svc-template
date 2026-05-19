package com.synapse.knowledge.graph.domain.policy;

/**
 * 그래프 연결 규칙 — 순수 도메인 룰.
 */
public final class GraphLinkPolicy {

    private GraphLinkPolicy() {}

    public static boolean canLink(Long fromNodeId, Long toNodeId) {
        if (fromNodeId == null || toNodeId == null) return false;
        return !fromNodeId.equals(toNodeId);   // 자기 자신과의 연결 금지
    }
}
