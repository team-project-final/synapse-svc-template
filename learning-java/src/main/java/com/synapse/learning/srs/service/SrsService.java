package com.synapse.learning.srs.service;

import com.synapse.learning.global.kafka.event.CardReviewed;
import com.synapse.learning.global.kafka.event.SrsRecommendationRequest;
import com.synapse.learning.srs.dto.request.ReviewCardRequest;
import com.synapse.learning.srs.dto.response.ReviewResultResponse;
import com.synapse.learning.srs.entity.ReviewRecord;
import com.synapse.learning.srs.kafka.producer.SrsEventPublisher;
import com.synapse.learning.srs.repository.ReviewRecordRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class SrsService {

    private final ReviewRecordRepository repository;
    private final SrsEventPublisher eventPublisher;

    public SrsService(ReviewRecordRepository repository, SrsEventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Spaced Repetition 알고리즘 (SM-2 단순화).
     * 복습 결과를 Kafka에 발행 — 엔게이지먼트 서비스 등이 이걸 듣고 포인트 부여.
     * 동시에 Python AI에 추천 요청도 발행.
     */
    public ReviewResultResponse review(ReviewCardRequest request) {
        long nextIntervalDays = request.quality() >= 3 ? (long) Math.pow(2, request.quality() - 2) : 1;
        Instant nextReview = Instant.now().plus(Duration.ofDays(nextIntervalDays));

        ReviewRecord record = repository.save(
            new ReviewRecord(request.cardId(), request.userId(), request.quality(), nextReview)
        );

        // 1) 활동 이벤트 (engagement 등이 구독)
        eventPublisher.publishCardReviewed(
            new CardReviewed(request.cardId(), request.userId(), request.quality(), Instant.now()));

        // 2) Python AI에 다음 추천 요청 (응답은 RecommendationReadyConsumer가 비동기 수신)
        eventPublisher.publishRecommendationRequest(
            new SrsRecommendationRequest(UUID.randomUUID().toString(), request.userId(), "after-review", 5, Instant.now()));

        return new ReviewResultResponse(record.getId(), record.getNextReviewAt(), nextIntervalDays);
    }
}
