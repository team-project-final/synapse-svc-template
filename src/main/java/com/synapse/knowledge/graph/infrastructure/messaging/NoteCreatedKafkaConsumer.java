package com.synapse.knowledge.graph.infrastructure.messaging;

import com.synapse.knowledge.global.kafka.event.NoteCreated;
import com.synapse.knowledge.graph.application.port.NodePort;
import com.synapse.knowledge.graph.domain.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component("graphNoteCreatedKafkaConsumer")
class NoteCreatedKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(NoteCreatedKafkaConsumer.class);

    private final NodePort nodePort;

    NoteCreatedKafkaConsumer(NodePort nodePort) {
        this.nodePort = nodePort;
    }

    @KafkaListener(
        topics = "synapse.knowledge.note.created.v1",
        groupId = "synapse-knowledge-graph"
    )
    public void onNoteCreated(NoteCreated event) {
        log.info("NoteCreated → graph node 생성: noteId={}", event.noteId());
        nodePort.save(new Node("note:" + event.noteId() + ":" + event.title()));
    }
}
