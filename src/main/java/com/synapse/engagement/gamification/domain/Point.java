package com.synapse.engagement.gamification.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "points")
public class Point {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private long amount;
    private String reason;
    private Instant awardedAt;

    protected Point() {}

    public Point(Long userId, long amount, String reason) {
        this.userId = userId;
        this.amount = amount;
        this.reason = reason;
        this.awardedAt = Instant.now();
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public long getAmount() { return amount; }
    public String getReason() { return reason; }
}
