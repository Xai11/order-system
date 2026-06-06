package com.example.analyticsservice.controller;

import com.example.analyticsservice.config.AnalyticsStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/analytics")
public class AnalyticsController {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsController.class);

    private final AnalyticsStore store;

    public AnalyticsController(AnalyticsStore store) {
        this.store = store;
    }

    @GetMapping("/status-counts")
    public ResponseEntity<Map<String, Long>> getStatusCounts() {
        return ResponseEntity.ok(buildStatusCounts());
    }

    @GetMapping("/latency")
    public ResponseEntity<Map<String, Double>> getLatency() {
        return ResponseEntity.ok(buildLatency());
    }

    @GetMapping("/failures")
    public ResponseEntity<Map<String, Long>> getFailures() {
        return ResponseEntity.ok(new HashMap<>(store.failureReasons));
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary() {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("statusCounts", buildStatusCounts());
        summary.put("latency", buildLatency());
        summary.put("failureBreakdown", new HashMap<>(store.failureReasons));
        return ResponseEntity.ok(summary);
    }

    private Map<String, Long> buildStatusCounts() {
        Map<String, Long> counts = new LinkedHashMap<>();
        counts.put("CREATED", store.ordersCreated.get());
        counts.put("PAID", store.ordersPaid.get());
        counts.put("FAILED", store.ordersFailed.get());
        counts.put("SHIPPED", store.ordersShipped.get());
        return counts;
    }

    private Map<String, Double> buildLatency() {
        List<Long> latencies = new ArrayList<>();
        for (Map.Entry<String, Long> entry : store.orderPaidTimes.entrySet()) {
            Long createdAt = store.orderCreatedTimes.get(entry.getKey());
            if (createdAt != null) {
                latencies.add(entry.getValue() - createdAt);
            }
        }
        Map<String, Double> result = new LinkedHashMap<>();
        if (latencies.isEmpty()) {
            result.put("avgMs", 0.0);
            result.put("p95Ms", 0.0);
            result.put("sampleSize", 0.0);
            return result;
        }
        double avg = latencies.stream().mapToLong(Long::longValue).average().orElse(0.0);
        Collections.sort(latencies);
        int p95idx = (int) Math.ceil(latencies.size() * 0.95) - 1;
        double p95 = latencies.get(Math.max(0, p95idx));
        result.put("avgMs", avg);
        result.put("p95Ms", p95);
        result.put("sampleSize", (double) latencies.size());
        return result;
    }
}
