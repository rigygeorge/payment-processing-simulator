package com.payment.inventory.service;

import com.payment.order.event.OrderEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Inventory Service - Listens to ORDER_CREATED events
 */
@Service
@Slf4j
public class InventoryEventListener {

    /**
     * Listen to order-events topic and process ORDER_CREATED events
     * This simulates inventory reservation logic
     */
    @KafkaListener(
        topics = "order-events",
        groupId = "inventory-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleOrderCreated(OrderEvent orderEvent) {
        log.info("=====================================");
        log.info("📦 INVENTORY SERVICE - Event Received");
        log.info("=====================================");
        log.info("Event Type: {}", orderEvent.getEventType());
        log.info("Correlation ID: {}", orderEvent.getCorrelationId());
        log.info("Order ID: {}", orderEvent.getOrderId());
        log.info("Customer ID: {}", orderEvent.getCustomerId());
        log.info("Total Amount: ${}", orderEvent.getTotalAmount());
        log.info("Order Status: {}", orderEvent.getOrderStatus());
        log.info("Number of Items: {}", orderEvent.getItems().size());
        
        // Log each item
        orderEvent.getItems().forEach(item -> {
            log.info("  - Product ID: {} | Quantity: {} | Price: ${}", 
                item.getProductId(), 
                item.getQuantity(), 
                item.getPrice());
        });
        
        log.info("✓ Order event processed by Inventory Service");
        log.info("=====================================\n");
        
        
    }
}