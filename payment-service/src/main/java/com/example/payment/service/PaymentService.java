package com.example.payment.service;

import com.example.payment.entity.Payment;
import com.example.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
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
     * @return the payment ID that can be used for refunds
     */
    public Long debit(Long orderId, Long productId, Double amount) {
        String externalId = UUID.randomUUID().toString();
        logger.info("Charging order {} for amount {} (externalId={})", orderId, amount, externalId);
        
        // Write to a file for debugging purposes
        writeToLogFile("PAYMENT_DEBIT", "Order: " + orderId + ", Amount: " + amount + ", ExternalId: " + externalId);
        
        Payment payment = Payment.builder()
                .orderId(orderId)
                .productId(productId)
                .amount(amount)
                .externalId(externalId)
                .build();
        Payment savedPayment = paymentRepository.save(payment);
        return savedPayment.getId();
    }
    
    /**
     * Refunds a payment previously made. In a real system, this would call an external
     * payment processor to issue a refund. Here we simulate the refund by logging it.
     *
     * @param paymentId the ID of the payment to refund
     */
    public void refund(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));
        
        String refundId = UUID.randomUUID().toString();
        logger.info("Refunding payment {} for amount {} (refundId={})", 
                paymentId, payment.getAmount(), refundId);
        
        // Write to a file for debugging purposes
        writeToLogFile("PAYMENT_REFUND", "Payment: " + paymentId + 
                ", Amount: " + payment.getAmount() + 
                ", RefundId: " + refundId + 
                ", OrderId: " + payment.getOrderId());
        
        // In a real system, you might update the payment status or create a refund record
        payment.setRefunded(true);
        payment.setRefundId(refundId);
        paymentRepository.save(payment);
    }
    
    /**
     * Writes a log entry to a file for debugging purposes
     */
    private void writeToLogFile(String action, String details) {
        try (PrintWriter writer = new PrintWriter(new FileWriter("/tmp/saga_payment_log.txt", true))) {
            writer.println(LocalDateTime.now() + " - " + action + " - " + details);
        } catch (IOException e) {
            logger.error("Failed to write to log file", e);
        }
    }
}