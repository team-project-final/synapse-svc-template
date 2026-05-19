package com.synapse.knowledge.chunking.service;

import com.synapse.knowledge.chunking.entity.Chunk;
import com.synapse.knowledge.chunking.entity.ChunkJob;
import com.synapse.knowledge.chunking.repository.ChunkJobRepository;
import com.synapse.knowledge.chunking.repository.ChunkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * chunking 도메인은 텍스트를 청크로 분할하는 파이프라인.
 * 외부에 HTTP 컨트롤러를 제공하지 않습니다.
 * W1에서는 메서드만 정의, W3에서 Kafka 컨슈머가 이 메서드를 호출하게 됩니다.
 */
@Service
public class ChunkingService {

    private static final int CHUNK_SIZE = 500;

    private final ChunkJobRepository jobRepository;
    private final ChunkRepository chunkRepository;

    public ChunkingService(ChunkJobRepository jobRepository, ChunkRepository chunkRepository) {
        this.jobRepository = jobRepository;
        this.chunkRepository = chunkRepository;
    }

    @Transactional
    public ChunkJob process(Long sourceNoteId, String text) {
        ChunkJob job = jobRepository.save(new ChunkJob(sourceNoteId, "RUNNING"));
        for (int i = 0; i < text.length(); i += CHUNK_SIZE) {
            int end = Math.min(text.length(), i + CHUNK_SIZE);
            chunkRepository.save(new Chunk(job.getId(), i / CHUNK_SIZE, text.substring(i, end)));
        }
        job.markCompleted();
        return jobRepository.save(job);
    }

    public List<ChunkJob> findAllJobs() {
        return jobRepository.findAll();
    }
}
