package com.example.paymentservice.config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/delay")
public class DelayController {

    private final DelayConfig delayConfig;

    public DelayController(DelayConfig delayConfig) {
        this.delayConfig = delayConfig;
    }

    @GetMapping
    public ResponseEntity<Map<String, Long>> getDelay() {
        return ResponseEntity.ok(Map.of("delayMs", delayConfig.get()));
    }

    @PostMapping
    public ResponseEntity<Map<String, Long>> setDelay(@RequestBody Map<String, Long> body) {
        long ms = body.getOrDefault("ms", 500L);
        delayConfig.set(ms);
        return ResponseEntity.ok(Map.of("delayMs", delayConfig.get()));
    }
}
