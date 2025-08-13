package com.example.common.activities;

import com.example.common.dto.OrderDTO;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

/**
 * PaymentActivity represents the external side effect of debiting the
 * customer's payment method.  Activities are the only place where
 * non‑deterministic operations (such as calling external services, databases
 * or generating random numbers) should occur when using Temporal.
 */
@ActivityInterface
public interface PaymentActivity {

    /**
     * Performs a debit operation for the given order.  If the underlying
     * payment provider is unavailable or returns a transient error, Temporal
     * will retry the Activity automatically according to its configured
     * {@link io.temporal.common.RetryOptions}.
     *
     * @param order information about the order to charge
     * @return the payment ID that can be used for refunds if needed
     */
    @ActivityMethod
    Long debitPayment(OrderDTO order);
    
    /**
     * Refunds a payment previously made. This is used as a compensating
     * transaction when a subsequent step in the workflow fails and we need
     * to maintain data consistency.
     *
     * @param paymentId the ID of the payment to refund
     */
    @ActivityMethod
    void refundPayment(Long paymentId);
}