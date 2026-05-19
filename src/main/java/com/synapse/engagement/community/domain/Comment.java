package com.synapse.engagement.community.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long postId;
    private Long authorId;

    @Lob
    private String body;

    protected Comment() {}

    public Comment(Long postId, Long authorId, String body) {
        this.postId = postId;
        this.authorId = authorId;
        this.body = body;
    }

    public Long getId() { return id; }
    public Long getPostId() { return postId; }
    public Long getAuthorId() { return authorId; }
    public String getBody() { return body; }
}
