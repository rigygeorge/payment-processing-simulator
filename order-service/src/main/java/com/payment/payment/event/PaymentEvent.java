package com.payment.payment.event;

import com.payment.common.event.BaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * Event published by Payment Service
 * Indicates payment processing result
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class PaymentEvent extends BaseEvent {
    
    private Long orderId;
    private Long paymentId;
    private BigDecimal amount;
    private String paymentMethod;
    private boolean success;
    private String message;
    private Integer riskScore; // Fraud detection score (0-100)
    
    public PaymentEvent(String correlationId, String eventType, Long orderId, 
                        Long paymentId, BigDecimal amount, String paymentMethod,
                        boolean success, String message, Integer riskScore) {
        super(correlationId, eventType, "payment-service");
        this.orderId = orderId;
        this.paymentId = paymentId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.success = success;
        this.message = message;
        this.riskScore = riskScore;
    }
    
    // Factory methods for convenience
    public static PaymentEvent processed(String correlationId, Long orderId, 
                                         Long paymentId, BigDecimal amount, 
                                         String paymentMethod, Integer riskScore) {
        return new PaymentEvent(
            correlationId,
            "PAYMENT_PROCESSED",
            orderId,
            paymentId,
            amount,
            paymentMethod,
            true,
            "Payment processed successfully",
            riskScore
        );
    }
    
    public static PaymentEvent failed(String correlationId, Long orderId, 
                                      BigDecimal amount, String reason) {
        return new PaymentEvent(
            correlationId,
            "PAYMENT_FAILED",
            orderId,
            null,
            amount,
            null,
            false,
            reason,
            null
        );
    }
    
    public static PaymentEvent fraudDetected(String correlationId, Long orderId, 
                                             BigDecimal amount, Integer riskScore) {
        return new PaymentEvent(
            correlationId,
            "PAYMENT_FAILED",
            orderId,
            null,
            amount,
            null,
            false,
            "Transaction flagged as high risk (score: " + riskScore + ")",
            riskScore
        );
    }
}