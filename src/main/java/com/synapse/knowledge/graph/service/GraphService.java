package com.synapse.knowledge.graph.service;

import com.synapse.knowledge.graph.dto.request.LinkNodesRequest;
import com.synapse.knowledge.graph.dto.response.EdgeResponse;
import com.synapse.knowledge.graph.dto.response.NodeResponse;
import com.synapse.knowledge.graph.entity.Edge;
import com.synapse.knowledge.graph.entity.Node;
import com.synapse.knowledge.graph.repository.EdgeRepository;
import com.synapse.knowledge.graph.repository.NodeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GraphService {

    private final NodeRepository nodeRepository;
    private final EdgeRepository edgeRepository;

    public GraphService(NodeRepository nodeRepository, EdgeRepository edgeRepository) {
        this.nodeRepository = nodeRepository;
        this.edgeRepository = edgeRepository;
    }

    public List<NodeResponse> allNodes() {
        return nodeRepository.findAll().stream()
            .map(n -> new NodeResponse(n.getId(), n.getLabel())).toList();
    }

    public EdgeResponse link(LinkNodesRequest request) {
        Edge edge = edgeRepository.save(new Edge(request.fromNodeId(), request.toNodeId(), request.relation()));
        return new EdgeResponse(edge.getId(), edge.getFromNodeId(), edge.getToNodeId(), edge.getRelation());
    }
}
