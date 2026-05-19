package com.synapse.knowledge.chunking.application.port;

import com.synapse.knowledge.chunking.domain.ChunkJob;

public interface ChunkJobPort {
    ChunkJob save(ChunkJob job);
}
