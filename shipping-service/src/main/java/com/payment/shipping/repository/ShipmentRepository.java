package com.payment.shipping.repository;

import com.payment.shipping.model.Shipment;
import com.payment.shipping.model.ShipmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Shipment entity
 * 
 * Location: shipping-service/src/main/java/com/payment/shipping/repository/ShipmentRepository.java
 */
@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    /**
     * Find shipment by order ID
     */
    Optional<Shipment> findByOrderId(Long orderId);

    /**
     * Find shipment by correlation ID
     */
    Optional<Shipment> findByCorrelationId(String correlationId);

    /**
     * Find shipment by tracking number
     */
    Optional<Shipment> findByTrackingNumber(String trackingNumber);

    /**
     * Find shipments by status
     */
    List<Shipment> findByStatus(ShipmentStatus status);

    /**
     * Find shipments that are in progress (not final state)
     */
    @Query("SELECT s FROM Shipment s WHERE s.status IN ('CREATED', 'IN_TRANSIT', 'OUT_FOR_DELIVERY')")
    List<Shipment> findInProgressShipments();

    /**
     * Check if shipment exists for order
     */
    boolean existsByOrderId(Long orderId);
}