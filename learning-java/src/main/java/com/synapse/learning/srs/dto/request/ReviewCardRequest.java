package com.synapse.learning.srs.dto.request;

public record ReviewCardRequest(Long cardId, Long userId, int quality) {
}
