package com.synapse.knowledge.note.api.dto.response;

public record NoteResponse(Long id, String title, String body, Long ownerId) {
}
