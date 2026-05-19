package com.synapse.engagement.community.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long authorId;
    private String title;

    @Lob
    private String body;

    protected Post() {}

    public Post(Long authorId, String title, String body) {
        this.authorId = authorId;
        this.title = title;
        this.body = body;
    }

    public Long getId() { return id; }
    public Long getAuthorId() { return authorId; }
    public String getTitle() { return title; }
    public String getBody() { return body; }
}
