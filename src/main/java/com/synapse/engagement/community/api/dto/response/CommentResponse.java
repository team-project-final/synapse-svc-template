package com.synapse.engagement.community.api.dto.response;

public record CommentResponse(Long id, Long postId, Long authorId, String body) {
}
