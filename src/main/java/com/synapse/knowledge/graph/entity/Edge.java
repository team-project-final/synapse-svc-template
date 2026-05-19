package com.synapse.knowledge.graph.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "graph_edges")
public class Edge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long fromNodeId;
    private Long toNodeId;
    private String relation;

    protected Edge() {}

    public Edge(Long fromNodeId, Long toNodeId, String relation) {
        this.fromNodeId = fromNodeId;
        this.toNodeId = toNodeId;
        this.relation = relation;
    }

    public Long getId() { return id; }
    public Long getFromNodeId() { return fromNodeId; }
    public Long getToNodeId() { return toNodeId; }
    public String getRelation() { return relation; }
}
