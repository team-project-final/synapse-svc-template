package com.synapse.learning.srs.application;

import com.synapse.learning.global.kafka.event.CardReviewed;
import com.synapse.learning.global.kafka.event.SrsRecommendationRequest;
import com.synapse.learning.srs.api.dto.request.ReviewCardRequest;
import com.synapse.learning.srs.api.dto.response.ReviewResultResponse;
import com.synapse.learning.srs.application.port.EventPort;
import com.synapse.learning.srs.application.port.ReviewRecordPort;
import com.synapse.learning.srs.domain.ReviewRecord;
import com.synapse.learning.srs.domain.policy.SrsSchedulingPolicy;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class SrsService {

    private final ReviewRecordPort recordPort;
    private final EventPort eventPort;

    public SrsService(ReviewRecordPort recordPort, EventPort eventPort) {
        this.recordPort = recordPort;
        this.eventPort = eventPort;
    }

    public ReviewResultResponse review(ReviewCardRequest request) {
        long intervalDays = SrsSchedulingPolicy.nextIntervalDays(request.quality());
        Instant nextReview = Instant.now().plusSeconds(intervalDays * 86400);

        ReviewRecord record = recordPort.save(
            new ReviewRecord(request.cardId(), request.userId(), request.quality(), nextReview));

        eventPort.publishCardReviewed(
            new CardReviewed(request.cardId(), request.userId(), request.quality(), Instant.now()));

        eventPort.publishRecommendationRequest(
            new SrsRecommendationRequest(UUID.randomUUID().toString(), request.userId(), "after-review", 5, Instant.now()));

        return new ReviewResultResponse(record.getId(), record.getNextReviewAt(), intervalDays);
    }
}
