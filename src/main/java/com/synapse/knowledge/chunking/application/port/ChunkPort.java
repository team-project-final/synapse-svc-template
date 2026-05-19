package com.synapse.knowledge.chunking.application.port;

import com.synapse.knowledge.chunking.domain.Chunk;

public interface ChunkPort {
    Chunk save(Chunk chunk);
}
