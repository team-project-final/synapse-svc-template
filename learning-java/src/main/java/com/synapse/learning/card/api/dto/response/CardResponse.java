package com.synapse.learning.card.api.dto.response;

public record CardResponse(Long id, Long ownerId, String frontText, String backText) {
}
