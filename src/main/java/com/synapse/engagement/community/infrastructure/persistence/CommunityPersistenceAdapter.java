package com.synapse.engagement.community.infrastructure.persistence;

import com.synapse.engagement.community.application.port.CommentPort;
import com.synapse.engagement.community.application.port.PostPort;
import com.synapse.engagement.community.domain.Comment;
import com.synapse.engagement.community.domain.Post;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
class CommunityPersistenceAdapter implements PostPort, CommentPort {

    private final PostJpaRepository postRepo;
    private final CommentJpaRepository commentRepo;

    CommunityPersistenceAdapter(PostJpaRepository postRepo, CommentJpaRepository commentRepo) {
        this.postRepo = postRepo;
        this.commentRepo = commentRepo;
    }

    @Override public Post save(Post post) { return postRepo.save(post); }
    @Override public List<Post> findAll() { return postRepo.findAll(); }
    @Override public Comment save(Comment comment) { return commentRepo.save(comment); }
}
