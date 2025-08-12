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
    public void debitPayment(OrderDTO order) {
        // Compute the total amount.  Use Activity.getExecutionContext().getInfo() to log progress.
        double amount = order.getPrice() * order.getQuantity();
        logger.info("[PaymentActivity] Processing order {} for amount {}", order.getOrderId(), amount);
        paymentService.debit(order.getOrderId(), order.getProductId(), amount);
        // Simulate a potential transient failure for demonstration.  You could
        // uncomment the following lines to randomly throw an exception and
        // observe automatic retries:
        // if (Math.random() < 0.2) {
        //     throw new RuntimeException("Simulated payment failure");
        // }
    }
}