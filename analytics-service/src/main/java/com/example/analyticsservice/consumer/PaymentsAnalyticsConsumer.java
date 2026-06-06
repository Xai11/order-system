package com.example.analyticsservice.consumer;

import com.example.analyticsservice.config.AnalyticsStore;
import com.example.analyticsservice.event.PaymentResultEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class PaymentsAnalyticsConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentsAnalyticsConsumer.class);

    private final AnalyticsStore store;

    public PaymentsAnalyticsConsumer(AnalyticsStore store) {
        this.store = store;
    }

    @KafkaListener(topics = "payments", groupId = "analytics-payments-group",
                   containerFactory = "paymentsKafkaListenerContainerFactory")
    public void consumePayment(ConsumerRecord<String, Object> record, Acknowledgment ack) {
        try {
            if (record.value() instanceof PaymentResultEvent event) {
                String orderId = event.getOrderId();
                if ("PAID".equals(event.getStatus())) {
                    store.recordPaymentPaid(orderId,
                            event.getProcessedAt() != null ? event.getProcessedAt() : java.time.Instant.now());
                    log.info("[ANALYTICS-SERVICE] [orderId={}] Event: PaymentProcessed status: PAID", orderId);
                } else {
                    store.recordPaymentFailed(orderId, event.getReason());
                    log.info("[ANALYTICS-SERVICE] [orderId={}] Event: PaymentFailed status: FAILED reason: {}",
                            orderId, event.getReason());
                }
            }
        } catch (Exception e) {
            log.error("[ANALYTICS-SERVICE] Error processing payment event: {}", e.getMessage(), e);
        } finally {
            ack.acknowledge();
        }
    }
}
