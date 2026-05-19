package com.synapse.knowledge.global.kafka.event;

import java.time.Instant;

public record ChunkReady(
    Long jobId,
    Long sourceNoteId,
    Integer totalChunks,
    Instant completedAt
) {
}
