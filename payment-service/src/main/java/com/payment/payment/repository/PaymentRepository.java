package com.payment.payment.repository;

import com.payment.payment.model.Payment;
import com.payment.payment.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Payment entity
 * 
 * Location: payment-service/src/main/java/com/payment/payment/repository/PaymentRepository.java
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Find payment by order ID
     */
    Optional<Payment> findByOrderId(Long orderId);

    /**
     * Find payment by correlation ID
     */
    Optional<Payment> findByCorrelationId(String correlationId);

    /**
     * Find payments by status
     */
    List<Payment> findByStatus(PaymentStatus status);

    /**
     * Find high-risk payments (fraud detection)
     */
    @Query("SELECT p FROM Payment p WHERE p.riskScore >= :threshold")
    List<Payment> findHighRiskPayments(Integer threshold);

    /**
     * Check if payment exists for order
     */
    boolean existsByOrderId(Long orderId);

    /**
     * Check if payment exists by correlation ID
     */
    boolean existsByCorrelationId(String correlationId);
}