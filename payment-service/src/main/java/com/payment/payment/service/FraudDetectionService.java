package com.payment.payment.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Random;

/**
 * Fraud Detection Service - Calculates risk score for payments
 * 
 * Location: payment-service/src/main/java/com/payment/payment/service/FraudDetectionService.java
 * 
 * NOTE: This is a SIMPLIFIED simulation for demonstration.
 * Real fraud detection would use machine learning models, historical data, etc.
 */
@Service
@Slf4j
public class FraudDetectionService {

    private final Random random = new Random();

    // Risk thresholds
    private static final int LOW_RISK_THRESHOLD = 30;
    private static final int MEDIUM_RISK_THRESHOLD = 60;
    private static final int HIGH_RISK_THRESHOLD = 80;

    /**
     * Calculate risk score for a payment (0-100)
     * Higher score = higher risk
     * 
     * Factors considered (simplified):
     * - Transaction amount
     * - Random variation (simulates other factors)
     */
    public int calculateRiskScore(BigDecimal amount, Long customerId, Long orderId) {
        log.info("🔍 Calculating fraud risk score...");
        log.info("Amount: ${}, Customer: {}, Order: {}", amount, customerId, orderId);

        int riskScore = 0;

        // Factor 1: Transaction amount
        // Higher amounts = higher risk
        if (amount.compareTo(new BigDecimal("5000")) > 0) {
            riskScore += 50;
            log.info("⚠️ High amount transaction (>${}) - Adding 50 risk points", 5000);
        } else if (amount.compareTo(new BigDecimal("1000")) > 0) {
            riskScore += 30;
            log.info("⚠️ Medium amount transaction (>${}) - Adding 30 risk points", 1000);
        } else if (amount.compareTo(new BigDecimal("500")) > 0) {
            riskScore += 15;
            log.info("⚠️ Moderate amount transaction (>${}) - Adding 15 risk points", 500);
        } else {
            riskScore += 5;
            log.info("✓ Low amount transaction - Adding 5 risk points");
        }

        // Factor 2: Random variation (simulates velocity checks, location, device fingerprinting, etc.)
        int randomFactor = random.nextInt(40); // 0-39
        riskScore += randomFactor;
        log.info("Random risk factor: +{} points", randomFactor);

        // Ensure score stays within 0-100
        riskScore = Math.min(riskScore, 100);

        log.info("📊 Final Risk Score: {}/100", riskScore);
        log.info(getRiskLevel(riskScore));

        return riskScore;
    }

    /**
     * Determine if transaction should be blocked based on risk score
     */
    public boolean shouldBlockTransaction(int riskScore) {
        boolean shouldBlock = riskScore >= HIGH_RISK_THRESHOLD;
        
        if (shouldBlock) {
            log.warn("🚫 Transaction BLOCKED - Risk score {} exceeds threshold {}", 
                riskScore, HIGH_RISK_THRESHOLD);
        } else {
            log.info("✅ Transaction APPROVED - Risk score {} below threshold {}", 
                riskScore, HIGH_RISK_THRESHOLD);
        }
        
        return shouldBlock;
    }

    /**
     * Get risk level description
     */
    public String getRiskLevel(int riskScore) {
        if (riskScore < LOW_RISK_THRESHOLD) {
            return "🟢 Risk Level: LOW (" + riskScore + "/100)";
        } else if (riskScore < MEDIUM_RISK_THRESHOLD) {
            return "🟡 Risk Level: MEDIUM (" + riskScore + "/100)";
        } else if (riskScore < HIGH_RISK_THRESHOLD) {
            return "🟠 Risk Level: HIGH (" + riskScore + "/100) - Requires review";
        } else {
            return "🔴 Risk Level: CRITICAL (" + riskScore + "/100) - BLOCKED";
        }
    }

    /**
     * Simulate payment gateway processing
     * Returns true if payment successful, false if declined
     */
    public boolean simulatePaymentProcessing() {
        // 90% success rate (10% random payment failures)
        boolean success = random.nextInt(100) < 90;
        
        if (success) {
            log.info("💳 Payment gateway response: APPROVED");
        } else {
            log.warn("❌ Payment gateway response: DECLINED");
        }
        
        return success;
    }

    /**
     * Generate mock transaction ID
     */
    public String generateTransactionId() {
        return "TXN-" + System.currentTimeMillis() + "-" + random.nextInt(10000);
    }
}