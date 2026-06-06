package com.example.analyticsservice.consumer;

import com.example.analyticsservice.config.AnalyticsStore;
import com.example.analyticsservice.event.OrderCreatedEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class OrdersAnalyticsConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrdersAnalyticsConsumer.class);

    private final AnalyticsStore store;

    public OrdersAnalyticsConsumer(AnalyticsStore store) {
        this.store = store;
    }

    @KafkaListener(topics = "orders", groupId = "analytics-orders-group",
                   containerFactory = "ordersKafkaListenerContainerFactory")
    public void consumeOrder(ConsumerRecord<String, Object> record, Acknowledgment ack) {
        try {
            if (record.value() instanceof OrderCreatedEvent event) {
                store.recordOrderCreated(event.getOrderId(),
                        event.getTimestamp() != null ? event.getTimestamp() : java.time.Instant.now());
                log.info("[ANALYTICS-SERVICE] [orderId={}] Event: OrderCreated status: CREATED", event.getOrderId());
            }
        } catch (Exception e) {
            log.error("[ANALYTICS-SERVICE] Error processing order event: {}", e.getMessage(), e);
        } finally {
            ack.acknowledge();
        }
    }
}
