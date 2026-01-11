package com.payment.order.model;

/**
 * Order Status Enum - Represents the lifecycle of an order
 * 
 * Location: order-service/src/main/java/com/payment/order/model/OrderStatus.java
 */
public enum OrderStatus {
    
    /**
     * Order has been created and is waiting for processing
     */
    PENDING,
    
    /**
     * Inventory has been reserved for this order
     */
    INVENTORY_RESERVED,
    
    /**
     * Payment has been processed successfully
     */
    PAYMENT_PROCESSED,
    
    /**
     * Shipment has been created and order is being delivered
     */
    SHIPPED,
    
    /**
     * Order completed successfully - end state
     */
    COMPLETED,
    
    /**
     * Order failed at some step - end state
     */
    FAILED,
    
    /**
     * Order is being rolled back due to failure
     */
    COMPENSATING;
    
    /**
     * Check if order is in a final state (no more processing needed)
     */
    public boolean isFinalState() {
        return this == COMPLETED || this == FAILED;
    }
    
    /**
     * Check if order is still in progress
     */
    public boolean isInProgress() {
        return !isFinalState();
    }
}