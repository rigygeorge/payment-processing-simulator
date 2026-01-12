package com.payment.shipping.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Shipment Entity - Represents a shipment for an order
 * 
 * Location: shipping-service/src/main/java/com/payment/shipping/model/Shipment.java
 */
@Entity
@Table(name = "shipments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long orderId;

    @Column(nullable = false, unique = true)
    private String correlationId;

    @Column(nullable = false, unique = true, length = 50)
    private String trackingNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShipmentStatus status;

    @Column(length = 50)
    private String carrier; // FEDEX, UPS, DHL, etc.

    @Column(length = 200)
    private String shippingAddress;

    private LocalDateTime estimatedDelivery;

    private LocalDateTime actualDelivery;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Check if shipment is in final state
     */
    public boolean isFinalState() {
        return status == ShipmentStatus.DELIVERED || status == ShipmentStatus.CANCELLED;
    }

    /**
     * Progress to next shipment status
     */
    public void progressStatus() {
        switch (status) {
            case CREATED -> status = ShipmentStatus.IN_TRANSIT;
            case IN_TRANSIT -> status = ShipmentStatus.OUT_FOR_DELIVERY;
            case OUT_FOR_DELIVERY -> {
                status = ShipmentStatus.DELIVERED;
                actualDelivery = LocalDateTime.now();
            }
            default -> {} // Already in final state
        }
    }
}