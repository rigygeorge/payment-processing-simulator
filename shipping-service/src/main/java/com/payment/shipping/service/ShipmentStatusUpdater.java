package com.payment.shipping.service;

import com.payment.shipping.model.Shipment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Scheduled job to simulate shipment status updates
 * Runs every 30 seconds and progresses shipments through their lifecycle
 * 
 * Location: shipping-service/src/main/java/com/payment/shipping/service/ShipmentStatusUpdater.java
 */
@Component
@EnableScheduling
@Slf4j
public class ShipmentStatusUpdater {

    private final ShippingService shippingService;

    public ShipmentStatusUpdater(ShippingService shippingService) {
        this.shippingService = shippingService;
    }

    /**
     * Scheduled job runs every 30 seconds
     * Simulates shipment progression: CREATED → IN_TRANSIT → OUT_FOR_DELIVERY → DELIVERED
     */
    @Scheduled(fixedDelay = 30000) // 30 seconds
    public void updateShipmentStatuses() {
        try {
            List<Shipment> inProgressShipments = shippingService.getInProgressShipments();

            if (inProgressShipments.isEmpty()) {
                log.debug("No in-progress shipments to update");
                return;
            }

            log.info("╔═══════════════════════════════════════════════════╗");
            log.info("║  🔄 SCHEDULED JOB: Updating Shipment Statuses    ║");
            log.info("╚═══════════════════════════════════════════════════╝");
            log.info("Found {} in-progress shipments", inProgressShipments.size());

            for (Shipment shipment : inProgressShipments) {
                log.info("───────────────────────────────────────────────────");
                log.info("Processing Shipment ID: {}", shipment.getId());
                log.info("Current Status: {}", shipment.getStatus());
                log.info("Tracking Number: {}", shipment.getTrackingNumber());

                // Update status
                shippingService.updateShipmentStatus(shipment.getId());
            }

            log.info("╔═══════════════════════════════════════════════════╗");
            log.info("║  ✅ Scheduled Job Complete                        ║");
            log.info("╚═══════════════════════════════════════════════════╝\n");

        } catch (Exception e) {
            log.error("❌ Error in scheduled shipment status update: {}", e.getMessage(), e);
        }
    }

    /**
     * Initial delay before first execution
     * Gives time for services to start up
     */
    @Scheduled(initialDelay = 60000, fixedDelay = Long.MAX_VALUE)
    public void logScheduledJobStart() {
        log.info("🚀 Shipment Status Updater scheduled job is active");
        log.info("   Running every 30 seconds to update shipment statuses");
    }
}