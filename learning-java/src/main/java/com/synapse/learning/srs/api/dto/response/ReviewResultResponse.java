package com.synapse.learning.srs.api.dto.response;

import java.time.Instant;

public record ReviewResultResponse(Long recordId, Instant nextReviewAt, long intervalDays) {
}
