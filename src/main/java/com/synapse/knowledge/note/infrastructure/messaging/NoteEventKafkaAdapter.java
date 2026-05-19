package com.synapse.knowledge.note.infrastructure.messaging;

import com.synapse.knowledge.global.kafka.event.NoteCreated;
import com.synapse.knowledge.note.application.port.EventPort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
class NoteEventKafkaAdapter implements EventPort {

    public static final String TOPIC = "synapse.knowledge.note.created.v1";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    NoteEventKafkaAdapter(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publishNoteCreated(NoteCreated event) {
        kafkaTemplate.send(TOPIC, event.noteId().toString(), event);
    }
}
