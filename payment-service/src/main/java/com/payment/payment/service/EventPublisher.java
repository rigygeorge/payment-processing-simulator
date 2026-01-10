package com.payment.payment.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Service to publish events to Kafka topics
 */
@Service
@Slf4j
public class EventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public EventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Publish event to specified topic
     * @param topic - Kafka topic name
     * @param key - Message key (usually correlationId or orderId)
     * @param event - Event object to publish
     */
    public void publishEvent(String topic, String key, Object event) {
        log.info("Publishing event to topic: {} with key: {}", topic, key);
        
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, key, event);
        
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("✓ Event published successfully to topic: {} | Partition: {} | Offset: {}", 
                    topic, 
                    result.getRecordMetadata().partition(), 
                    result.getRecordMetadata().offset());
            } else {
                log.error("✗ Failed to publish event to topic: {} | Error: {}", topic, ex.getMessage());
            }
        });
    }

    /**
     * Publish event and wait for confirmation (synchronous)
     * Use this when you need to ensure message was sent before continuing
     */
    public void publishEventSync(String topic, String key, Object event) {
        try {
            log.info("Publishing event synchronously to topic: {} with key: {}", topic, key);
            SendResult<String, Object> result = kafkaTemplate.send(topic, key, event).get();
            log.info("✓ Event published successfully to topic: {} | Partition: {} | Offset: {}", 
                topic, 
                result.getRecordMetadata().partition(), 
                result.getRecordMetadata().offset());
        } catch (Exception e) {
            log.error("✗ Failed to publish event to topic: {} | Error: {}", topic, e.getMessage());
            throw new RuntimeException("Failed to publish event", e);
        }
    }
}