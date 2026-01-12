package com.payment.shipping.model;

/**
 * Shipment Status Enum - Lifecycle of a shipment
 * 
 * Location: shipping-service/src/main/java/com/payment/shipping/model/ShipmentStatus.java
 */
public enum ShipmentStatus {
    
    /**
     * Shipment created, waiting to be picked up
     */
    CREATED,
    
    /**
     * Package picked up and in transit to destination
     */
    IN_TRANSIT,
    
    /**
     * Package out for delivery (last mile)
     */
    OUT_FOR_DELIVERY,
    
    /**
     * Package successfully delivered
     */
    DELIVERED,
    
    /**
     * Shipment cancelled (order cancelled or payment failed)
     */
    CANCELLED;
}