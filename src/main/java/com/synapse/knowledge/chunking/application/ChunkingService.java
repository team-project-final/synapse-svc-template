package com.synapse.knowledge.chunking.application;

import com.synapse.knowledge.chunking.application.port.ChunkJobPort;
import com.synapse.knowledge.chunking.application.port.ChunkPort;
import com.synapse.knowledge.chunking.application.port.EventPort;
import com.synapse.knowledge.chunking.domain.Chunk;
import com.synapse.knowledge.chunking.domain.ChunkJob;
import com.synapse.knowledge.chunking.domain.policy.ChunkingPolicy;
import com.synapse.knowledge.global.kafka.event.ChunkReady;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class ChunkingService {

    private final ChunkJobPort jobPort;
    private final ChunkPort chunkPort;
    private final EventPort eventPort;

    public ChunkingService(ChunkJobPort jobPort, ChunkPort chunkPort, EventPort eventPort) {
        this.jobPort = jobPort;
        this.chunkPort = chunkPort;
        this.eventPort = eventPort;
    }

    @Transactional
    public ChunkJob process(Long sourceNoteId, String text) {
        ChunkJob job = jobPort.save(new ChunkJob(sourceNoteId, "RUNNING"));
        int count = 0;
        for (int i = 0; i < text.length(); i += ChunkingPolicy.chunkSize()) {
            int end = Math.min(text.length(), i + ChunkingPolicy.chunkSize());
            chunkPort.save(new Chunk(job.getId(), count, text.substring(i, end)));
            count++;
        }
        job.markCompleted();
        ChunkJob saved = jobPort.save(job);
        eventPort.publishChunkReady(new ChunkReady(saved.getId(), sourceNoteId, count, Instant.now()));
        return saved;
    }
}
