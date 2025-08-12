package com.example.shipping.service;

import com.example.shipping.entity.Shipment;
import com.example.shipping.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Business service that handles shipping operations.  A real system would
 * integrate with a fulfilment provider or logistics API.  Here we simply
 * generate a tracking number and persist the shipment.
 */
@Service
@RequiredArgsConstructor
public class ShippingService {
    private static final Logger logger = LoggerFactory.getLogger(ShippingService.class);
    private final ShipmentRepository shipmentRepository;

    /**
     * Ships the given order by recording a shipment.  Generates a random
     * tracking number to simulate integration with a shipping carrier.
     *
     * @param orderId   the identifier of the order being shipped
     * @param productId the product being shipped
     * @param quantity  quantity of items to ship
     * @return the persisted shipment entity
     */
    public Shipment ship(Long orderId, Long productId, Integer quantity) {
        String trackingNumber = UUID.randomUUID().toString().substring(0, 8);
        logger.info("Shipping order {} with tracking number {}", orderId, trackingNumber);
        Shipment shipment = Shipment.builder()
                .orderId(orderId)
                .productId(productId)
                .quantity(quantity)
                .trackingNumber(trackingNumber)
                .build();
        return shipmentRepository.save(shipment);
    }
}