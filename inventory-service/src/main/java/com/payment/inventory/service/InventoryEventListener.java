package com.payment.inventory.service;

import com.payment.order.event.OrderEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Inventory Service Event Listener - Processes order events
 * 
 * Location: inventory-service/src/main/java/com/payment/inventory/service/InventoryEventListener.java
 */
@Service
@Slf4j
public class InventoryEventListener {

    private final InventoryService inventoryService;

    public InventoryEventListener(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    /**
     * Listen to order-events topic and process ORDER_CREATED events
     * For each item in the order, attempt to reserve inventory
     */
    @KafkaListener(
        topics = "order-events",
        groupId = "inventory-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleOrderCreated(OrderEvent orderEvent) {
        log.info("═══════════════════════════════════════════════════");
        log.info("📦 INVENTORY SERVICE - Order Event Received");
        log.info("═══════════════════════════════════════════════════");
        log.info("Event Type: {}", orderEvent.getEventType());
        log.info("Correlation ID: {}", orderEvent.getCorrelationId());
        log.info("Order ID: {}", orderEvent.getOrderId());
        log.info("Customer ID: {}", orderEvent.getCustomerId());
        log.info("Total Amount: ${}", orderEvent.getTotalAmount());
        log.info("Number of Items: {}", orderEvent.getItems().size());
        log.info("───────────────────────────────────────────────────");

        // Only process ORDER_CREATED events
        if (!"ORDER_CREATED".equals(orderEvent.getEventType())) {
            log.info("⏭️  Skipping event type: {}", orderEvent.getEventType());
            log.info("═══════════════════════════════════════════════════\n");
            return;
        }

        try {
            // Process each item in the order
            log.info("Processing {} order items...", orderEvent.getItems().size());
            
            boolean allItemsReserved = true;
            
            for (OrderEvent.OrderItemEvent item : orderEvent.getItems()) {
                log.info("───────────────────────────────────────────────────");
                log.info("Processing item: Product ID {} | Quantity {}", 
                    item.getProductId(), item.getQuantity());

                // Attempt to reserve inventory for this item
                var inventoryEvent = inventoryService.reserveInventory(
                    orderEvent.getOrderId(),
                    item.getProductId(),
                    item.getQuantity(),
                    orderEvent.getCorrelationId()
                );

                // Check if reservation was successful
                if (!inventoryEvent.isSuccess()) {
                    log.warn("❌ Failed to reserve inventory for product {}", item.getProductId());
                    log.warn("Reason: {}", inventoryEvent.getMessage());
                    allItemsReserved = false;
                    break; // Stop processing remaining items
                } else {
                    log.info("✅ Inventory reserved for product {}", item.getProductId());
                }
            }

            log.info("───────────────────────────────────────────────────");
            
            if (allItemsReserved) {
                log.info("🎉 All inventory items reserved successfully");
                log.info("📤 INVENTORY_RESERVED event published");
            } else {
                log.warn("⚠️ Some inventory items could not be reserved");
                log.warn("📤 INVENTORY_FAILED event published");
            }

        } catch (Exception e) {
            log.error("❌ Error processing order event: {}", e.getMessage(), e);
        }

        log.info("═══════════════════════════════════════════════════\n");
    }
}