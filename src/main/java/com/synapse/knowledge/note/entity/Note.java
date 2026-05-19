package com.synapse.knowledge.note.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "notes")
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Lob
    private String body;

    private Long ownerId;

    protected Note() {}

    public Note(String title, String body, Long ownerId) {
        this.title = title;
        this.body = body;
        this.ownerId = ownerId;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getBody() { return body; }
    public Long getOwnerId() { return ownerId; }
}
