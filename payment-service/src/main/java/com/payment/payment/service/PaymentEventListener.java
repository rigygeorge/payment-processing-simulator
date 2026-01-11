package com.payment.payment.service;

import com.payment.inventory.event.InventoryEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Payment Service Event Listener - Processes inventory events
 * 
 * Location: payment-service/src/main/java/com/payment/payment/service/PaymentEventListener.java
 */
@Service
@Slf4j
public class PaymentEventListener {

    private final PaymentService paymentService;

    public PaymentEventListener(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Listen to inventory-events topic
     * Process payment after inventory is successfully reserved
     */
    @KafkaListener(
        topics = "inventory-events",
        groupId = "payment-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleInventoryEvent(InventoryEvent event) {
        log.info("═══════════════════════════════════════════════════");
        log.info("💳 PAYMENT SERVICE - Inventory Event Received");
        log.info("═══════════════════════════════════════════════════");
        log.info("Event Type: {}", event.getEventType());
        log.info("Correlation ID: {}", event.getCorrelationId());
        log.info("Order ID: {}", event.getOrderId());
        log.info("Success: {}", event.isSuccess());
        log.info("Message: {}", event.getMessage());
        log.info("───────────────────────────────────────────────────");

        // Only process INVENTORY_RESERVED events
        if (!"INVENTORY_RESERVED".equals(event.getEventType())) {
            log.info("⏭️  Skipping event type: {}", event.getEventType());
            log.info("═══════════════════════════════════════════════════\n");
            return;
        }

        if (!event.isSuccess()) {
            log.warn("⚠️ Inventory reservation failed - Skipping payment");
            log.info("═══════════════════════════════════════════════════\n");
            return;
        }

        try {
            log.info("✅ Inventory successfully reserved - Processing payment...");
            
            // NOTE: In real implementation, we'd get amount and customerId from the order
            // For now, using placeholder values
            // TODO: Fetch order details from Order Service or include in event
            
            // Process payment
            // This will trigger fraud detection, idempotency check, and payment gateway simulation
            paymentService.processPayment(
                event.getOrderId(),
                java.math.BigDecimal.valueOf(100.00), // Placeholder - should come from order
                12345L, // Placeholder customer ID
                event.getCorrelationId()
            );

        } catch (Exception e) {
            log.error("❌ Error handling inventory event: {}", e.getMessage(), e);
        }

        log.info("═══════════════════════════════════════════════════\n");
    }
}