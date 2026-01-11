package com.payment.order.service;

import com.payment.inventory.event.InventoryEvent;
import com.payment.payment.event.PaymentEvent;
import com.payment.shipping.event.ShipmentEvent;
import com.payment.order.model.OrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Saga Event Listener - Orchestrates the order flow
 * Listens to events from Inventory, Payment, and Shipping services
 * Updates order status based on event outcomes
 * 
 * Location: order-service/src/main/java/com/payment/order/service/SagaEventListener.java
 */
@Service
@Slf4j
public class SagaEventListener {

    private final OrderService orderService;

    public SagaEventListener(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Listen to inventory-events topic
     * Handles INVENTORY_RESERVED and INVENTORY_FAILED events
     */
    @KafkaListener(
        topics = "inventory-events",
        groupId = "order-service-saga-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleInventoryEvent(InventoryEvent event) {
        log.info("═══════════════════════════════════════════════════");
        log.info("🎭 SAGA: Inventory Event Received");
        log.info("═══════════════════════════════════════════════════");
        log.info("Event Type: {}", event.getEventType());
        log.info("Correlation ID: {}", event.getCorrelationId());
        log.info("Order ID: {}", event.getOrderId());
        log.info("Success: {}", event.isSuccess());
        log.info("Message: {}", event.getMessage());
        log.info("───────────────────────────────────────────────────");

        try {
            switch (event.getEventType()) {
                case "INVENTORY_RESERVED" -> {
                    log.info("✅ Inventory reserved successfully");
                    orderService.updateOrderStatus(
                        event.getCorrelationId(),
                        OrderStatus.INVENTORY_RESERVED,
                        null
                    );
                    log.info("📊 Order status updated to INVENTORY_RESERVED");
                    log.info("⏭️  Next: Waiting for payment processing...");
                }
                case "INVENTORY_FAILED" -> {
                    log.warn("❌ Inventory reservation failed: {}", event.getMessage());
                    orderService.updateOrderStatus(
                        event.getCorrelationId(),
                        OrderStatus.FAILED,
                        "Inventory reservation failed: " + event.getMessage()
                    );
                    log.info("📊 Order status updated to FAILED");
                    log.info("🛑 Order processing stopped - insufficient inventory");
                }
                default -> log.warn("⚠️ Unknown inventory event type: {}", event.getEventType());
            }
        } catch (Exception e) {
            log.error("❌ Error handling inventory event: {}", e.getMessage(), e);
        }

        log.info("═══════════════════════════════════════════════════\n");
    }

    /**
     * Listen to payment-events topic
     * Handles PAYMENT_PROCESSED and PAYMENT_FAILED events
     */
    @KafkaListener(
        topics = "payment-events",
        groupId = "order-service-saga-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePaymentEvent(PaymentEvent event) {
        log.info("═══════════════════════════════════════════════════");
        log.info("🎭 SAGA: Payment Event Received");
        log.info("═══════════════════════════════════════════════════");
        log.info("Event Type: {}", event.getEventType());
        log.info("Correlation ID: {}", event.getCorrelationId());
        log.info("Order ID: {}", event.getOrderId());
        log.info("Payment ID: {}", event.getPaymentId());
        log.info("Amount: ${}", event.getAmount());
        log.info("Success: {}", event.isSuccess());
        log.info("Message: {}", event.getMessage());
        if (event.getRiskScore() != null) {
            log.info("Risk Score: {}", event.getRiskScore());
        }
        log.info("───────────────────────────────────────────────────");

        try {
            switch (event.getEventType()) {
                case "PAYMENT_PROCESSED" -> {
                    log.info("✅ Payment processed successfully");
                    orderService.updateOrderStatus(
                        event.getCorrelationId(),
                        OrderStatus.PAYMENT_PROCESSED,
                        null
                    );
                    log.info("📊 Order status updated to PAYMENT_PROCESSED");
                    log.info("⏭️  Next: Waiting for shipment creation...");
                }
                case "PAYMENT_FAILED" -> {
                    log.warn("❌ Payment processing failed: {}", event.getMessage());
                    // Mark order as COMPENSATING (will trigger inventory unreservation)
                    orderService.updateOrderStatus(
                        event.getCorrelationId(),
                        OrderStatus.COMPENSATING,
                        "Payment failed: " + event.getMessage()
                    );
                    log.info("📊 Order status updated to COMPENSATING");
                    log.info("🔄 Compensation: Will unreserve inventory");
                    
                    // TODO: Publish COMPENSATION_REQUIRED event for inventory service
                    // For now, this is handled manually or we'll implement it next
                    
                    // Finally mark as FAILED
                    orderService.updateOrderStatus(
                        event.getCorrelationId(),
                        OrderStatus.FAILED,
                        "Payment failed: " + event.getMessage()
                    );
                }
                default -> log.warn("⚠️ Unknown payment event type: {}", event.getEventType());
            }
        } catch (Exception e) {
            log.error("❌ Error handling payment event: {}", e.getMessage(), e);
        }

        log.info("═══════════════════════════════════════════════════\n");
    }

    /**
     * Listen to shipping-events topic
     * Handles SHIPMENT_CREATED event
     */
    @KafkaListener(
        topics = "shipping-events",
        groupId = "order-service-saga-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleShippingEvent(ShipmentEvent event) {
        log.info("═══════════════════════════════════════════════════");
        log.info("🎭 SAGA: Shipping Event Received");
        log.info("═══════════════════════════════════════════════════");
        log.info("Event Type: {}", event.getEventType());
        log.info("Correlation ID: {}", event.getCorrelationId());
        log.info("Order ID: {}", event.getOrderId());
        log.info("Shipment ID: {}", event.getShipmentId());
        log.info("Tracking Number: {}", event.getTrackingNumber());
        log.info("Status: {}", event.getShipmentStatus());
        log.info("───────────────────────────────────────────────────");

        try {
            switch (event.getEventType()) {
                case "SHIPMENT_CREATED" -> {
                    log.info("✅ Shipment created successfully");
                    orderService.updateOrderStatus(
                        event.getCorrelationId(),
                        OrderStatus.SHIPPED,
                        null
                    );
                    log.info("📊 Order status updated to SHIPPED");
                }
                case "SHIPMENT_UPDATED" -> {
                    log.info("📦 Shipment status updated: {}", event.getShipmentStatus());
                    
                    // If shipment is delivered, mark order as completed
                    if ("DELIVERED".equals(event.getShipmentStatus())) {
                        orderService.updateOrderStatus(
                            event.getCorrelationId(),
                            OrderStatus.COMPLETED,
                            null
                        );
                        log.info("🎉 Order status updated to COMPLETED");
                        log.info("✨ Order journey finished successfully!");
                    }
                }
                default -> log.warn("⚠️ Unknown shipping event type: {}", event.getEventType());
            }
        } catch (Exception e) {
            log.error("❌ Error handling shipping event: {}", e.getMessage(), e);
        }

        log.info("═══════════════════════════════════════════════════\n");
    }
}