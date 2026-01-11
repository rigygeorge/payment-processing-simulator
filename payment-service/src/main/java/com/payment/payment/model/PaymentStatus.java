package com.payment.payment.model;

/**
 * Payment Status Enum
 * 
 * Location: payment-service/src/main/java/com/payment/payment/model/PaymentStatus.java
 */
public enum PaymentStatus {
    
    /**
     * Payment is being processed
     */
    PENDING,
    
    /**
     * Payment completed successfully
     */
    COMPLETED,
    
    /**
     * Payment failed (declined, fraud, etc.)
     */
    FAILED,
    
    /**
     * Payment flagged as high risk
     */
    FRAUD_DETECTED;
}