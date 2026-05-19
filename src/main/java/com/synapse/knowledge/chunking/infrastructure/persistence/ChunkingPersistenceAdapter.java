package com.synapse.knowledge.chunking.infrastructure.persistence;

import com.synapse.knowledge.chunking.application.port.ChunkJobPort;
import com.synapse.knowledge.chunking.application.port.ChunkPort;
import com.synapse.knowledge.chunking.domain.Chunk;
import com.synapse.knowledge.chunking.domain.ChunkJob;
import org.springframework.stereotype.Component;

@Component
class ChunkingPersistenceAdapter implements ChunkJobPort, ChunkPort {

    private final ChunkJobJpaRepository jobRepo;
    private final ChunkJpaRepository chunkRepo;

    ChunkingPersistenceAdapter(ChunkJobJpaRepository jobRepo, ChunkJpaRepository chunkRepo) {
        this.jobRepo = jobRepo;
        this.chunkRepo = chunkRepo;
    }

    @Override public ChunkJob save(ChunkJob job) { return jobRepo.save(job); }
    @Override public Chunk save(Chunk chunk) { return chunkRepo.save(chunk); }
}
