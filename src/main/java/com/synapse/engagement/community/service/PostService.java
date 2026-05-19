package com.synapse.engagement.community.service;

import com.synapse.engagement.community.dto.request.CreateCommentRequest;
import com.synapse.engagement.community.dto.request.CreatePostRequest;
import com.synapse.engagement.community.dto.response.CommentResponse;
import com.synapse.engagement.community.dto.response.PostResponse;
import com.synapse.engagement.community.entity.Comment;
import com.synapse.engagement.community.entity.Post;
import com.synapse.engagement.community.repository.CommentRepository;
import com.synapse.engagement.community.repository.PostRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    public PostService(PostRepository postRepository, CommentRepository commentRepository) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
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
        return new CommentResponse(saved.getId(), saved.getPostId(), saved.getAuthorId(), saved.getBody());
    }
}
