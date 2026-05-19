package com.synapse.learning.card.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "cards")
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long ownerId;

    @Lob
    private String frontText;

    @Lob
    private String backText;

    protected Card() {}

    public Card(Long ownerId, String frontText, String backText) {
        this.ownerId = ownerId;
        this.frontText = frontText;
        this.backText = backText;
    }

    public Long getId() { return id; }
    public Long getOwnerId() { return ownerId; }
    public String getFrontText() { return frontText; }
    public String getBackText() { return backText; }
}
