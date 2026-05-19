package com.synapse.knowledge.note.dto.response;

public record NoteResponse(Long id, String title, String body, Long ownerId) {
}
