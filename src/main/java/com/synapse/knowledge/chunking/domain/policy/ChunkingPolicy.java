package com.synapse.knowledge.chunking.domain.policy;

/**
 * 청크 분할 정책 — 외부 의존성 0.
 * 임베딩 모델·검색 시스템에 맞춰 크기를 조정.
 */
public final class ChunkingPolicy {

    private static final int CHUNK_SIZE = 500;
    private static final int OVERLAP = 50;

    private ChunkingPolicy() {}

    public static int chunkSize() { return CHUNK_SIZE; }
    public static int overlap() { return OVERLAP; }
}
