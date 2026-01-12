package com.payment.shipping.service;

import com.payment.payment.event.PaymentEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Shipping Event Listener - Processes payment events
 * 
 * Location: shipping-service/src/main/java/com/payment/shipping/service/ShippingEventListener.java
 */
@Service
@Slf4j
public class ShippingEventListener {

    private final ShippingService shippingService;

    public ShippingEventListener(ShippingService shippingService) {
        this.shippingService = shippingService;
    }

    /**
     * Listen to payment-events topic
     * Create shipment after payment is successfully processed
     */
    @KafkaListener(
        topics = "payment-events",
        groupId = "shipping-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePaymentEvent(PaymentEvent event) {
        log.info("═══════════════════════════════════════════════════");
        log.info("🚢 SHIPPING SERVICE - Payment Event Received");
        log.info("═══════════════════════════════════════════════════");
        log.info("Event Type: {}", event.getEventType());
        log.info("Correlation ID: {}", event.getCorrelationId());
        log.info("Order ID: {}", event.getOrderId());
        log.info("Payment ID: {}", event.getPaymentId());
        log.info("Amount: ${}", event.getAmount());
        log.info("Success: {}", event.isSuccess());
        log.info("───────────────────────────────────────────────────");

        // Only process PAYMENT_PROCESSED events
        if (!"PAYMENT_PROCESSED".equals(event.getEventType())) {
            log.info("⏭️  Skipping event type: {}", event.getEventType());
            log.info("═══════════════════════════════════════════════════\n");
            return;
        }

        if (!event.isSuccess()) {
            log.warn("⚠️ Payment not successful - Skipping shipment creation");
            log.info("═══════════════════════════════════════════════════\n");
            return;
        }

        try {
            log.info("✅ Payment successfully processed - Creating shipment...");
            
            // Create shipment
            shippingService.createShipment(event.getOrderId(), event.getCorrelationId());

        } catch (Exception e) {
            log.error("❌ Error handling payment event: {}", e.getMessage(), e);
        }

        log.info("═══════════════════════════════════════════════════\n");
    }
}