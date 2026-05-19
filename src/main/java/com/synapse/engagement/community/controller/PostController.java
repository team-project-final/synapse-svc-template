package com.synapse.engagement.community.controller;

import com.synapse.engagement.community.dto.request.CreateCommentRequest;
import com.synapse.engagement.community.dto.request.CreatePostRequest;
import com.synapse.engagement.community.dto.response.CommentResponse;
import com.synapse.engagement.community.dto.response.PostResponse;
import com.synapse.engagement.community.service.PostService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/community/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    public PostResponse createPost(@RequestBody CreatePostRequest request) {
        return postService.createPost(request);
    }

    @GetMapping
    public List<PostResponse> list() {
        return postService.findAll();
    }

    @PostMapping("/{postId}/comments")
    public CommentResponse createComment(@PathVariable Long postId, @RequestBody CreateCommentRequest request) {
        return postService.createComment(postId, request);
    }
}
