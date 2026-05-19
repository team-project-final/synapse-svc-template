package com.synapse.knowledge.graph.controller;

import com.synapse.knowledge.graph.dto.request.LinkNodesRequest;
import com.synapse.knowledge.graph.dto.response.EdgeResponse;
import com.synapse.knowledge.graph.dto.response.NodeResponse;
import com.synapse.knowledge.graph.service.GraphService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/graph")
public class GraphController {

    private final GraphService graphService;

    public GraphController(GraphService graphService) {
        this.graphService = graphService;
    }

    @GetMapping("/nodes")
    public List<NodeResponse> nodes() {
        return graphService.allNodes();
    }

    @PostMapping("/edges")
    public EdgeResponse link(@RequestBody LinkNodesRequest request) {
        return graphService.link(request);
    }
}
