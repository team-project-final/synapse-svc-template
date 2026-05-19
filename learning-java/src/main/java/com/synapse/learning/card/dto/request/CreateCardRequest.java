package com.synapse.learning.card.dto.request;

public record CreateCardRequest(Long ownerId, String frontText, String backText) {
}
