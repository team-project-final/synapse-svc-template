package com.synapse.engagement.community.service;

import com.synapse.engagement.community.dto.request.CreateCommentRequest;
import com.synapse.engagement.community.dto.request.CreatePostRequest;
import com.synapse.engagement.community.dto.response.CommentResponse;
import com.synapse.engagement.community.dto.response.PostResponse;
import com.synapse.engagement.community.entity.Comment;
import com.synapse.engagement.community.entity.Post;
import com.synapse.engagement.community.kafka.producer.CommunityEventPublisher;
import com.synapse.engagement.community.repository.CommentRepository;
import com.synapse.engagement.community.repository.PostRepository;
import com.synapse.engagement.global.kafka.event.CommentCreated;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final CommunityEventPublisher eventPublisher;

    public PostService(PostRepository postRepository,
                       CommentRepository commentRepository,
                       CommunityEventPublisher eventPublisher) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.eventPublisher = eventPublisher;
    }

    public PostResponse createPost(CreatePostRequest request) {
        Post saved = postRepository.save(new Post(request.authorId(), request.title(), request.body()));
        return new PostResponse(saved.getId(), saved.getAuthorId(), saved.getTitle(), saved.getBody());
    }

    public List<PostResponse> findAll() {
        return postRepository.findAll().stream()
            .map(p -> new PostResponse(p.getId(), p.getAuthorId(), p.getTitle(), p.getBody()))
            .toList();
    }

    public CommentResponse createComment(Long postId, CreateCommentRequest request) {
        Comment saved = commentRepository.save(new Comment(postId, request.authorId(), request.body()));
        eventPublisher.publishCommentCreated(new CommentCreated(
            saved.getId(), postId, saved.getAuthorId(), Instant.now()));
        return new CommentResponse(saved.getId(), saved.getPostId(), saved.getAuthorId(), saved.getBody());
    }
}
