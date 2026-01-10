package com.payment.notification.service;

import com.payment.order.event.OrderEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Notification Service - Listens to ALL events from all topics
 * Place this in NOTIFICATION SERVICE
 */
@Service
@Slf4j
public class NotificationEventListener {

    /**
     * Listen to order-events topic
     */
    @KafkaListener(
        topics = "order-events",
        groupId = "notification-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleOrderEvent(OrderEvent orderEvent) {
        log.info("=====================================");
        log.info("📧 NOTIFICATION SERVICE - Order Event");
        log.info("=====================================");
        log.info("Event Type: {}", orderEvent.getEventType());
        log.info("Correlation ID: {}", orderEvent.getCorrelationId());
        log.info("Order ID: {}", orderEvent.getOrderId());
        
        // Simulate sending notification based on event type
        String notification = formatNotification(orderEvent);
        log.info("📨 Sending Notification: {}", notification);
        log.info("=====================================\n");
        
        // TODO: In next phase, we'll store these notifications in database
    }

    /**
     * Listen to inventory-events topic
     */
    @KafkaListener(
        topics = "inventory-events",
        groupId = "notification-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleInventoryEvent(Object inventoryEvent) {
        log.info("📧 NOTIFICATION SERVICE - Inventory Event Received");
        log.info("Event: {}", inventoryEvent);
        // TODO: Add proper handling once InventoryEvent is implemented
    }

    /**
     * Listen to payment-events topic
     */
    @KafkaListener(
        topics = "payment-events",
        groupId = "notification-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePaymentEvent(Object paymentEvent) {
        log.info("📧 NOTIFICATION SERVICE - Payment Event Received");
        log.info("Event: {}", paymentEvent);
        // TODO: Add proper handling once PaymentEvent is implemented
    }

    /**
     * Listen to shipping-events topic
     */
    @KafkaListener(
        topics = "shipping-events",
        groupId = "notification-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleShippingEvent(Object shippingEvent) {
        log.info("📧 NOTIFICATION SERVICE - Shipping Event Received");
        log.info("Event: {}", shippingEvent);
        // TODO: Add proper handling once ShipmentEvent is implemented
    }

    /**
     * Format notification message based on event type
     */
    private String formatNotification(OrderEvent event) {
        return switch (event.getEventType()) {
            case "ORDER_CREATED" -> 
                String.format("Your order #%d has been received and is being processed.", event.getOrderId());
            case "ORDER_COMPLETED" -> 
                String.format("Your order #%d has been completed successfully!", event.getOrderId());
            case "ORDER_FAILED" -> 
                String.format("Unfortunately, your order #%d could not be processed.", event.getOrderId());
            default -> 
                String.format("Order #%d status update: %s", event.getOrderId(), event.getEventType());
        };
    }
}