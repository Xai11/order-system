package com.example.analyticsservice.repository;

import com.example.analyticsservice.model.LatencyMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LatencyMetricRepository extends JpaRepository<LatencyMetric, Long> {}
