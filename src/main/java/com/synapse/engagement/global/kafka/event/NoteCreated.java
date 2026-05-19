package com.synapse.engagement.global.kafka.event;

import java.time.Instant;

public record NoteCreated(Long noteId, Long ownerId, String title, String body, Instant createdAt) {
}
