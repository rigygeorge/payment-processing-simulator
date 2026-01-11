package com.payment.inventory.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Product Entity - Represents inventory items
 * 
 * Location: inventory-service/src/main/java/com/payment/inventory/model/Product.java
 */
@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String sku; // Stock Keeping Unit (unique product code)

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private Integer availableQuantity; // Available for sale

    @Column(nullable = false)
    private Integer reservedQuantity; // Reserved for pending orders

    @Version // Optimistic locking to prevent race conditions
    private Long version;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Get total quantity (available + reserved)
     */
    public Integer getTotalQuantity() {
        return availableQuantity + reservedQuantity;
    }

    /**
     * Check if we have enough stock to reserve
     */
    public boolean hasEnoughStock(Integer requestedQuantity) {
        return availableQuantity >= requestedQuantity;
    }

    /**
     * Reserve inventory (decrease available, increase reserved)
     * Returns true if successful, false if insufficient stock
     */
    public boolean reserve(Integer quantity) {
        if (!hasEnoughStock(quantity)) {
            return false;
        }
        this.availableQuantity -= quantity;
        this.reservedQuantity += quantity;
        return true;
    }

    /**
     * Unreserve inventory (increase available, decrease reserved)
     * Used for compensation when payment fails
     */
    public void unreserve(Integer quantity) {
        this.reservedQuantity -= quantity;
        this.availableQuantity += quantity;
    }

    /**
     * Complete sale (decrease reserved)
     * Used when order is shipped
     */
    public void completeSale(Integer quantity) {
        this.reservedQuantity -= quantity;
    }
}