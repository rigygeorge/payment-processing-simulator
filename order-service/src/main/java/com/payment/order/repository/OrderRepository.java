package com.payment.order.repository;

import com.payment.order.model.Order;
import com.payment.order.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Order entity - Database access layer
 * 
 * Location: order-service/src/main/java/com/payment/order/repository/OrderRepository.java
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Find order by correlation ID (used for Saga tracking)
     */
    Optional<Order> findByCorrelationId(String correlationId);

    /**
     * Find all orders for a specific customer
     */
    List<Order> findByCustomerId(Long customerId);

    /**
     * Find orders by status
     */
    List<Order> findByStatus(OrderStatus status);

    /**
     * Find orders by customer and status
     */
    List<Order> findByCustomerIdAndStatus(Long customerId, OrderStatus status);

    /**
     * Find orders created within a date range
     */
    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate")
    List<Order> findOrdersByDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Count orders by status
     */
    long countByStatus(OrderStatus status);

    /**
     * Check if order exists by correlation ID
     */
    boolean existsByCorrelationId(String correlationId);
}