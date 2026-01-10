package com.payment.order.event;

import com.payment.common.event.BaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

/**
 * Event published by Order Service
 * Contains order details that other services need
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class OrderEvent extends BaseEvent {
    
    private Long orderId;
    private Long customerId;
    private BigDecimal totalAmount;
    private String orderStatus;
    private List<OrderItemEvent> items;
    
    public OrderEvent(String correlationId, String eventType, Long orderId, 
                      Long customerId, BigDecimal totalAmount, String orderStatus,
                      List<OrderItemEvent> items) {
        super(correlationId, eventType, "order-service");
        this.orderId = orderId;
        this.customerId = customerId;
        this.totalAmount = totalAmount;
        this.orderStatus = orderStatus;
        this.items = items;
    }
    
    /**
     * Nested class for order items
     */
    @Data
    @NoArgsConstructor
    public static class OrderItemEvent {
        private Long productId;
        private Integer quantity;
        private BigDecimal price;
        
        public OrderItemEvent(Long productId, Integer quantity, BigDecimal price) {
            this.productId = productId;
            this.quantity = quantity;
            this.price = price;
        }
    }
}