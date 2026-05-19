package com.synapse.knowledge.graph.kafka.consumer;

import com.synapse.knowledge.global.kafka.event.ChunkReady;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ChunkReadyConsumer {

    private static final Logger log = LoggerFactory.getLogger(ChunkReadyConsumer.class);

    @KafkaListener(
        topics = "synapse.knowledge.chunking.chunk-ready.v1",
        groupId = "synapse-knowledge-graph"
    )
    public void onChunkReady(ChunkReady event) {
        log.info("ChunkReady → 그래프 임베딩/링크 갱신 예정: jobId={}", event.jobId());
        // TODO: 청크별로 임베딩 → 유사 노드 찾기 → edge 자동 생성
    }
}
