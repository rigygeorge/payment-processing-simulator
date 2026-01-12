package com.payment.shipping.controller;

import com.payment.shipping.model.Shipment;
import com.payment.shipping.service.ShippingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Shipping REST Controller - API endpoints
 * 
 * Location: shipping-service/src/main/java/com/payment/shipping/controller/ShippingController.java
 */
@RestController
@RequestMapping("/api/shipping")
@Slf4j
public class ShippingController {

    private final ShippingService shippingService;

    public ShippingController(ShippingService shippingService) {
        this.shippingService = shippingService;
    }

    /**
     * Track shipment by tracking number
     * GET /api/shipping/track/{trackingNumber}
     */
    @GetMapping("/track/{trackingNumber}")
    public ResponseEntity<Shipment> trackShipment(@PathVariable String trackingNumber) {
        log.info("📍 API: Track shipment request for tracking number: {}", trackingNumber);
        Shipment shipment = shippingService.getShipmentByTrackingNumber(trackingNumber);
        return ResponseEntity.ok(shipment);
    }

    /**
     * Get shipment by order ID
     * GET /api/shipping/order/{orderId}
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<Shipment> getShipmentByOrderId(@PathVariable Long orderId) {
        log.info("📍 API: Get shipment request for order: {}", orderId);
        Shipment shipment = shippingService.getShipmentByOrderId(orderId);
        return ResponseEntity.ok(shipment);
    }

    /**
     * Get all in-progress shipments
     * GET /api/shipping/in-progress
     */
    @GetMapping("/in-progress")
    public ResponseEntity<List<Shipment>> getInProgressShipments() {
        log.info("📍 API: Get in-progress shipments request");
        List<Shipment> shipments = shippingService.getInProgressShipments();
        log.info("✅ API: Returning {} in-progress shipments", shipments.size());
        return ResponseEntity.ok(shipments);
    }

    /**
     * Health check endpoint
     * GET /api/shipping/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "shipping-service");
        response.put("endpoints", List.of(
            "GET /api/shipping/track/{trackingNumber} - Track shipment",
            "GET /api/shipping/order/{orderId} - Get shipment by order ID",
            "GET /api/shipping/in-progress - Get all in-progress shipments"
        ));
        return ResponseEntity.ok(response);
    }

    /**
     * Exception handler
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleException(RuntimeException ex) {
        log.error("❌ API Error: {}", ex.getMessage());
        
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        error.put("status", "error");
        
        return ResponseEntity.badRequest().body(error);
    }
}