package com.example.orderservice.event;

import java.time.Instant;

public class OrderCreatedEvent {
    private String orderId;
    private String userId;
    private double amount;
    private String status;
    private Instant timestamp;

    public OrderCreatedEvent() {}

    public OrderCreatedEvent(String orderId, String userId, double amount, String status, Instant timestamp) {
        this.orderId = orderId;
        this.userId = userId;
        this.amount = amount;
        this.status = status;
        this.timestamp = timestamp;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
