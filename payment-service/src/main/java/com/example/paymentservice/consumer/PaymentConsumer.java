package com.example.paymentservice.consumer;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.UUID;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import com.example.paymentservice.config.DelayConfig;
import com.example.paymentservice.event.OrderCreatedEvent;
import com.example.paymentservice.event.PaymentResultEvent;

@Component
public class PaymentConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentConsumer.class);
    private static final String[] FAILURE_REASONS = {"INSUFFICIENT_FUNDS", "TIMEOUT", "CARD_DECLINED"};
    private static final String PAYMENTS_TOPIC = "payments";

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final DelayConfig delayConfig;
    private final SecureRandom random = new SecureRandom();

    public PaymentConsumer(KafkaTemplate<String, Object> kafkaTemplate, DelayConfig delayConfig) {
        this.kafkaTemplate = kafkaTemplate;
        this.delayConfig = delayConfig;
    }

    @KafkaListener(topics = "orders", groupId = "payment-group",
                   containerFactory = "kafkaListenerContainerFactory")
    public void processOrder(ConsumerRecord<String, OrderCreatedEvent> record, Acknowledgment ack) {
        OrderCreatedEvent event = record.value();
        if (event == null) {
            log.warn("[PAYMENT-SERVICE] Received null event, skipping");
            ack.acknowledge();
            return;
        }
        String orderId = event.getOrderId();
        try {
            long delay = delayConfig.get();
            log.info("[PAYMENT-SERVICE] [orderId={}] Processing with delay={}ms", orderId, delay);
            Thread.sleep(delay);
            PaymentResultEvent result;
            if (random.nextDouble() < 0.8) {
                result = new PaymentResultEvent(
                        orderId,
                        UUID.randomUUID().toString(),
                        "PAID",
                        null,
                        Instant.now()
                );
                log.info("[PAYMENT-SERVICE] [orderId={}] Event: PaymentProcessed status: PAID", orderId);
            } else {
                String reason = FAILURE_REASONS[random.nextInt(FAILURE_REASONS.length)];
                result = new PaymentResultEvent(
                        orderId,
                        UUID.randomUUID().toString(),
                        "FAILED",
                        reason,
                        Instant.now()
                );
                log.info("[PAYMENT-SERVICE] [orderId={}] Event: PaymentFailed status: FAILED reason: {}", orderId, reason);
            }
            kafkaTemplate.send(PAYMENTS_TOPIC, orderId, result);
            ack.acknowledge();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[PAYMENT-SERVICE] [orderId={}] Interrupted during processing", orderId);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("[PAYMENT-SERVICE] [orderId={}] Error processing payment: {}", orderId, e.getMessage(), e);
            ack.acknowledge();
        }
    }
}
