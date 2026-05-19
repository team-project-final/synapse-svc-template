package com.synapse.knowledge.note.api;

import com.synapse.knowledge.global.response.ApiResponse;
import com.synapse.knowledge.note.api.dto.request.CreateNoteRequest;
import com.synapse.knowledge.note.api.dto.response.NoteResponse;
import com.synapse.knowledge.note.application.NoteService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notes")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @PostMapping
    public ApiResponse<NoteResponse> create(@Valid @RequestBody CreateNoteRequest request) {
        return ApiResponse.ok(noteService.create(request));
    }

    @GetMapping
    public ApiResponse<List<NoteResponse>> list() {
        return ApiResponse.ok(noteService.findAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<NoteResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(noteService.findById(id));
    }
}
