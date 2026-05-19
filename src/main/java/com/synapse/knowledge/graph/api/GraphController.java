package com.synapse.knowledge.graph.api;

import com.synapse.knowledge.global.response.ApiResponse;
import com.synapse.knowledge.graph.api.dto.request.LinkNodesRequest;
import com.synapse.knowledge.graph.api.dto.response.EdgeResponse;
import com.synapse.knowledge.graph.api.dto.response.NodeResponse;
import com.synapse.knowledge.graph.application.GraphService;
import jakarta.validation.Valid;
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
    public ApiResponse<List<NodeResponse>> nodes() {
        return ApiResponse.ok(graphService.allNodes());
    }

    @PostMapping("/edges")
    public ApiResponse<EdgeResponse> link(@Valid @RequestBody LinkNodesRequest request) {
        return ApiResponse.ok(graphService.link(request));
    }
}
