package com.example.payment.activity;

import com.example.common.activities.PaymentActivity;
import com.example.common.dto.OrderDTO;
import com.example.payment.service.PaymentService;
import io.temporal.activity.Activity;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Implementation of the {@link PaymentActivity}.  This class delegates to
 * {@link PaymentService} to perform the actual debit and persists the
 * payment.  It may throw runtime exceptions to signal failure and trigger
 * retries.  Because Activities run outside of the Temporal workflow thread
 * they are allowed to call external services and perform blocking I/O.
 */
@Component
@RequiredArgsConstructor
public class PaymentActivityImpl implements PaymentActivity {

    private static final Logger logger = LoggerFactory.getLogger(PaymentActivityImpl.class);

    private final PaymentService paymentService;

    @Override
    public Long debitPayment(OrderDTO order) {
        // Compute the total amount.  Use Activity.getExecutionContext().getInfo() to log progress.
        double amount = order.getPrice() * order.getQuantity();
        logger.info("[SAGA Payment] Processing payment for order {} (productId: {}) for amount {}", 
                order.getOrderId(), order.getProductId(), amount);
        Long paymentId = paymentService.debit(order.getOrderId(), order.getProductId(), amount);
        logger.info("[SAGA Payment] Successfully processed payment ID {} for order {}", 
                paymentId, order.getOrderId());
        // Simulate a potential transient failure for demonstration.  You could
        // uncomment the following lines to randomly throw an exception and
        // observe automatic retries:
        // if (Math.random() < 0.2) {
        //     throw new RuntimeException("Simulated payment failure");
        // }
        return paymentId;
    }
    
    @Override
    public void refundPayment(Long paymentId) {
        logger.info("[SAGA Compensation] Starting refund process for payment {}", paymentId);
        try {
            paymentService.refund(paymentId);
            logger.info("[SAGA Compensation] Successfully refunded payment {}", paymentId);
        } catch (Exception e) {
            logger.error("[SAGA Compensation] Failed to refund payment {}. Error: {}", 
                    paymentId, e.getMessage());
            throw e;
        }
    }
}