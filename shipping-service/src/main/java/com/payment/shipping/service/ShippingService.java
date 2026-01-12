package com.payment.shipping.service;

import com.payment.shipping.event.ShipmentEvent;
import com.payment.shipping.model.Shipment;
import com.payment.shipping.model.ShipmentStatus;
import com.payment.shipping.repository.ShipmentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Shipping Service - Business logic for shipment management
 * 
 * Location: shipping-service/src/main/java/com/payment/shipping/service/ShippingService.java
 */
@Service
@Slf4j
public class ShippingService {

    private final ShipmentRepository shipmentRepository;
    private final EventPublisher eventPublisher;
    private final Random random = new Random();

    public ShippingService(ShipmentRepository shipmentRepository, EventPublisher eventPublisher) {
        this.shipmentRepository = shipmentRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Create shipment for an order
     */
    @Transactional
    public ShipmentEvent createShipment(Long orderId, String correlationId) {
        log.info("═══════════════════════════════════════════════════");
        log.info("📦 Creating shipment for order: {}", orderId);
        log.info("Correlation ID: {}", correlationId);
        log.info("═══════════════════════════════════════════════════");

        try {
            // Check if shipment already exists
            if (shipmentRepository.existsByOrderId(orderId)) {
                log.warn("⚠️ Shipment already exists for order {}", orderId);
                Shipment existing = shipmentRepository.findByOrderId(orderId)
                    .orElseThrow(() -> new RuntimeException("Shipment not found"));
                return buildShipmentEvent(existing, "SHIPMENT_CREATED");
            }

            // Generate tracking number
            String trackingNumber = generateTrackingNumber();
            
            // Select random carrier
            String carrier = selectCarrier();
            
            // Calculate estimated delivery (3-5 business days)
            LocalDateTime estimatedDelivery = calculateEstimatedDelivery();

            // Create shipment
            Shipment shipment = Shipment.builder()
                .orderId(orderId)
                .correlationId(correlationId)
                .trackingNumber(trackingNumber)
                .status(ShipmentStatus.CREATED)
                .carrier(carrier)
                .shippingAddress("123 Main St, City, State 12345") // Simulated
                .estimatedDelivery(estimatedDelivery)
                .build();

            // Save to database
            Shipment savedShipment = shipmentRepository.save(shipment);

            log.info("✅ Shipment created successfully");
            log.info("Shipment ID: {}", savedShipment.getId());
            log.info("Tracking Number: {}", trackingNumber);
            log.info("Carrier: {}", carrier);
            log.info("Estimated Delivery: {}", estimatedDelivery);

            // Publish SHIPMENT_CREATED event
            ShipmentEvent event = ShipmentEvent.created(
                correlationId,
                orderId,
                savedShipment.getId(),
                trackingNumber,
                carrier,
                estimatedDelivery.toString()
            );

            eventPublisher.publishEvent("shipping-events", correlationId, event);
            log.info("📤 SHIPMENT_CREATED event published");

            return event;

        } catch (Exception e) {
            log.error("❌ Error creating shipment: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create shipment", e);
        } finally {
            log.info("═══════════════════════════════════════════════════\n");
        }
    }

    /**
     * Update shipment status (called by scheduled job)
     */
    @Transactional
    public void updateShipmentStatus(Long shipmentId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
            .orElseThrow(() -> new RuntimeException("Shipment not found: " + shipmentId));

        if (shipment.isFinalState()) {
            log.debug("Shipment {} already in final state: {}", shipmentId, shipment.getStatus());
            return;
        }

        // Progress to next status
        ShipmentStatus oldStatus = shipment.getStatus();
        shipment.progressStatus();
        
        shipmentRepository.save(shipment);

        log.info("📦 Shipment {} status updated: {} → {}", 
            shipmentId, oldStatus, shipment.getStatus());

        // Publish SHIPMENT_UPDATED event
        ShipmentEvent event = ShipmentEvent.updated(
            shipment.getCorrelationId(),
            shipment.getOrderId(),
            shipment.getId(),
            shipment.getTrackingNumber(),
            shipment.getStatus().name()
        );

        eventPublisher.publishEvent("shipping-events", shipment.getCorrelationId(), event);
        log.info("📤 SHIPMENT_UPDATED event published (status: {})", shipment.getStatus());
    }

    /**
     * Get all shipments that are in progress
     */
    @Transactional(readOnly = true)
    public List<Shipment> getInProgressShipments() {
        return shipmentRepository.findInProgressShipments();
    }

    /**
     * Get shipment by tracking number
     */
    @Transactional(readOnly = true)
    public Shipment getShipmentByTrackingNumber(String trackingNumber) {
        return shipmentRepository.findByTrackingNumber(trackingNumber)
            .orElseThrow(() -> new RuntimeException("Shipment not found: " + trackingNumber));
    }

    /**
     * Get shipment by order ID
     */
    @Transactional(readOnly = true)
    public Shipment getShipmentByOrderId(Long orderId) {
        return shipmentRepository.findByOrderId(orderId)
            .orElseThrow(() -> new RuntimeException("Shipment not found for order: " + orderId));
    }

    /**
     * Generate unique tracking number
     */
    private String generateTrackingNumber() {
        return "TRK-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Select random carrier
     */
    private String selectCarrier() {
        String[] carriers = {"FedEx", "UPS", "DHL", "USPS"};
        return carriers[random.nextInt(carriers.length)];
    }

    /**
     * Calculate estimated delivery date (3-5 business days from now)
     */
    private LocalDateTime calculateEstimatedDelivery() {
        int daysToAdd = 3 + random.nextInt(3); // 3 to 5 days
        return LocalDateTime.now().plusDays(daysToAdd);
    }

    /**
     * Build ShipmentEvent from Shipment entity
     */
    private ShipmentEvent buildShipmentEvent(Shipment shipment, String eventType) {
        return new ShipmentEvent(
            shipment.getCorrelationId(),
            eventType,
            shipment.getOrderId(),
            shipment.getId(),
            shipment.getTrackingNumber(),
            shipment.getStatus().name(),
            shipment.getCarrier(),
            shipment.getEstimatedDelivery() != null ? shipment.getEstimatedDelivery().toString() : null
        );
    }
}