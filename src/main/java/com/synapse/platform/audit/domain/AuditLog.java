package com.synapse.platform.audit.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String action;

    private Long actorId;

    private Instant occurredAt;

    protected AuditLog() {}

    public AuditLog(String action, Long actorId) {
        this.action = action;
        this.actorId = actorId;
        this.occurredAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getAction() { return action; }
    public Long getActorId() { return actorId; }
    public Instant getOccurredAt() { return occurredAt; }
}
