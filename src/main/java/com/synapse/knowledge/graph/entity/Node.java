package com.synapse.knowledge.graph.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "graph_nodes")
public class Node {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String label;

    protected Node() {}

    public Node(String label) { this.label = label; }

    public Long getId() { return id; }
    public String getLabel() { return label; }
}
