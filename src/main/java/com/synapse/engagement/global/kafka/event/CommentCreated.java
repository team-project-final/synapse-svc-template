package com.synapse.engagement.global.kafka.event;

import java.time.Instant;

public record CommentCreated(Long commentId, Long postId, Long authorId, Instant createdAt) {
}
