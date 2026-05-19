package com.synapse.knowledge.note.repository;

import com.synapse.knowledge.note.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoteRepository extends JpaRepository<Note, Long> {
}
