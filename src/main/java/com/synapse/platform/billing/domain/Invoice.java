package com.synapse.platform.billing.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "invoices")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private BigDecimal amount;

    private String status;

    protected Invoice() {}

    public Invoice(Long userId, BigDecimal amount, String status) {
        this.userId = userId;
        this.amount = amount;
        this.status = status;
    }

    public void markPaid() {
        this.status = "PAID";
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public BigDecimal getAmount() { return amount; }
    public String getStatus() { return status; }
}
