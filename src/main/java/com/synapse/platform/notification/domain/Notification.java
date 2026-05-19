package com.synapse.platform.notification.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String channel;

    private String status;

    protected Notification() {}

    public Notification(Long userId, String channel, String status) {
        this.userId = userId;
        this.channel = channel;
        this.status = status;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getChannel() { return channel; }
    public String getStatus() { return status; }
}
