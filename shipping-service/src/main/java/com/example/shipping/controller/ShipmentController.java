package com.example.shipping.controller;

import com.example.shipping.entity.Shipment;
import com.example.shipping.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller that exposes endpoints to inspect shipment records.  Shipments
 * are created as part of the ShippingActivity; this controller simply
 * returns them.
 */
@RestController
@RequestMapping("/shipments")
@RequiredArgsConstructor
public class ShipmentController {
    private final ShipmentRepository shipmentRepository;

    @GetMapping
    public ResponseEntity<List<Shipment>> listShipments() {
        return ResponseEntity.ok(shipmentRepository.findAll());
    }
}