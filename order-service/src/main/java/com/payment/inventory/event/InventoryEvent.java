package com.payment.inventory.event;

import com.payment.common.event.BaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Event published by Inventory Service
 * Indicates whether inventory was successfully reserved or failed
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class InventoryEvent extends BaseEvent {
    
    private Long orderId;
    private Long productId;
    private Integer quantity;
    private boolean success;
    private String message;
    
    public InventoryEvent(String correlationId, String eventType, Long orderId, 
                          Long productId, Integer quantity, boolean success, String message) {
        super(correlationId, eventType, "inventory-service");
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.success = success;
        this.message = message;
    }
    
    // Factory methods for common scenarios
    public static InventoryEvent reserved(String correlationId, Long orderId, 
                                          Long productId, Integer quantity) {
        return new InventoryEvent(
            correlationId, 
            "INVENTORY_RESERVED", 
            orderId, 
            productId, 
            quantity, 
            true, 
            "Inventory reserved successfully"
        );
    }
    
    public static InventoryEvent failed(String correlationId, Long orderId, 
                                        Long productId, Integer quantity, String reason) {
        return new InventoryEvent(
            correlationId, 
            "INVENTORY_FAILED", 
            orderId, 
            productId, 
            quantity, 
            false, 
            reason
        );
    }
    
    public static InventoryEvent unreserved(String correlationId, Long orderId, 
                                            Long productId, Integer quantity) {
        return new InventoryEvent(
            correlationId, 
            "INVENTORY_UNRESERVED", 
            orderId, 
            productId, 
            quantity, 
            true, 
            "Inventory unreserved (compensation)"
        );
    }
}