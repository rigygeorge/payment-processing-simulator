package com.payment.payment.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Idempotency Service - Prevents duplicate payment processing
 * Uses Redis to track processed payments
 * 
 * Location: payment-service/src/main/java/com/payment/payment/service/IdempotencyService.java
 */
@Service
@Slf4j
public class IdempotencyService {

    private final StringRedisTemplate redisTemplate;
    
    private static final String KEY_PREFIX = "payment:processed:";
    private static final Duration TTL = Duration.ofHours(24); // 24 hours

    public IdempotencyService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Check if payment has already been processed
     * 
     * @param orderId - Order ID to check
     * @return true if already processed, false otherwise
     */
    public boolean isAlreadyProcessed(Long orderId) {
        String key = KEY_PREFIX + orderId;
        Boolean exists = redisTemplate.hasKey(key);
        
        if (Boolean.TRUE.equals(exists)) {
            log.warn("⚠️ DUPLICATE DETECTED: Payment for order {} already processed", orderId);
            return true;
        }
        
        log.info("✓ First time processing payment for order {}", orderId);
        return false;
    }

    /**
     * Mark payment as processed
     * Sets a key in Redis with 24-hour TTL
     * 
     * @param orderId - Order ID to mark as processed
     */
    public void markAsProcessed(Long orderId) {
        String key = KEY_PREFIX + orderId;
        redisTemplate.opsForValue().set(key, "processed", TTL);
        
        log.info("✅ Marked payment for order {} as processed (TTL: 24 hours)", orderId);
    }

    /**
     * Check if payment was processed (for debugging)
     */
    public boolean checkProcessed(Long orderId) {
        String key = KEY_PREFIX + orderId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * Remove processed flag (for testing/compensation)
     */
    public void removeProcessedFlag(Long orderId) {
        String key = KEY_PREFIX + orderId;
        redisTemplate.delete(key);
        log.info("🗑️ Removed processed flag for order {}", orderId);
    }
}