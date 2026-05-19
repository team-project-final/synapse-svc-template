package com.synapse.engagement.gamification.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "badges")
public class Badge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String code;
    private Instant awardedAt;

    protected Badge() {}

    public Badge(Long userId, String code) {
        this.userId = userId;
        this.code = code;
        this.awardedAt = Instant.now();
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getCode() { return code; }
}
