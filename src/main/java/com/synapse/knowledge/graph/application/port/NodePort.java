package com.synapse.knowledge.graph.application.port;

import com.synapse.knowledge.graph.domain.Node;

import java.util.List;
import java.util.Optional;

public interface NodePort {
    Node save(Node node);
    Optional<Node> findById(Long id);
    List<Node> findAll();
}
