package com.example.analyticsservice.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_stats")
public class OrderStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "status")
    private String status;

    @Column(name = "count")
    private Long count;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    public OrderStats() {}

    public OrderStats(String status, Long count, LocalDateTime lastUpdated) {
        this.status = status;
        this.count = count;
        this.lastUpdated = lastUpdated;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getCount() { return count; }
    public void setCount(Long count) { this.count = count; }

    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
}
