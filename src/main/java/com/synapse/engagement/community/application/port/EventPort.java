package com.synapse.engagement.community.application.port;

import com.synapse.engagement.global.kafka.event.CommentCreated;

public interface EventPort {
    void publishCommentCreated(CommentCreated event);
}
