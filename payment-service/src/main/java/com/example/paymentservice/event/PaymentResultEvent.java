package com.example.paymentservice.event;

import java.time.Instant;

public class PaymentResultEvent {
    private String orderId;
    private String paymentId;
    private String status;
    private String reason;
    private Instant processedAt;

    public PaymentResultEvent() {}

    public PaymentResultEvent(String orderId, String paymentId, String status, String reason, Instant processedAt) {
        this.orderId = orderId;
        this.paymentId = paymentId;
        this.status = status;
        this.reason = reason;
        this.processedAt = processedAt;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public Instant getProcessedAt() { return processedAt; }
    public void setProcessedAt(Instant processedAt) { this.processedAt = processedAt; }
}
