package com.example.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the order service.  In addition to starting the Spring
 * application context this class triggers construction of Temporal workers
 * defined via beans in the context.  The service exposes REST endpoints
 * implemented in {@link com.example.order.controller.OrderController}.
 */
@SpringBootApplication
public class OrderServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}