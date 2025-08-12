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
     */
    @ActivityMethod
    void debitPayment(OrderDTO order);
}