package com.payment.payment.service;

import com.payment.order.event.OrderEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Payment Service - Listens to ORDER_CREATED events
 * Place this in PAYMENT SERVICE
 */
@Service
@Slf4j
public class PaymentEventListener {

    /**
     * Listen to order-events topic and process ORDER_CREATED events
     * This simulates payment processing logic
     */
    @KafkaListener(
        topics = "order-events",
        groupId = "payment-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleOrderCreated(OrderEvent orderEvent) {
        log.info("=====================================");
        log.info("💳 PAYMENT SERVICE - Event Received");
        log.info("=====================================");
        log.info("Event Type: {}", orderEvent.getEventType());
        log.info("Correlation ID: {}", orderEvent.getCorrelationId());
        log.info("Order ID: {}", orderEvent.getOrderId());
        log.info("Amount to Process: ${}", orderEvent.getTotalAmount());
        log.info("✓ Payment event received - Ready to process payment");
        log.info("=====================================\n");
        
        // TODO: In next phase, we'll add actual payment processing logic here
        // Including fraud detection and Redis idempotency checks
    }
}