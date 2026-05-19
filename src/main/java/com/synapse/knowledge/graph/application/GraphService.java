package com.synapse.knowledge.graph.application;

import com.synapse.knowledge.global.exception.BusinessException;
import com.synapse.knowledge.global.exception.ErrorCode;
import com.synapse.knowledge.graph.api.dto.request.LinkNodesRequest;
import com.synapse.knowledge.graph.api.dto.response.EdgeResponse;
import com.synapse.knowledge.graph.api.dto.response.NodeResponse;
import com.synapse.knowledge.graph.application.port.EdgePort;
import com.synapse.knowledge.graph.application.port.NodePort;
import com.synapse.knowledge.graph.domain.Edge;
import com.synapse.knowledge.graph.domain.policy.GraphLinkPolicy;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GraphService {

    private final NodePort nodePort;
    private final EdgePort edgePort;

    public GraphService(NodePort nodePort, EdgePort edgePort) {
        this.nodePort = nodePort;
        this.edgePort = edgePort;
    }

    public List<NodeResponse> allNodes() {
        return nodePort.findAll().stream()
            .map(n -> new NodeResponse(n.getId(), n.getLabel())).toList();
    }

    public EdgeResponse link(LinkNodesRequest request) {
        if (!GraphLinkPolicy.canLink(request.fromNodeId(), request.toNodeId())) {
            throw new BusinessException(ErrorCode.EDGE_SELF_LOOP);
        }
        Edge edge = edgePort.save(new Edge(request.fromNodeId(), request.toNodeId(), request.relation()));
        return new EdgeResponse(edge.getId(), edge.getFromNodeId(), edge.getToNodeId(), edge.getRelation());
    }
}
