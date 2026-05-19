package com.synapse.engagement.community.application;

import com.synapse.engagement.community.api.dto.request.CreateCommentRequest;
import com.synapse.engagement.community.api.dto.request.CreatePostRequest;
import com.synapse.engagement.community.api.dto.response.CommentResponse;
import com.synapse.engagement.community.api.dto.response.PostResponse;
import com.synapse.engagement.community.application.port.CommentPort;
import com.synapse.engagement.community.application.port.EventPort;
import com.synapse.engagement.community.application.port.PostPort;
import com.synapse.engagement.community.domain.Comment;
import com.synapse.engagement.community.domain.Post;
import com.synapse.engagement.global.kafka.event.CommentCreated;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class PostService {

    private final PostPort postPort;
    private final CommentPort commentPort;
    private final EventPort eventPort;

    public PostService(PostPort postPort, CommentPort commentPort, EventPort eventPort) {
        this.postPort = postPort;
        this.commentPort = commentPort;
        this.eventPort = eventPort;
    }

    public PostResponse createPost(CreatePostRequest request) {
        Post saved = postPort.save(new Post(request.authorId(), request.title(), request.body()));
        return new PostResponse(saved.getId(), saved.getAuthorId(), saved.getTitle(), saved.getBody());
    }

    public List<PostResponse> findAll() {
        return postPort.findAll().stream()
            .map(p -> new PostResponse(p.getId(), p.getAuthorId(), p.getTitle(), p.getBody()))
            .toList();
    }

    public CommentResponse createComment(Long postId, CreateCommentRequest request) {
        Comment saved = commentPort.save(new Comment(postId, request.authorId(), request.body()));
        eventPort.publishCommentCreated(new CommentCreated(
            saved.getId(), postId, saved.getAuthorId(), Instant.now()));
        return new CommentResponse(saved.getId(), saved.getPostId(), saved.getAuthorId(), saved.getBody());
    }
}
