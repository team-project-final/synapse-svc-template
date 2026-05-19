package com.synapse.knowledge.graph.kafka.consumer;

import com.synapse.knowledge.global.kafka.event.NoteCreated;
import com.synapse.knowledge.graph.entity.Node;
import com.synapse.knowledge.graph.repository.NodeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component("graphNoteCreatedConsumer")
public class NoteCreatedConsumer {

    private static final Logger log = LoggerFactory.getLogger(NoteCreatedConsumer.class);

    private final NodeRepository nodeRepository;

    public NoteCreatedConsumer(NodeRepository nodeRepository) {
        this.nodeRepository = nodeRepository;
    }

    @KafkaListener(
        topics = "synapse.knowledge.note.created.v1",
        groupId = "synapse-knowledge-graph"
    )
    public void onNoteCreated(NoteCreated event) {
        log.info("NoteCreated → graph node 생성: noteId={}", event.noteId());
        nodeRepository.save(new Node("note:" + event.noteId() + ":" + event.title()));
    }
}
