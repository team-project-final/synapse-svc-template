package com.synapse.knowledge.note.dto.request;

public record CreateNoteRequest(String title, String body, Long ownerId) {
}
