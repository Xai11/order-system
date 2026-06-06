package com.example.orderservice.controller;

import com.example.orderservice.event.OrderCreatedEvent;
import com.example.orderservice.model.CreateOrderRequest;
import com.example.orderservice.model.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);
    private static final String TOPIC = "orders";

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ConcurrentHashMap<String, Order> store = new ConcurrentHashMap<>();

    public OrderController(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody CreateOrderRequest req) {
        String orderId = UUID.randomUUID().toString();
        Instant now = Instant.now();
        Order order = new Order(orderId, req.getUserId(), req.getAmount(), "CREATED", now);
        store.put(orderId, order);

        OrderCreatedEvent event = new OrderCreatedEvent(orderId, req.getUserId(), req.getAmount(), "CREATED", now);
        kafkaTemplate.send(TOPIC, orderId, event);

        log.info("[ORDER-SERVICE] [orderId={}] Event: OrderCreated status: CREATED", orderId);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrder(@PathVariable String orderId) {
        Order order = store.get(orderId);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(order);
    }

    @GetMapping
    public ResponseEntity<Collection<Order>> getAllOrders() {
        return ResponseEntity.ok(store.values());
    }
}
