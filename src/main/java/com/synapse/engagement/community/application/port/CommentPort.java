package com.synapse.engagement.community.application.port;

import com.synapse.engagement.community.domain.Comment;

public interface CommentPort {
    Comment save(Comment comment);
}
