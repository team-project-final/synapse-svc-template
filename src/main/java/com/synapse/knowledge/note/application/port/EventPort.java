package com.synapse.knowledge.note.application.port;

import com.synapse.knowledge.global.kafka.event.NoteCreated;

public interface EventPort {
    void publishNoteCreated(NoteCreated event);
}
