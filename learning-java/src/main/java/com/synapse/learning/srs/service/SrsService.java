package com.synapse.learning.srs.service;

import com.synapse.learning.srs.dto.request.ReviewCardRequest;
import com.synapse.learning.srs.dto.response.ReviewResultResponse;
import com.synapse.learning.srs.entity.ReviewRecord;
import com.synapse.learning.srs.repository.ReviewRecordRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class SrsService {

    private final ReviewRecordRepository repository;

    public SrsService(ReviewRecordRepository repository) {
        this.repository = repository;
    }

    /**
     * Spaced Repetition 알고리즘 (SM-2 단순화).
     * quality 0~5 입력으로 다음 복습 날짜 계산.
     */
    public ReviewResultResponse review(ReviewCardRequest request) {
        // 단순화: quality에 따라 다음 간격 결정 (실제 SM-2는 더 복잡)
        long nextIntervalDays = request.quality() >= 3 ? (long) Math.pow(2, request.quality() - 2) : 1;
        Instant nextReview = Instant.now().plus(Duration.ofDays(nextIntervalDays));

        ReviewRecord record = repository.save(
            new ReviewRecord(request.cardId(), request.userId(), request.quality(), nextReview)
        );

        return new ReviewResultResponse(record.getId(), record.getNextReviewAt(), nextIntervalDays);
    }
}
