package com.synapse.engagement.community.infrastructure.messaging;

import com.synapse.engagement.community.application.port.EventPort;
import com.synapse.engagement.global.kafka.event.CommentCreated;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
class CommunityEventKafkaAdapter implements EventPort {

    public static final String TOPIC = "synapse.engagement.community.comment-created.v1";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    CommunityEventKafkaAdapter(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publishCommentCreated(CommentCreated event) {
        kafkaTemplate.send(TOPIC, event.commentId().toString(), event);
    }
}
