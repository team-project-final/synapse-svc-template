package com.synapse.engagement.gamification.infrastructure.messaging;

import com.synapse.engagement.gamification.application.GamificationService;
import com.synapse.engagement.gamification.domain.policy.PointPolicy;
import com.synapse.engagement.global.kafka.event.CommentCreated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
class CommentCreatedKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(CommentCreatedKafkaConsumer.class);

    private final GamificationService gamificationService;

    CommentCreatedKafkaConsumer(GamificationService gamificationService) {
        this.gamificationService = gamificationService;
    }

    @KafkaListener(
        topics = "synapse.engagement.community.comment-created.v1",
        groupId = "synapse-engagement-gamification"
    )
    public void onCommentCreated(CommentCreated event) {
        log.info("CommentCreated → 댓글 포인트: userId={}", event.authorId());
        gamificationService.awardForEvent(event.authorId(), PointPolicy.REASON_COMMENT, 0);
    }
}
