package com.synapse.knowledge.note.application.port;

import com.synapse.knowledge.note.domain.Note;

import java.util.List;
import java.util.Optional;

public interface NotePort {
    Note save(Note note);
    Optional<Note> findById(Long id);
    List<Note> findAll();
}
