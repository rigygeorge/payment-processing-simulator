package com.payment.shipping.event;

import com.payment.common.event.BaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Event published by Shipping Service
 * Contains shipment details and tracking information
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ShipmentEvent extends BaseEvent {
    
    private Long orderId;
    private Long shipmentId;
    private String trackingNumber;
    private String shipmentStatus; // CREATED, IN_TRANSIT, DELIVERED
    private String carrier;
    private String estimatedDelivery;
    
    public ShipmentEvent(String correlationId, String eventType, Long orderId, 
                         Long shipmentId, String trackingNumber, String shipmentStatus,
                         String carrier, String estimatedDelivery) {
        super(correlationId, eventType, "shipping-service");
        this.orderId = orderId;
        this.shipmentId = shipmentId;
        this.trackingNumber = trackingNumber;
        this.shipmentStatus = shipmentStatus;
        this.carrier = carrier;
        this.estimatedDelivery = estimatedDelivery;
    }
    
    // Factory methods
    public static ShipmentEvent created(String correlationId, Long orderId, 
                                        Long shipmentId, String trackingNumber,
                                        String carrier, String estimatedDelivery) {
        return new ShipmentEvent(
            correlationId,
            "SHIPMENT_CREATED",
            orderId,
            shipmentId,
            trackingNumber,
            "CREATED",
            carrier,
            estimatedDelivery
        );
    }
    
    public static ShipmentEvent updated(String correlationId, Long orderId, 
                                        Long shipmentId, String trackingNumber,
                                        String newStatus) {
        return new ShipmentEvent(
            correlationId,
            "SHIPMENT_UPDATED",
            orderId,
            shipmentId,
            trackingNumber,
            newStatus,
            null,
            null
        );
    }
}