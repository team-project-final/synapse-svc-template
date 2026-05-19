package com.synapse.knowledge.note.kafka.producer;

import com.synapse.knowledge.global.kafka.event.NoteCreated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class NoteEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(NoteEventPublisher.class);
    public static final String TOPIC_NOTE_CREATED = "synapse.knowledge.note.created.v1";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public NoteEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishNoteCreated(NoteCreated event) {
        log.info("Publishing NoteCreated: noteId={}", event.noteId());
        kafkaTemplate.send(TOPIC_NOTE_CREATED, event.noteId().toString(), event);
    }
}
