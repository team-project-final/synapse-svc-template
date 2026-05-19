package com.synapse.knowledge.chunking.infrastructure.messaging;

import com.synapse.knowledge.chunking.application.ChunkingService;
import com.synapse.knowledge.global.kafka.event.NoteCreated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * chunking 도메인의 진짜 입구 — controller가 아닌 Kafka 컨슈머.
 */
@Component
class NoteCreatedKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(NoteCreatedKafkaConsumer.class);

    private final ChunkingService chunkingService;

    NoteCreatedKafkaConsumer(ChunkingService chunkingService) {
        this.chunkingService = chunkingService;
    }

    @KafkaListener(
        topics = "synapse.knowledge.note.created.v1",
        groupId = "synapse-knowledge-chunking"
    )
    public void onNoteCreated(NoteCreated event) {
        log.info("NoteCreated received → start chunking: noteId={}", event.noteId());
        chunkingService.process(event.noteId(), event.body());
    }
}
