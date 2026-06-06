package com.example.shippingservice.consumer;

import com.example.shippingservice.config.DelayConfig;
import com.example.shippingservice.event.PaymentResultEvent;
import com.example.shippingservice.event.ShipmentCreatedEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.UUID;

@Component
public class ShippingConsumer {

    private static final Logger log = LoggerFactory.getLogger(ShippingConsumer.class);
    private static final String[] CARRIERS = {"DHL", "FedEx", "UPS", "CDEK"};
    private static final String SHIPMENTS_TOPIC = "shipments";

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final DelayConfig delayConfig;
    private final SecureRandom random = new SecureRandom();

    public ShippingConsumer(KafkaTemplate<String, Object> kafkaTemplate, DelayConfig delayConfig) {
        this.kafkaTemplate = kafkaTemplate;
        this.delayConfig = delayConfig;
    }

    @KafkaListener(topics = "payments", groupId = "shipping-group",
                   containerFactory = "kafkaListenerContainerFactory")
    public void processPayment(ConsumerRecord<String, PaymentResultEvent> record, Acknowledgment ack) {
        PaymentResultEvent event = record.value();
        if (event == null) {
            log.warn("[SHIPPING-SERVICE] Received null event, skipping");
            ack.acknowledge();
            return;
        }
        String orderId = event.getOrderId();
        try {
            if (!"PAID".equals(event.getStatus())) {
                log.info("[SHIPPING-SERVICE] [orderId={}] Skipping non-PAID status: {}", orderId, event.getStatus());
                ack.acknowledge();
                return;
            }
            long delay = delayConfig.get();
            log.info("[SHIPPING-SERVICE] [orderId={}] Processing with delay={}ms", orderId, delay);
            Thread.sleep(delay);
            String carrier = CARRIERS[random.nextInt(CARRIERS.length)];
            ShipmentCreatedEvent shipment = new ShipmentCreatedEvent(
                    orderId,
                    UUID.randomUUID().toString(),
                    carrier,
                    "SHIPPED",
                    Instant.now()
            );
            kafkaTemplate.send(SHIPMENTS_TOPIC, orderId, shipment);
            log.info("[SHIPPING-SERVICE] [orderId={}] Event: ShipmentCreated status: SHIPPED carrier: {}", orderId, carrier);
            ack.acknowledge();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[SHIPPING-SERVICE] [orderId={}] Interrupted during processing", orderId);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("[SHIPPING-SERVICE] [orderId={}] Error processing shipment: {}", orderId, e.getMessage(), e);
            ack.acknowledge();
        }
    }
}
