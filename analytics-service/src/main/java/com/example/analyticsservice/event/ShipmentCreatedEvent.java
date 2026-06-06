package com.example.analyticsservice.event;

import java.time.Instant;

public class ShipmentCreatedEvent {
    private String orderId;
    private String shipmentId;
    private String carrier;
    private String status;
    private Instant shippedAt;

    public ShipmentCreatedEvent() {}

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getShipmentId() { return shipmentId; }
    public void setShipmentId(String shipmentId) { this.shipmentId = shipmentId; }

    public String getCarrier() { return carrier; }
    public void setCarrier(String carrier) { this.carrier = carrier; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Instant getShippedAt() { return shippedAt; }
    public void setShippedAt(Instant shippedAt) { this.shippedAt = shippedAt; }
}
