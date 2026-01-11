package com.payment.inventory.controller;

import com.payment.inventory.model.Product;
import com.payment.inventory.service.InventoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Inventory REST Controller - API endpoints
 * 
 * Location: inventory-service/src/main/java/com/payment/inventory/controller/InventoryController.java
 */
@RestController
@RequestMapping("/api/inventory")
@Slf4j
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    /**
     * Get all products
     * GET /api/inventory/products
     */
    @GetMapping("/products")
    public ResponseEntity<List<Product>> getAllProducts() {
        log.info("📖 API: Get all products request");
        List<Product> products = inventoryService.getAllProducts();
        log.info("✅ API: Returning {} products", products.size());
        return ResponseEntity.ok(products);
    }

    /**
     * Get product by ID
     * GET /api/inventory/products/{id}
     */
    @GetMapping("/products/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        log.info("📖 API: Get product request for ID: {}", id);
        Product product = inventoryService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    /**
     * Get product by SKU
     * GET /api/inventory/products/sku/{sku}
     */
    @GetMapping("/products/sku/{sku}")
    public ResponseEntity<Product> getProductBySku(@PathVariable String sku) {
        log.info("📖 API: Get product request for SKU: {}", sku);
        Product product = inventoryService.getProductBySku(sku);
        return ResponseEntity.ok(product);
    }

    /**
     * Check stock availability for a product
     * GET /api/inventory/products/{id}/availability?quantity=5
     */
    @GetMapping("/products/{id}/availability")
    public ResponseEntity<Map<String, Object>> checkAvailability(
            @PathVariable Long id,
            @RequestParam Integer quantity) {
        
        log.info("🔍 API: Check availability for product {} (quantity: {})", id, quantity);
        
        Product product = inventoryService.getProductById(id);
        boolean available = product.hasEnoughStock(quantity);
        
        Map<String, Object> response = new HashMap<>();
        response.put("productId", id);
        response.put("productName", product.getName());
        response.put("requestedQuantity", quantity);
        response.put("availableQuantity", product.getAvailableQuantity());
        response.put("reservedQuantity", product.getReservedQuantity());
        response.put("available", available);
        
        log.info("✅ API: Availability check complete - Available: {}", available);
        return ResponseEntity.ok(response);
    }

    /**
     * Health check endpoint
     * GET /api/inventory/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "inventory-service");
        response.put("endpoints", List.of(
            "GET /api/inventory/products - Get all products",
            "GET /api/inventory/products/{id} - Get product by ID",
            "GET /api/inventory/products/sku/{sku} - Get product by SKU",
            "GET /api/inventory/products/{id}/availability - Check stock availability"
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