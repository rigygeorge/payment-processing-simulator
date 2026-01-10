package com.payment.common.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Base event class containing common fields for all events
 * All event types will extend this class
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseEvent {
    
    /**
     * Unique identifier for this specific event
     */
    private String eventId;
    
    /**
     * Correlation ID to track the entire flow across services
     * Same correlationId will be used for all events related to one order
     */
    private String correlationId;
    
    /**
     * Timestamp when the event was created
     */
    private LocalDateTime timestamp;
    
    /**
     * Event type (e.g., ORDER_CREATED, PAYMENT_PROCESSED)
     */
    private String eventType;
    
    /**
     * Source service that created this event
     */
    private String source;
    
    /**
     * Constructor to auto-generate eventId and timestamp
     */
    public BaseEvent(String correlationId, String eventType, String source) {
        this.eventId = UUID.randomUUID().toString();
        this.correlationId = correlationId;
        this.timestamp = LocalDateTime.now();
        this.eventType = eventType;
        this.source = source;
    }
}