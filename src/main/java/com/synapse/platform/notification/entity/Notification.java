package com.synapse.platform.notification.entity;

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

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getChannel() { return channel; }
    public String getStatus() { return status; }
}
