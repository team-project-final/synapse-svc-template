package com.synapse.knowledge.chunking.infrastructure.messaging;

import com.synapse.knowledge.chunking.application.port.EventPort;
import com.synapse.knowledge.global.kafka.event.ChunkReady;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
class ChunkEventKafkaAdapter implements EventPort {

    public static final String TOPIC = "synapse.knowledge.chunking.chunk-ready.v1";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    ChunkEventKafkaAdapter(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publishChunkReady(ChunkReady event) {
        kafkaTemplate.send(TOPIC, event.jobId().toString(), event);
    }
}
