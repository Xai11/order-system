package com.example.paymentservice.config;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class DelayConfig {

    private final AtomicLong delayMs = new AtomicLong(500);

    public long get() {
        return delayMs.get();
    }

    public void set(long ms) {
        delayMs.set(Math.max(0, ms));
    }
}
