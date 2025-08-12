package com.example.order.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * Simple JPA entity representing an order.  This entity is stored in an
 * in‑memory H2 database.  Only a few fields are defined for demonstration
 * purposes.  In a real application you would likely include customer
 * information, timestamps and other metadata.
 */
@Entity
@Table(name = "orders")
@Data
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productId;

    private Double price;

    private Integer quantity;
}