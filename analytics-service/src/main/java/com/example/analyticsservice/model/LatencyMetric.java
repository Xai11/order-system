package com.example.analyticsservice.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "latency_metrics")
public class LatencyMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "metric_name")
    private String metricName;

    @Column(name = "avg_ms")
    private Double avgMs;

    @Column(name = "p95_ms")
    private Double p95Ms;

    @Column(name = "recorded_at")
    private LocalDateTime recordedAt;

    public LatencyMetric() {}

    public LatencyMetric(String metricName, Double avgMs, Double p95Ms, LocalDateTime recordedAt) {
        this.metricName = metricName;
        this.avgMs = avgMs;
        this.p95Ms = p95Ms;
        this.recordedAt = recordedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMetricName() { return metricName; }
    public void setMetricName(String metricName) { this.metricName = metricName; }

    public Double getAvgMs() { return avgMs; }
    public void setAvgMs(Double avgMs) { this.avgMs = avgMs; }

    public Double getP95Ms() { return p95Ms; }
    public void setP95Ms(Double p95Ms) { this.p95Ms = p95Ms; }

    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }
}
