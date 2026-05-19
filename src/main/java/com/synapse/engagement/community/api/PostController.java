package com.synapse.engagement.community.api;

import com.synapse.engagement.community.api.dto.request.CreateCommentRequest;
import com.synapse.engagement.community.api.dto.request.CreatePostRequest;
import com.synapse.engagement.community.api.dto.response.CommentResponse;
import com.synapse.engagement.community.api.dto.response.PostResponse;
import com.synapse.engagement.community.application.PostService;
import com.synapse.engagement.global.response.ApiResponse;
import jakarta.validation.Valid;
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
    public ApiResponse<PostResponse> createPost(@Valid @RequestBody CreatePostRequest request) {
        return ApiResponse.ok(postService.createPost(request));
    }

    @GetMapping
    public ApiResponse<List<PostResponse>> list() {
        return ApiResponse.ok(postService.findAll());
    }

    @PostMapping("/{postId}/comments")
    public ApiResponse<CommentResponse> createComment(@PathVariable Long postId,
                                                      @Valid @RequestBody CreateCommentRequest request) {
        return ApiResponse.ok(postService.createComment(postId, request));
    }
}
