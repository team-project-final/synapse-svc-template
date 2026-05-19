package com.synapse.knowledge.chunking.kafka.consumer;

import com.synapse.knowledge.chunking.entity.ChunkJob;
import com.synapse.knowledge.chunking.kafka.producer.ChunkEventPublisher;
import com.synapse.knowledge.chunking.service.ChunkingService;
import com.synapse.knowledge.global.kafka.event.ChunkReady;
import com.synapse.knowledge.global.kafka.event.NoteCreated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * chunking 도메인의 진짜 입구 — controller가 아닌 Kafka 컨슈머.
 * note가 생성되면 자동으로 청크 분할이 시작됨.
 * 이게 W1/W2에서 chunking이 외부에서 invokable이 아니었던 이유.
 */
@Component
public class NoteCreatedConsumer {

    private static final Logger log = LoggerFactory.getLogger(NoteCreatedConsumer.class);

    private final ChunkingService chunkingService;
    private final ChunkEventPublisher chunkEventPublisher;

    public NoteCreatedConsumer(ChunkingService chunkingService, ChunkEventPublisher chunkEventPublisher) {
        this.chunkingService = chunkingService;
        this.chunkEventPublisher = chunkEventPublisher;
    }

    @KafkaListener(
        topics = "synapse.knowledge.note.created.v1",
        groupId = "synapse-knowledge-chunking"
    )
    public void onNoteCreated(NoteCreated event) {
        log.info("NoteCreated received → start chunking: noteId={}", event.noteId());
        ChunkJob job = chunkingService.process(event.noteId(), event.body());
        chunkEventPublisher.publishChunkReady(new ChunkReady(
            job.getId(), event.noteId(), null, Instant.now()
        ));
    }
}
