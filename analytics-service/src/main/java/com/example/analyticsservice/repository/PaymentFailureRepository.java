package com.example.analyticsservice.repository;

import com.example.analyticsservice.model.PaymentFailure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentFailureRepository extends JpaRepository<PaymentFailure, Long> {}
