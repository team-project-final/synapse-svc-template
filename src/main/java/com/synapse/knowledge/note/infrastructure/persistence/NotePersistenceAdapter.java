package com.synapse.knowledge.note.infrastructure.persistence;

import com.synapse.knowledge.note.application.port.NotePort;
import com.synapse.knowledge.note.domain.Note;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
class NotePersistenceAdapter implements NotePort {

    private final NoteJpaRepository jpaRepository;

    NotePersistenceAdapter(NoteJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override public Note save(Note note) { return jpaRepository.save(note); }
    @Override public Optional<Note> findById(Long id) { return jpaRepository.findById(id); }
    @Override public List<Note> findAll() { return jpaRepository.findAll(); }
}
