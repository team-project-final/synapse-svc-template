package com.synapse.knowledge.note.infrastructure.persistence;

import com.synapse.knowledge.note.domain.Note;
import org.springframework.data.jpa.repository.JpaRepository;

interface NoteJpaRepository extends JpaRepository<Note, Long> {
}
