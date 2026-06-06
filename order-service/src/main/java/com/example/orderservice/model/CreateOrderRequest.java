package com.example.orderservice.model;

public class CreateOrderRequest {
    private String userId;
    private double amount;

    public CreateOrderRequest() {}

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
}
