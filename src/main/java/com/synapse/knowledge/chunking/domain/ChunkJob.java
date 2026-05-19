package com.synapse.knowledge.chunking.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "chunk_jobs")
public class ChunkJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long sourceNoteId;
    private String status;
    private Instant startedAt;
    private Instant completedAt;

    protected ChunkJob() {}

    public ChunkJob(Long sourceNoteId, String status) {
        this.sourceNoteId = sourceNoteId;
        this.status = status;
        this.startedAt = Instant.now();
    }

    public void markCompleted() {
        this.status = "COMPLETED";
        this.completedAt = Instant.now();
    }

    public Long getId() { return id; }
    public Long getSourceNoteId() { return sourceNoteId; }
    public String getStatus() { return status; }
}
