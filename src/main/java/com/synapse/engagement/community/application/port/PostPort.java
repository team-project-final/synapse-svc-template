package com.synapse.engagement.community.application.port;

import com.synapse.engagement.community.domain.Post;

import java.util.List;

public interface PostPort {
    Post save(Post post);
    List<Post> findAll();
}
