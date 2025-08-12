package com.example.shipping;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the shipping service.  This service runs a Temporal worker
 * that handles shipping activities and exposes a REST API for inspection of
 * recorded shipments.
 */
@SpringBootApplication
public class ShippingServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ShippingServiceApplication.class, args);
    }
}