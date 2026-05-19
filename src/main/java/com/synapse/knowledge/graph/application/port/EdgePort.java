package com.synapse.knowledge.graph.application.port;

import com.synapse.knowledge.graph.domain.Edge;

public interface EdgePort {
    Edge save(Edge edge);
}
