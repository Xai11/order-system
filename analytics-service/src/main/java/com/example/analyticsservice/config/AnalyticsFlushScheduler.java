package com.example.analyticsservice.config;

import com.example.analyticsservice.model.LatencyMetric;
import com.example.analyticsservice.model.OrderStats;
import com.example.analyticsservice.model.PaymentFailure;
import com.example.analyticsservice.repository.LatencyMetricRepository;
import com.example.analyticsservice.repository.OrderStatsRepository;
import com.example.analyticsservice.repository.PaymentFailureRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class AnalyticsFlushScheduler {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsFlushScheduler.class);

    private final AnalyticsStore store;
    private final OrderStatsRepository orderStatsRepo;
    private final PaymentFailureRepository paymentFailureRepo;
    private final LatencyMetricRepository latencyMetricRepo;

    public AnalyticsFlushScheduler(AnalyticsStore store,
                                   OrderStatsRepository orderStatsRepo,
                                   PaymentFailureRepository paymentFailureRepo,
                                   LatencyMetricRepository latencyMetricRepo) {
        this.store = store;
        this.orderStatsRepo = orderStatsRepo;
        this.paymentFailureRepo = paymentFailureRepo;
        this.latencyMetricRepo = latencyMetricRepo;
    }

    @Scheduled(fixedDelay = 10000)
    public void flush() {
        LocalDateTime now = LocalDateTime.now();
        try {
            flushOrderStats(now);
            flushPaymentFailures(now);
            flushLatencyMetrics(now);
            log.info("[ANALYTICS-SERVICE] Flushed aggregates to PostgreSQL at {}", now);
        } catch (Exception e) {
            log.error("[ANALYTICS-SERVICE] Error flushing to PostgreSQL: {}", e.getMessage(), e);
        }
    }

    private void flushOrderStats(LocalDateTime now) {
        orderStatsRepo.save(new OrderStats("CREATED", store.ordersCreated.get(), now));
        orderStatsRepo.save(new OrderStats("PAID", store.ordersPaid.get(), now));
        orderStatsRepo.save(new OrderStats("FAILED", store.ordersFailed.get(), now));
        orderStatsRepo.save(new OrderStats("SHIPPED", store.ordersShipped.get(), now));
    }

    private void flushPaymentFailures(LocalDateTime now) {
        LocalDateTime windowStart = now.minusSeconds(10);
        for (Map.Entry<String, Long> entry : store.failureReasons.entrySet()) {
            paymentFailureRepo.save(new PaymentFailure(entry.getKey(), entry.getValue(), windowStart, now));
        }
    }

    private void flushLatencyMetrics(LocalDateTime now) {
        List<Long> latencies = computeOrderToPaymentLatencies();
        if (!latencies.isEmpty()) {
            double avg = latencies.stream().mapToLong(Long::longValue).average().orElse(0.0);
            Collections.sort(latencies);
            int p95idx = (int) Math.ceil(latencies.size() * 0.95) - 1;
            double p95 = latencies.get(Math.max(0, p95idx));
            latencyMetricRepo.save(new LatencyMetric("order_to_payment", avg, p95, now));
        }
    }

    private List<Long> computeOrderToPaymentLatencies() {
        List<Long> result = new ArrayList<>();
        for (Map.Entry<String, Long> entry : store.orderPaidTimes.entrySet()) {
            String orderId = entry.getKey();
            Long createdAt = store.orderCreatedTimes.get(orderId);
            if (createdAt != null) {
                result.add(entry.getValue() - createdAt);
            }
        }
        return result;
    }
}
