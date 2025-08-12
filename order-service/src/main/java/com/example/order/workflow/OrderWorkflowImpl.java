package com.example.order.workflow;

import com.example.common.activities.PaymentActivity;
import com.example.common.activities.ShippingActivity;
import com.example.common.dto.OrderDTO;
import com.example.common.workflows.OrderWorkflow;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;
import java.time.Duration;

/**
 * Implementation of the {@link OrderWorkflow}.  Because workflows must be
 * deterministic, this class does not have any dependencies injected via
 * Spring.  Instead, it creates Activity stubs using the Temporal API and
 * calls them as part of the workflow logic.  Any exceptions thrown from
 * Activities will cause the workflow to retry the Activity according to the
 * configured retry policy.
 */
public class OrderWorkflowImpl implements OrderWorkflow {

    private final PaymentActivity paymentActivity;
    private final ShippingActivity shippingActivity;

    public OrderWorkflowImpl() {
        // Configure Activity options such as timeouts and retry policies.  These
        // settings control how Activities behave in the face of failures.
        RetryOptions retryOptions = RetryOptions.newBuilder()
                .setMaximumAttempts(3)
                .setInitialInterval(Duration.ofSeconds(2))
                .setBackoffCoefficient(2.0)
                .build();

        ActivityOptions options = ActivityOptions.newBuilder()
                .setStartToCloseTimeout(Duration.ofMinutes(1))
                .setRetryOptions(retryOptions)
                .build();

        // Create stubs for the Activities.  When the workflow calls a method
        // on these stubs the corresponding Activity implementation will run in
        // a separate worker (potentially in another microservice).
        this.paymentActivity = Workflow.newActivityStub(PaymentActivity.class, options);
        this.shippingActivity = Workflow.newActivityStub(ShippingActivity.class, options);
    }

    @Override
    public void placeOrder(OrderDTO order) {
        // Charge the customer.  If this fails the Activity will automatically
        // retry according to the retry options specified above.
        paymentActivity.debitPayment(order);

        // Ship the goods.  Again, this call is durable and will be retried if
        // necessary.
        shippingActivity.shipOrder(order);
    }
}