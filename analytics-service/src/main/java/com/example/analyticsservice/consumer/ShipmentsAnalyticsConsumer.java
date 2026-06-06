package com.example.analyticsservice.consumer;

import com.example.analyticsservice.config.AnalyticsStore;
import com.example.analyticsservice.event.ShipmentCreatedEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class ShipmentsAnalyticsConsumer {

    private static final Logger log = LoggerFactory.getLogger(ShipmentsAnalyticsConsumer.class);

    private final AnalyticsStore store;

    public ShipmentsAnalyticsConsumer(AnalyticsStore store) {
        this.store = store;
    }

    @KafkaListener(topics = "shipments", groupId = "analytics-shipments-group",
                   containerFactory = "shipmentsKafkaListenerContainerFactory")
    public void consumeShipment(ConsumerRecord<String, Object> record, Acknowledgment ack) {
        try {
            if (record.value() instanceof ShipmentCreatedEvent event) {
                store.recordShipped(event.getOrderId());
                log.info("[ANALYTICS-SERVICE] [orderId={}] Event: ShipmentCreated status: SHIPPED", event.getOrderId());
            }
        } catch (Exception e) {
            log.error("[ANALYTICS-SERVICE] Error processing shipment event: {}", e.getMessage(), e);
        } finally {
            ack.acknowledge();
        }
    }
}
