package com.synapse.knowledge.note.service;

import com.synapse.knowledge.global.kafka.event.NoteCreated;
import com.synapse.knowledge.note.dto.request.CreateNoteRequest;
import com.synapse.knowledge.note.dto.response.NoteResponse;
import com.synapse.knowledge.note.entity.Note;
import com.synapse.knowledge.note.kafka.producer.NoteEventPublisher;
import com.synapse.knowledge.note.repository.NoteRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class NoteService {

    private final NoteRepository noteRepository;
    private final NoteEventPublisher noteEventPublisher;

    public NoteService(NoteRepository noteRepository, NoteEventPublisher noteEventPublisher) {
        this.noteRepository = noteRepository;
        this.noteEventPublisher = noteEventPublisher;
    }

    public NoteResponse create(CreateNoteRequest request) {
        Note saved = noteRepository.save(new Note(request.title(), request.body(), request.ownerId()));
        noteEventPublisher.publishNoteCreated(new NoteCreated(
            saved.getId(), saved.getOwnerId(), saved.getTitle(), saved.getBody(), Instant.now()
        ));
        return toResponse(saved);
    }

    public List<NoteResponse> findAll() {
        return noteRepository.findAll().stream().map(this::toResponse).toList();
    }

    public NoteResponse findById(Long id) {
        return noteRepository.findById(id).map(this::toResponse)
            .orElseThrow(() -> new com.synapse.knowledge.global.exception.BusinessException(
                com.synapse.knowledge.global.exception.ErrorCode.NOTE_NOT_FOUND));
    }

    private NoteResponse toResponse(Note n) {
        return new NoteResponse(n.getId(), n.getTitle(), n.getBody(), n.getOwnerId());
    }
}
