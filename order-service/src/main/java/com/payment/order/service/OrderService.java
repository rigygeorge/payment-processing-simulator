package com.payment.order.service;

import com.payment.order.dto.CreateOrderRequest;
import com.payment.order.dto.OrderResponse;
import com.payment.order.event.OrderEvent;
import com.payment.order.model.Order;
import com.payment.order.model.OrderItem;
import com.payment.order.model.OrderStatus;
import com.payment.order.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Order Service - Business logic for order management
 * 
 * Location: order-service/src/main/java/com/payment/order/service/OrderService.java
 */
@Service
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final EventPublisher eventPublisher;

    public OrderService(OrderRepository orderRepository, EventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Create a new order and publish ORDER_CREATED event
     */
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        log.info("Creating new order for customer: {}", request.getCustomerId());

        // Generate unique correlation ID for Saga tracking
        String correlationId = UUID.randomUUID().toString();

        // Build Order entity
        Order order = Order.builder()
            .customerId(request.getCustomerId())
            .correlationId(correlationId)
            .status(OrderStatus.PENDING)
            .build();

        // Add order items
        request.getItems().forEach(itemRequest -> {
            OrderItem item = OrderItem.builder()
                .productId(itemRequest.getProductId())
                .quantity(itemRequest.getQuantity())
                .price(itemRequest.getPrice())
                .build();
            order.addItem(item);
        });

        // Calculate total amount
        order.calculateTotal();

        // Save to database
        Order savedOrder = orderRepository.save(order);
        log.info("Order created with ID: {} and correlationId: {}", savedOrder.getId(), correlationId);

        // Publish ORDER_CREATED event to Kafka
        OrderEvent orderEvent = buildOrderEvent(savedOrder, "ORDER_CREATED");
        eventPublisher.publishEvent("order-events", correlationId, orderEvent);
        log.info("ORDER_CREATED event published for order: {}", savedOrder.getId());

        return mapToResponse(savedOrder);
    }

    /**
     * Get order by ID
     */
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId) {
        log.info("Fetching order with ID: {}", orderId);
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));
        return mapToResponse(order);
    }

    /**
     * Get order by correlation ID (used by Saga)
     */
    @Transactional(readOnly = true)
    public OrderResponse getOrderByCorrelationId(String correlationId) {
        log.info("Fetching order with correlationId: {}", correlationId);
        Order order = orderRepository.findByCorrelationId(correlationId)
            .orElseThrow(() -> new RuntimeException("Order not found with correlationId: " + correlationId));
        return mapToResponse(order);
    }

    /**
     * Get all orders for a customer
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByCustomerId(Long customerId) {
        log.info("Fetching orders for customer: {}", customerId);
        return orderRepository.findByCustomerId(customerId)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Update order status (called by Saga event handlers)
     */
    @Transactional
    public void updateOrderStatus(String correlationId, OrderStatus newStatus, String failureReason) {
        log.info("Updating order status for correlationId: {} to {}", correlationId, newStatus);
        
        Order order = orderRepository.findByCorrelationId(correlationId)
            .orElseThrow(() -> new RuntimeException("Order not found with correlationId: " + correlationId));

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(newStatus);

        if (failureReason != null) {
            order.setFailureReason(failureReason);
        }

        orderRepository.save(order);
        log.info("Order {} status updated from {} to {}", order.getId(), oldStatus, newStatus);

        // Publish status change event if final state
        if (newStatus.isFinalState()) {
            String eventType = newStatus == OrderStatus.COMPLETED ? "ORDER_COMPLETED" : "ORDER_FAILED";
            OrderEvent event = buildOrderEvent(order, eventType);
            eventPublisher.publishEvent("order-events", correlationId, event);
            log.info("{} event published for order: {}", eventType, order.getId());
        }
    }

    /**
     * Build OrderEvent from Order entity
     */
    private OrderEvent buildOrderEvent(Order order, String eventType) {
        List<OrderEvent.OrderItemEvent> itemEvents = order.getItems().stream()
            .map(item -> new OrderEvent.OrderItemEvent(
                item.getProductId(),
                item.getQuantity(),
                item.getPrice()
            ))
            .collect(Collectors.toList());

        return new OrderEvent(
            order.getCorrelationId(),
            eventType,
            order.getId(),
            order.getCustomerId(),
            order.getTotalAmount(),
            order.getStatus().name(),
            itemEvents
        );
    }

    /**
     * Map Order entity to OrderResponse DTO
     */
    private OrderResponse mapToResponse(Order order) {
        List<OrderResponse.OrderItemResponse> itemResponses = order.getItems().stream()
            .map(item -> OrderResponse.OrderItemResponse.builder()
                .itemId(item.getId())
                .productId(item.getProductId())
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .subtotal(item.getSubtotal())
                .build())
            .collect(Collectors.toList());

        return OrderResponse.builder()
            .orderId(order.getId())
            .customerId(order.getCustomerId())
            .correlationId(order.getCorrelationId())
            .status(order.getStatus())
            .totalAmount(order.getTotalAmount())
            .items(itemResponses)
            .failureReason(order.getFailureReason())
            .createdAt(order.getCreatedAt())
            .updatedAt(order.getUpdatedAt())
            .build();
    }
}