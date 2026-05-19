package com.synapse.knowledge.note.service;

import com.synapse.knowledge.note.dto.request.CreateNoteRequest;
import com.synapse.knowledge.note.dto.response.NoteResponse;
import com.synapse.knowledge.note.entity.Note;
import com.synapse.knowledge.note.repository.NoteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NoteService {

    private final NoteRepository noteRepository;

    public NoteService(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    public NoteResponse create(CreateNoteRequest request) {
        Note saved = noteRepository.save(new Note(request.title(), request.body(), request.ownerId()));
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
