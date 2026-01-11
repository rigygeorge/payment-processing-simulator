package com.payment.inventory.repository;

import com.payment.inventory.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Product entity
 * 
 * Location: inventory-service/src/main/java/com/payment/inventory/repository/ProductRepository.java
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Find product by SKU (Stock Keeping Unit)
     */
    Optional<Product> findBySku(String sku);

    /**
     * Find products with available quantity greater than 0
     */
    @Query("SELECT p FROM Product p WHERE p.availableQuantity > 0")
    List<Product> findAvailableProducts();

    /**
     * Find products by name (case-insensitive partial match)
     */
    List<Product> findByNameContainingIgnoreCase(String name);

    /**
     * Check if product exists by SKU
     */
    boolean existsBySku(String sku);

    /**
     * Find low stock products (available quantity below threshold)
     */
    @Query("SELECT p FROM Product p WHERE p.availableQuantity < :threshold")
    List<Product> findLowStockProducts(Integer threshold);
}