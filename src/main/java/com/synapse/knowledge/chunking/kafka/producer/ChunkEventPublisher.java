package com.synapse.knowledge.chunking.kafka.producer;

import com.synapse.knowledge.global.kafka.event.ChunkReady;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ChunkEventPublisher {

    public static final String TOPIC_CHUNK_READY = "synapse.knowledge.chunking.chunk-ready.v1";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public ChunkEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishChunkReady(ChunkReady event) {
        kafkaTemplate.send(TOPIC_CHUNK_READY, event.jobId().toString(), event);
    }
}
