package com.payment.order.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka Consumer Configuration
 */
@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        
        // Kafka broker address
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        
        // Consumer group - all consumers in same group share the workload
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        
        // Deserializers - convert bytes back to objects
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        
        // Start reading from earliest message if no offset exists
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        
        // JSON deserializer settings
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*"); // Trust all packages (be careful in production!)
        configProps.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false); // Don't require type headers
        configProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "com.payment.common.event.BaseEvent");
        
        // Commit offset after processing (at-least-once delivery)
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        configProps.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 1000);
        
        // Fetch settings for performance
        configProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100); // Process up to 100 records per poll
        configProps.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 1); // Minimum data to fetch
        configProps.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 500); // Max wait time for fetch
        
        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        
        // Number of concurrent consumers per listener
        factory.setConcurrency(3);
        
        return factory;
    }
}