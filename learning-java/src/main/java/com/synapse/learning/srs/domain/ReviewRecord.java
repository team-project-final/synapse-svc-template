package com.synapse.learning.srs.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "review_records")
public class ReviewRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long cardId;
    private Long userId;
    private int quality;
    private Instant reviewedAt;
    private Instant nextReviewAt;

    protected ReviewRecord() {}

    public ReviewRecord(Long cardId, Long userId, int quality, Instant nextReviewAt) {
        this.cardId = cardId;
        this.userId = userId;
        this.quality = quality;
        this.reviewedAt = Instant.now();
        this.nextReviewAt = nextReviewAt;
    }

    public Long getId() { return id; }
    public Long getCardId() { return cardId; }
    public Long getUserId() { return userId; }
    public int getQuality() { return quality; }
    public Instant getNextReviewAt() { return nextReviewAt; }
}
