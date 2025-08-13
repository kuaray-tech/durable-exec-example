package com.example.payment.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Entity representing a payment transaction.  It stores a reference to the
 * order being charged as well as a generated external payment ID.  In a
 * production system this would map to a payment processor transaction.
 */
@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long orderId;

    private Long productId;

    private Double amount;

    private String externalId;
    
    /**
     * Flag indicating whether this payment has been refunded.
     * Used for compensation in the Saga pattern.
     */
    private boolean refunded;
    
    /**
     * External ID for the refund transaction.
     * Only populated if the payment has been refunded.
     */
    private String refundId;
}