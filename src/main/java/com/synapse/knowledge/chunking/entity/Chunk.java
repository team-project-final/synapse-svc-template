package com.synapse.knowledge.chunking.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "chunks")
public class Chunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long jobId;
    private Integer ordinal;

    @Lob
    private String content;

    protected Chunk() {}

    public Chunk(Long jobId, Integer ordinal, String content) {
        this.jobId = jobId;
        this.ordinal = ordinal;
        this.content = content;
    }

    public Long getId() { return id; }
    public Long getJobId() { return jobId; }
    public Integer getOrdinal() { return ordinal; }
    public String getContent() { return content; }
}
