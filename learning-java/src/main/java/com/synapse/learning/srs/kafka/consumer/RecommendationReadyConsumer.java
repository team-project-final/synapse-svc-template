package com.synapse.learning.srs.kafka.consumer;

import com.synapse.learning.global.kafka.event.SrsRecommendationReady;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Python AI 서비스가 발행한 추천 결과를 수신.
 * Java가 Python을 동기 호출하지 않고 이벤트로만 통신하는 핵심.
 */
@Component
public class RecommendationReadyConsumer {

    private static final Logger log = LoggerFactory.getLogger(RecommendationReadyConsumer.class);

    @KafkaListener(
        topics = "synapse.learning.ai.recommendation-ready.v1",
        groupId = "synapse-learning-srs"
    )
    public void onRecommendationReady(SrsRecommendationReady event) {
        log.info("RecommendationReady from Python: requestId={}, userId={}, count={}",
            event.requestId(), event.userId(), event.recommendedCardIds().size());
        // TODO: 캐시에 저장 → 다음 카드 조회 시 사용
    }
}
