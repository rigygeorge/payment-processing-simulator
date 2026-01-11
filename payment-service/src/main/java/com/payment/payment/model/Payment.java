package com.payment.payment.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment Entity - Represents a payment transaction
 * 
 * Location: payment-service/src/main/java/com/payment/payment/model/Payment.java
 */
@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false, unique = true)
    private String correlationId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(length = 50)
    private String paymentMethod; // CREDIT_CARD, DEBIT_CARD, PAYPAL, etc.

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(nullable = false)
    private Integer riskScore; // 0-100, fraud detection score

    @Column(length = 500)
    private String failureReason;

    @Column(length = 100)
    private String transactionId; // External payment gateway transaction ID

    @Column(length = 50)
    private String idempotencyKey; // Key used for Redis deduplication

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Check if payment is in final state
     */
    public boolean isFinalState() {
        return status == PaymentStatus.COMPLETED || status == PaymentStatus.FAILED;
    }
}