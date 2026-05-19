package com.synapse.knowledge.note.application;

import com.synapse.knowledge.global.exception.BusinessException;
import com.synapse.knowledge.global.exception.ErrorCode;
import com.synapse.knowledge.global.kafka.event.NoteCreated;
import com.synapse.knowledge.note.api.dto.request.CreateNoteRequest;
import com.synapse.knowledge.note.api.dto.response.NoteResponse;
import com.synapse.knowledge.note.application.port.EventPort;
import com.synapse.knowledge.note.application.port.NotePort;
import com.synapse.knowledge.note.domain.Note;
import com.synapse.knowledge.note.domain.policy.NoteValidationPolicy;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class NoteService {

    private final NotePort notePort;
    private final EventPort eventPort;

    public NoteService(NotePort notePort, EventPort eventPort) {
        this.notePort = notePort;
        this.eventPort = eventPort;
    }

    public NoteResponse create(CreateNoteRequest request) {
        if (!NoteValidationPolicy.isValidTitle(request.title())) {
            throw new BusinessException(ErrorCode.NOTE_TITLE_BLANK);
        }
        Note saved = notePort.save(new Note(request.title(), request.body(), request.ownerId()));
        eventPort.publishNoteCreated(new NoteCreated(
            saved.getId(), saved.getOwnerId(), saved.getTitle(), saved.getBody(), Instant.now()
        ));
        return toResponse(saved);
    }

    public List<NoteResponse> findAll() {
        return notePort.findAll().stream().map(this::toResponse).toList();
    }

    public NoteResponse findById(Long id) {
        return notePort.findById(id).map(this::toResponse)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOTE_NOT_FOUND));
    }

    private NoteResponse toResponse(Note n) {
        return new NoteResponse(n.getId(), n.getTitle(), n.getBody(), n.getOwnerId());
    }
}
