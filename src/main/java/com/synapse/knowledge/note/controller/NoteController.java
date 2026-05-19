package com.synapse.knowledge.note.controller;

import com.synapse.knowledge.note.dto.request.CreateNoteRequest;
import com.synapse.knowledge.note.dto.response.NoteResponse;
import com.synapse.knowledge.note.service.NoteService;
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
    public NoteResponse create(@RequestBody CreateNoteRequest request) {
        return noteService.create(request);
    }

    @GetMapping
    public List<NoteResponse> list() {
        return noteService.findAll();
    }

    @GetMapping("/{id}")
    public NoteResponse get(@PathVariable Long id) {
        return noteService.findById(id);
    }
}
