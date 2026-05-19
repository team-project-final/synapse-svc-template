package com.synapse.engagement.gamification.kafka.consumer;

import com.synapse.engagement.gamification.service.GamificationService;
import com.synapse.engagement.global.kafka.event.CommentCreated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * community의 댓글 이벤트를 수신 — 같은 서비스 내 다른 도메인이지만 직접 호출 아닌 이벤트로.
 */
@Component
public class CommentCreatedConsumer {

    private static final Logger log = LoggerFactory.getLogger(CommentCreatedConsumer.class);

    private final GamificationService gamificationService;

    public CommentCreatedConsumer(GamificationService gamificationService) {
        this.gamificationService = gamificationService;
    }

    @KafkaListener(
        topics = "synapse.engagement.community.comment-created.v1",
        groupId = "synapse-engagement-gamification"
    )
    public void onCommentCreated(CommentCreated event) {
        log.info("CommentCreated → 댓글 포인트: userId={}", event.authorId());
        gamificationService.awardPoint(event.authorId(), 10, "COMMENT");
    }
}
