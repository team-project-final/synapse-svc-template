package com.synapse.knowledge.chunking.application.port;

import com.synapse.knowledge.global.kafka.event.ChunkReady;

public interface EventPort {
    void publishChunkReady(ChunkReady event);
}
