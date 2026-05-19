package com.synapse.engagement.community.kafka.producer;

import com.synapse.engagement.global.kafka.event.CommentCreated;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class CommunityEventPublisher {

    public static final String TOPIC_COMMENT_CREATED = "synapse.engagement.community.comment-created.v1";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public CommunityEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishCommentCreated(CommentCreated event) {
        kafkaTemplate.send(TOPIC_COMMENT_CREATED, event.commentId().toString(), event);
    }
}
