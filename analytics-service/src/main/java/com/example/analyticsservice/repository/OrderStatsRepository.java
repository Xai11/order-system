package com.example.analyticsservice.repository;

import com.example.analyticsservice.model.OrderStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderStatsRepository extends JpaRepository<OrderStats, Long> {}
