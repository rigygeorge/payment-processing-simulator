package com.payment.payment.service;

import com.payment.payment.event.PaymentEvent;
import com.payment.payment.model.Payment;
import com.payment.payment.model.PaymentStatus;
import com.payment.payment.repository.PaymentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Payment Service - Business logic for payment processing
 * 
 * Location: payment-service/src/main/java/com/payment/payment/service/PaymentService.java
 */
@Service
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final IdempotencyService idempotencyService;
    private final FraudDetectionService fraudDetectionService;
    private final EventPublisher eventPublisher;

    public PaymentService(
            PaymentRepository paymentRepository,
            IdempotencyService idempotencyService,
            FraudDetectionService fraudDetectionService,
            EventPublisher eventPublisher) {
        this.paymentRepository = paymentRepository;
        this.idempotencyService = idempotencyService;
        this.fraudDetectionService = fraudDetectionService;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Process payment for an order
     * Includes idempotency check, fraud detection, and payment processing
     */
    @Transactional
    public PaymentEvent processPayment(
            Long orderId,
            BigDecimal amount,
            Long customerId,
            String correlationId) {
        
        log.info("═══════════════════════════════════════════════════");
        log.info("💳 Processing payment for order: {}", orderId);
        log.info("Amount: ${}, Customer: {}", amount, customerId);
        log.info("Correlation ID: {}", correlationId);
        log.info("═══════════════════════════════════════════════════");

        try {
            // Step 1: Idempotency check
            log.info("Step 1: Checking idempotency...");
            if (idempotencyService.isAlreadyProcessed(orderId)) {
                log.warn("⚠️ Duplicate payment request detected - Returning cached result");
                
                // Find existing payment
                Payment existingPayment = paymentRepository.findByOrderId(orderId)
                    .orElseThrow(() -> new RuntimeException("Payment record not found"));
                
                return buildPaymentEvent(existingPayment);
            }

            // Step 2: Create payment record
            log.info("Step 2: Creating payment record...");
            Payment payment = Payment.builder()
                .orderId(orderId)
                .correlationId(correlationId)
                .amount(amount)
                .paymentMethod("CREDIT_CARD") // Simulated
                .status(PaymentStatus.PENDING)
                .riskScore(0)
                .build();

            // Step 3: Fraud detection
            log.info("Step 3: Running fraud detection...");
            int riskScore = fraudDetectionService.calculateRiskScore(amount, customerId, orderId);
            payment.setRiskScore(riskScore);

            // Check if transaction should be blocked
            if (fraudDetectionService.shouldBlockTransaction(riskScore)) {
                log.warn("🚫 FRAUD DETECTED - Blocking transaction");
                payment.setStatus(PaymentStatus.FRAUD_DETECTED);
                payment.setFailureReason("Transaction blocked due to high fraud risk (score: " + riskScore + ")");
                
                paymentRepository.save(payment);
                
                // Publish PAYMENT_FAILED event
                PaymentEvent event = PaymentEvent.fraudDetected(correlationId, orderId, amount, riskScore);
                eventPublisher.publishEvent("payment-events", correlationId, event);
                
                return event;
            }

            // Step 4: Process payment (simulate payment gateway)
            log.info("Step 4: Processing payment with gateway...");
            boolean paymentSuccessful = fraudDetectionService.simulatePaymentProcessing();

            if (paymentSuccessful) {
                // Payment succeeded
                String transactionId = fraudDetectionService.generateTransactionId();
                payment.setStatus(PaymentStatus.COMPLETED);
                payment.setTransactionId(transactionId);
                
                paymentRepository.save(payment);
                
                // Mark as processed in Redis
                idempotencyService.markAsProcessed(orderId);
                
                log.info("✅ Payment processed successfully");
                log.info("Transaction ID: {}", transactionId);
                log.info("Risk Score: {}", riskScore);
                
                // Publish PAYMENT_PROCESSED event
                PaymentEvent event = PaymentEvent.processed(
                    correlationId, orderId, payment.getId(), amount, "CREDIT_CARD", riskScore
                );
                eventPublisher.publishEvent("payment-events", correlationId, event);
                
                return event;
                
            } else {
                // Payment declined by gateway
                payment.setStatus(PaymentStatus.FAILED);
                payment.setFailureReason("Payment declined by payment gateway");
                
                paymentRepository.save(payment);
                
                log.warn("❌ Payment declined by gateway");
                
                // Publish PAYMENT_FAILED event
                PaymentEvent event = PaymentEvent.failed(
                    correlationId, orderId, amount, "Payment declined by payment gateway"
                );
                eventPublisher.publishEvent("payment-events", correlationId, event);
                
                return event;
            }

        } catch (Exception e) {
            log.error("❌ Error processing payment: {}", e.getMessage(), e);
            
            PaymentEvent event = PaymentEvent.failed(
                correlationId, orderId, amount, "Payment processing error: " + e.getMessage()
            );
            eventPublisher.publishEvent("payment-events", correlationId, event);
            
            return event;
        } finally {
            log.info("═══════════════════════════════════════════════════\n");
        }
    }

    /**
     * Get all payments
     */
    @Transactional(readOnly = true)
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    /**
     * Get payment by order ID
     */
    @Transactional(readOnly = true)
    public Payment getPaymentByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
            .orElseThrow(() -> new RuntimeException("Payment not found for order: " + orderId));
    }

    /**
     * Build PaymentEvent from Payment entity
     */
    private PaymentEvent buildPaymentEvent(Payment payment) {
        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            return PaymentEvent.processed(
                payment.getCorrelationId(),
                payment.getOrderId(),
                payment.getId(),
                payment.getAmount(),
                payment.getPaymentMethod(),
                payment.getRiskScore()
            );
        } else {
            return PaymentEvent.failed(
                payment.getCorrelationId(),
                payment.getOrderId(),
                payment.getAmount(),
                payment.getFailureReason()
            );
        }
    }
}