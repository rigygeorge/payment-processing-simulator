package com.payment.order.controller;

import com.payment.order.dto.CreateOrderRequest;
import com.payment.order.dto.OrderResponse;
import com.payment.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Order REST Controller - API endpoints
 * 
 * Location: order-service/src/main/java/com/payment/order/controller/OrderController.java
 */
@RestController
@RequestMapping("/api/orders")
@Slf4j
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Create a new order
     * 
     * POST /api/orders
     * 
     * Request Body:
     * {
     *   "customerId": 67890,
     *   "items": [
     *     {"productId": 1, "quantity": 2, "price": 29.99},
     *     {"productId": 2, "quantity": 1, "price": 49.99}
     *   ]
     * }
     */
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        log.info("📝 API: Create order request received for customer: {}", request.getCustomerId());
        
        OrderResponse response = orderService.createOrder(request);
        
        log.info("✅ API: Order created successfully with ID: {}", response.getOrderId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get order by ID
     * 
     * GET /api/orders/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id) {
        log.info("📖 API: Get order request for ID: {}", id);
        
        OrderResponse response = orderService.getOrderById(id);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get order by correlation ID
     * 
     * GET /api/orders/correlation/{correlationId}
     */
    @GetMapping("/correlation/{correlationId}")
    public ResponseEntity<OrderResponse> getOrderByCorrelationId(@PathVariable String correlationId) {
        log.info("📖 API: Get order request for correlationId: {}", correlationId);
        
        OrderResponse response = orderService.getOrderByCorrelationId(correlationId);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get all orders for a customer
     * 
     * GET /api/orders/customer/{customerId}
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByCustomerId(@PathVariable Long customerId) {
        log.info("📖 API: Get orders request for customer: {}", customerId);
        
        List<OrderResponse> responses = orderService.getOrdersByCustomerId(customerId);
        
        log.info("✅ API: Found {} orders for customer: {}", responses.size(), customerId);
        return ResponseEntity.ok(responses);
    }

    /**
     * Health check endpoint for order controller
     * 
     * GET /api/orders/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("controller", "OrderController");
        response.put("endpoints", List.of(
            "POST /api/orders - Create order",
            "GET /api/orders/{id} - Get order by ID",
            "GET /api/orders/correlation/{correlationId} - Get order by correlation ID",
            "GET /api/orders/customer/{customerId} - Get customer orders"
        ));
        return ResponseEntity.ok(response);
    }

    /**
     * Exception handler for RuntimeException
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        log.error("❌ API Error: {}", ex.getMessage());
        
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        error.put("status", "error");
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}