package com.synapse.engagement.community.dto.request;

public record CreatePostRequest(Long authorId, String title, String body) {
}
