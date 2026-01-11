package com.payment.inventory.config;

import com.payment.inventory.model.Product;
import com.payment.inventory.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Data Loader - Seeds initial products into database
 * 
 * Location: inventory-service/src/main/java/com/payment/inventory/config/DataLoader.java
 */
@Configuration
@Slf4j
public class DataLoader {

    @Bean
    CommandLineRunner initDatabase(ProductRepository productRepository) {
        return args -> {
            // Check if products already exist
            if (productRepository.count() > 0) {
                log.info("Database already contains {} products. Skipping initialization.", 
                    productRepository.count());
                return;
            }

            log.info("Initializing database with sample products...");

            // Create sample products
            Product product1 = Product.builder()
                .sku("LAPTOP-001")
                .name("Dell XPS 13 Laptop")
                .description("13-inch laptop with Intel Core i7 processor")
                .availableQuantity(50)
                .reservedQuantity(0)
                .build();

            Product product2 = Product.builder()
                .sku("PHONE-001")
                .name("iPhone 15 Pro")
                .description("Latest iPhone with A17 Pro chip")
                .availableQuantity(100)
                .reservedQuantity(0)
                .build();

            Product product3 = Product.builder()
                .sku("TABLET-001")
                .name("iPad Air")
                .description("10.9-inch iPad with M1 chip")
                .availableQuantity(75)
                .reservedQuantity(0)
                .build();

            Product product4 = Product.builder()
                .sku("WATCH-001")
                .name("Apple Watch Series 9")
                .description("Smartwatch with health tracking")
                .availableQuantity(200)
                .reservedQuantity(0)
                .build();

            Product product5 = Product.builder()
                .sku("HEADPHONE-001")
                .name("Sony WH-1000XM5")
                .description("Noise-cancelling wireless headphones")
                .availableQuantity(150)
                .reservedQuantity(0)
                .build();

            Product product6 = Product.builder()
                .sku("MOUSE-001")
                .name("Logitech MX Master 3")
                .description("Wireless ergonomic mouse")
                .availableQuantity(300)
                .reservedQuantity(0)
                .build();

            Product product7 = Product.builder()
                .sku("KEYBOARD-001")
                .name("Keychron K2 Mechanical Keyboard")
                .description("Wireless mechanical keyboard")
                .availableQuantity(120)
                .reservedQuantity(0)
                .build();

            Product product8 = Product.builder()
                .sku("MONITOR-001")
                .name("LG 27-inch 4K Monitor")
                .description("4K UHD display with HDR support")
                .availableQuantity(30)
                .reservedQuantity(0)
                .build();

            Product product9 = Product.builder()
                .sku("CAMERA-001")
                .name("Canon EOS R6")
                .description("Mirrorless camera with 20MP sensor")
                .availableQuantity(25)
                .reservedQuantity(0)
                .build();

            Product product10 = Product.builder()
                .sku("SPEAKER-001")
                .name("Sonos One SL")
                .description("Wireless smart speaker")
                .availableQuantity(80)
                .reservedQuantity(0)
                .build();

            // Save all products
            productRepository.save(product1);
            productRepository.save(product2);
            productRepository.save(product3);
            productRepository.save(product4);
            productRepository.save(product5);
            productRepository.save(product6);
            productRepository.save(product7);
            productRepository.save(product8);
            productRepository.save(product9);
            productRepository.save(product10);

            log.info("✅ Database initialized with 10 sample products");
            log.info("Products: Laptops, Phones, Tablets, Watches, Headphones, Mice, Keyboards, Monitors, Cameras, Speakers");
        };
    }
}