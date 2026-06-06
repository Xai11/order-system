package com.example.analyticsservice.config;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class AnalyticsStore {

    public final AtomicLong ordersCreated = new AtomicLong(0);
    public final AtomicLong ordersPaid = new AtomicLong(0);
    public final AtomicLong ordersFailed = new AtomicLong(0);
    public final AtomicLong ordersShipped = new AtomicLong(0);

    public final ConcurrentHashMap<String, Long> failureReasons = new ConcurrentHashMap<>();

    // orderId -> createdAt epoch millis
    public final ConcurrentHashMap<String, Long> orderCreatedTimes = new ConcurrentHashMap<>();

    // orderId -> paidAt epoch millis
    public final ConcurrentHashMap<String, Long> orderPaidTimes = new ConcurrentHashMap<>();

    public void recordOrderCreated(String orderId, Instant createdAt) {
        ordersCreated.incrementAndGet();
        orderCreatedTimes.put(orderId, createdAt.toEpochMilli());
    }

    public void recordPaymentPaid(String orderId, Instant paidAt) {
        ordersPaid.incrementAndGet();
        orderPaidTimes.put(orderId, paidAt.toEpochMilli());
    }

    public void recordPaymentFailed(String orderId, String reason) {
        ordersFailed.incrementAndGet();
        if (reason != null) {
            failureReasons.merge(reason, 1L, Long::sum);
        }
    }

    public void recordShipped(String orderId) {
        ordersShipped.incrementAndGet();
    }
}
