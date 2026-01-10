package com.payment.order.controller;

import com.payment.order.event.OrderEvent;
import com.payment.order.service.EventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Test controller to manually publish events
 */
@RestController
@RequestMapping("/api/test")
@Slf4j
public class EventTestController {

    private final EventPublisher eventPublisher;

    public EventTestController(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /**
     * Test endpoint to publish a sample ORDER_CREATED event
     * POST http://localhost:8081/api/test/publish-order-event
     */
    @PostMapping("/publish-order-event")
    public ResponseEntity<Map<String, Object>> publishOrderEvent() {
        log.info("Test endpoint called: Publishing ORDER_CREATED event");
        
        // Generate correlation ID for tracking
        String correlationId = UUID.randomUUID().toString();
        
        // Create sample order items
        List<OrderEvent.OrderItemEvent> items = new ArrayList<>();
        items.add(new OrderEvent.OrderItemEvent(1L, 2, new BigDecimal("29.99")));
        items.add(new OrderEvent.OrderItemEvent(2L, 1, new BigDecimal("49.99")));
        
        // Create ORDER_CREATED event
        OrderEvent orderEvent = new OrderEvent(
            correlationId,
            "ORDER_CREATED",
            12345L, // orderId
            67890L, // customerId
            new BigDecimal("109.97"), // totalAmount
            "PENDING",
            items
        );
        
        // Publish to Kafka
        eventPublisher.publishEvent("order-events", correlationId, orderEvent);
        
        // Response
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "ORDER_CREATED event published");
        response.put("correlationId", correlationId);
        response.put("orderId", 12345L);
        response.put("topic", "order-events");
        
        log.info("✓ Test event published with correlationId: {}", correlationId);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Test endpoint to publish multiple events
     * POST http://localhost:8081/api/test/publish-multiple-events
     */
    @PostMapping("/publish-multiple-events")
    public ResponseEntity<Map<String, Object>> publishMultipleEvents() {
        log.info("Publishing multiple test events...");
        
        String correlationId = UUID.randomUUID().toString();
        
        // Publish 5 sample events
        for (int i = 1; i <= 5; i++) {
            List<OrderEvent.OrderItemEvent> items = new ArrayList<>();
            items.add(new OrderEvent.OrderItemEvent((long) i, 1, new BigDecimal("19.99")));
            
            OrderEvent orderEvent = new OrderEvent(
                correlationId + "-" + i,
                "ORDER_CREATED",
                (long) (12345 + i),
                67890L,
                new BigDecimal("19.99"),
                "PENDING",
                items
            );
            
            eventPublisher.publishEvent("order-events", correlationId + "-" + i, orderEvent);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Published 5 ORDER_CREATED events");
        response.put("baseCorrelationId", correlationId);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Health check for test controller
     * GET http://localhost:8081/api/test/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> testHealth() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("controller", "EventTestController");
        return ResponseEntity.ok(response);
    }
}