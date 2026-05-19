package com.synapse.platform.audit.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String action;

    private Long actorId;

    protected AuditLog() {}

    public Long getId() { return id; }
    public String getAction() { return action; }
    public Long getActorId() { return actorId; }
}
