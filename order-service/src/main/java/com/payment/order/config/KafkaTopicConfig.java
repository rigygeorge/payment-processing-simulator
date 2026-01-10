package com.payment.order.config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka Topic Configuration
 */
@Configuration
public class KafkaTopicConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(configs);
    }

    /**
     * Order Events Topic
     * Used for: ORDER_CREATED, ORDER_COMPLETED, ORDER_FAILED
     */
    @Bean
    public NewTopic orderEventsTopic() {
        return new NewTopic("order-events", 3, (short) 1);
        // 3 partitions for parallel processing
        // Replication factor 1 (only 1 Kafka broker in dev)
    }

    /**
     * Inventory Events Topic
     * Used for: INVENTORY_RESERVED, INVENTORY_FAILED, INVENTORY_UNRESERVED
     */
    @Bean
    public NewTopic inventoryEventsTopic() {
        return new NewTopic("inventory-events", 3, (short) 1);
    }

    /**
     * Payment Events Topic
     * Used for: PAYMENT_PROCESSED, PAYMENT_FAILED
     */
    @Bean
    public NewTopic paymentEventsTopic() {
        return new NewTopic("payment-events", 3, (short) 1);
    }

    /**
     * Shipping Events Topic
     * Used for: SHIPMENT_CREATED, SHIPMENT_UPDATED, SHIPMENT_DELIVERED
     */
    @Bean
    public NewTopic shippingEventsTopic() {
        return new NewTopic("shipping-events", 3, (short) 1);
    }

    /**
     * Notification Events Topic
     * Used by notification service to consume ALL events
     */
    @Bean
    public NewTopic notificationEventsTopic() {
        return new NewTopic("notification-events", 3, (short) 1);
    }

    /**
     * Dead Letter Queue Topic
     * For messages that failed processing after retries
     */
    @Bean
    public NewTopic deadLetterQueueTopic() {
        return new NewTopic("dlq-events", 1, (short) 1);
    }
}