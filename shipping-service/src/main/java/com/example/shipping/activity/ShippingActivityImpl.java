package com.example.shipping.activity;

import com.example.common.activities.ShippingActivity;
import com.example.common.dto.OrderDTO;
import com.example.shipping.service.ShippingService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;

/**
 * Implementation of the {@link ShippingActivity}.  Delegates to the
 * {@link ShippingService} to create a shipment record.  This Activity
 * executes outside of the workflow thread and can perform blocking I/O.
 */
@Component
@RequiredArgsConstructor
public class ShippingActivityImpl implements ShippingActivity {

    private static final Logger logger = LoggerFactory.getLogger(ShippingActivityImpl.class);
    private final ShippingService shippingService;

    @Override
    public void shipOrder(OrderDTO order) {
        logger.info("[SAGA Shipping] Starting shipping process for order {} (productId: {})", 
                order.getOrderId(), order.getProductId());
        
        writeToLogFile("SHIPPING_START", "Order: " + order.getOrderId() + ", ProductId: " + order.getProductId());
        
        // Simulate a failure for testing the Saga pattern's compensation mechanism
        // Comment out this block to test the normal flow
        if (order.getProductId() == 999) {
            logger.error("[SAGA Shipping] Simulating a shipping failure for product ID 999 in order {}", 
                    order.getOrderId());
            
            writeToLogFile("SHIPPING_FAILURE", "Order: " + order.getOrderId() + 
                    ", ProductId: " + order.getProductId() + 
                    ", Reason: Simulated failure for testing Saga compensation");
            
            throw new RuntimeException("Simulated shipping failure for testing Saga compensation");
        }
        
        try {
            shippingService.ship(order.getOrderId(), order.getProductId(), order.getQuantity());
            logger.info("[SAGA Shipping] Successfully shipped order {} (productId: {})", 
                    order.getOrderId(), order.getProductId());
            
            writeToLogFile("SHIPPING_SUCCESS", "Order: " + order.getOrderId() + 
                    ", ProductId: " + order.getProductId());
        } catch (Exception e) {
            logger.error("[SAGA Shipping] Failed to ship order {} (productId: {}). Error: {}", 
                    order.getOrderId(), order.getProductId(), e.getMessage());
            
            writeToLogFile("SHIPPING_ERROR", "Order: " + order.getOrderId() + 
                    ", ProductId: " + order.getProductId() + 
                    ", Error: " + e.getMessage());
            
            throw e;
        }
    }
    
    /**
     * Writes a log entry to a file for debugging purposes
     */
    private void writeToLogFile(String action, String details) {
        try (PrintWriter writer = new PrintWriter(new FileWriter("/tmp/saga_shipping_log.txt", true))) {
            writer.println(LocalDateTime.now() + " - " + action + " - " + details);
        } catch (IOException e) {
            logger.error("Failed to write to log file", e);
        }
    }
}