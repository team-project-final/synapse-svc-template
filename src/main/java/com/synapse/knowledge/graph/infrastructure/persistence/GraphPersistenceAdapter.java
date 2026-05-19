package com.synapse.knowledge.graph.infrastructure.persistence;

import com.synapse.knowledge.graph.application.port.EdgePort;
import com.synapse.knowledge.graph.application.port.NodePort;
import com.synapse.knowledge.graph.domain.Edge;
import com.synapse.knowledge.graph.domain.Node;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Node와 Edge는 한 도메인의 두 측면이라 하나의 Adapter가 양쪽 port를 모두 구현.
 */
@Component
class GraphPersistenceAdapter implements NodePort, EdgePort {

    private final NodeJpaRepository nodeRepo;
    private final EdgeJpaRepository edgeRepo;

    GraphPersistenceAdapter(NodeJpaRepository nodeRepo, EdgeJpaRepository edgeRepo) {
        this.nodeRepo = nodeRepo;
        this.edgeRepo = edgeRepo;
    }

    @Override public Node save(Node node) { return nodeRepo.save(node); }
    @Override public Optional<Node> findById(Long id) { return nodeRepo.findById(id); }
    @Override public List<Node> findAll() { return nodeRepo.findAll(); }

    @Override public Edge save(Edge edge) { return edgeRepo.save(edge); }
}
