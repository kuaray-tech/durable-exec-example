package com.example.payment.service;

import com.example.payment.entity.Payment;
import com.example.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Business service encapsulating payment processing.  In a real system this
 * would call an external payment processor such as Stripe or PayPal.  Here
 * we simulate the call by generating a UUID to represent the external
 * transaction ID and then persist the payment via JPA.
 */
@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;

    /**
     * Debits the customer's payment method for the given amount and records
     * the transaction.  A random UUID is used to simulate the external
     * payment ID.  This method is idempotent because a duplicate payment
     * would result in a separate row with a new external ID.  To avoid
     * duplicate charges in production you would perform idempotency checks.
     *
     * @param orderId   the identifier of the order being charged
     * @param productId the product being purchased
     * @param amount    the total charge amount
     * @return the persisted payment entity
     */
    public Payment debit(Long orderId, Long productId, Double amount) {
        String externalId = UUID.randomUUID().toString();
        logger.info("Charging order {} for amount {} (externalId={})", orderId, amount, externalId);
        Payment payment = Payment.builder()
                .orderId(orderId)
                .productId(productId)
                .amount(amount)
                .externalId(externalId)
                .build();
        return paymentRepository.save(payment);
    }
}