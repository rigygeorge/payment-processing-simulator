package com.payment.inventory.service;

import com.payment.inventory.event.InventoryEvent;
import com.payment.inventory.model.Product;
import com.payment.inventory.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Inventory Service - Business logic for inventory management
 * 
 * Location: inventory-service/src/main/java/com/payment/inventory/service/InventoryService.java
 */
@Service
@Slf4j
public class InventoryService {

    private final ProductRepository productRepository;
    private final EventPublisher eventPublisher;

    public InventoryService(ProductRepository productRepository, EventPublisher eventPublisher) {
        this.productRepository = productRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Reserve inventory for an order
     * Returns InventoryEvent indicating success or failure
     */
    @Transactional
    public InventoryEvent reserveInventory(Long orderId, Long productId, Integer quantity, String correlationId) {
        log.info("Attempting to reserve {} units of product {} for order {}", 
            quantity, productId, orderId);

        try {
            // Find product
            Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

            log.info("Product found: {} (SKU: {})", product.getName(), product.getSku());
            log.info("Available: {}, Reserved: {}", product.getAvailableQuantity(), product.getReservedQuantity());

            // Check and reserve inventory
            boolean reserved = product.reserve(quantity);

            if (reserved) {
                // Save updated product
                productRepository.save(product);

                log.info("✅ Inventory reserved successfully");
                log.info("New Available: {}, New Reserved: {}", 
                    product.getAvailableQuantity(), product.getReservedQuantity());

                // Create success event
                InventoryEvent event = InventoryEvent.reserved(correlationId, orderId, productId, quantity);
                
                // Publish event
                eventPublisher.publishEvent("inventory-events", correlationId, event);
                
                return event;
            } else {
                // Insufficient stock
                String message = String.format("Insufficient stock for product %s. Requested: %d, Available: %d",
                    product.getName(), quantity, product.getAvailableQuantity());
                
                log.warn("❌ Inventory reservation failed: {}", message);

                // Create failure event
                InventoryEvent event = InventoryEvent.failed(correlationId, orderId, productId, quantity, message);
                
                // Publish event
                eventPublisher.publishEvent("inventory-events", correlationId, event);
                
                return event;
            }

        } catch (Exception e) {
            log.error("❌ Error reserving inventory: {}", e.getMessage(), e);
            
            String message = "Error processing inventory reservation: " + e.getMessage();
            InventoryEvent event = InventoryEvent.failed(correlationId, orderId, productId, quantity, message);
            
            eventPublisher.publishEvent("inventory-events", correlationId, event);
            
            return event;
        }
    }

    /**
     * Unreserve inventory (compensation for payment failure)
     */
    @Transactional
    public InventoryEvent unreserveInventory(Long orderId, Long productId, Integer quantity, String correlationId) {
        log.info("🔄 COMPENSATION: Unreserving {} units of product {} for order {}", 
            quantity, productId, orderId);

        try {
            // Find product
            Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

            log.info("Product found: {} (SKU: {})", product.getName(), product.getSku());
            log.info("Before unreserve - Available: {}, Reserved: {}", 
                product.getAvailableQuantity(), product.getReservedQuantity());

            // Unreserve inventory
            product.unreserve(quantity);

            // Save updated product
            productRepository.save(product);

            log.info("✅ Inventory unreserved successfully");
            log.info("After unreserve - Available: {}, Reserved: {}", 
                product.getAvailableQuantity(), product.getReservedQuantity());

            // Create unreserved event
            InventoryEvent event = InventoryEvent.unreserved(correlationId, orderId, productId, quantity);
            
            // Publish event
            eventPublisher.publishEvent("inventory-events", correlationId, event);
            
            return event;

        } catch (Exception e) {
            log.error("❌ Error unreserving inventory: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to unreserve inventory", e);
        }
    }

    /**
     * Get all products
     */
    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    /**
     * Get product by ID
     */
    @Transactional(readOnly = true)
    public Product getProductById(Long id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found: " + id));
    }

    /**
     * Get product by SKU
     */
    @Transactional(readOnly = true)
    public Product getProductBySku(String sku) {
        return productRepository.findBySku(sku)
            .orElseThrow(() -> new RuntimeException("Product not found with SKU: " + sku));
    }
}