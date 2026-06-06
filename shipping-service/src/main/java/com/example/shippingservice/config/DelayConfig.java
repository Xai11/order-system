package com.example.shippingservice.config;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class DelayConfig {

    private final AtomicLong delayMs = new AtomicLong(200);

    public long get() {
        return delayMs.get();
    }

    public void set(long ms) {
        delayMs.set(Math.max(0, ms));
    }
}
