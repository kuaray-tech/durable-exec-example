package com.example.order.workflow;

import com.example.common.activities.PaymentActivity;
import com.example.common.activities.ShippingActivity;
import com.example.common.dto.OrderDTO;
import com.example.common.workflows.OrderWorkflow;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;
import org.slf4j.Logger;
import java.time.Duration;

/**
 * Implementation of the {@link OrderWorkflow}.  Because workflows must be
 * deterministic, this class does not have any dependencies injected via
 * Spring.  Instead, it creates Activity stubs using the Temporal API and
 * calls them as part of the workflow logic.  Any exceptions thrown from
 * Activities will cause the workflow to retry the Activity according to the
 * configured retry policy.
 * 
 * This implementation uses the Saga pattern to ensure data consistency across
 * distributed services. If any step fails after a successful payment, the
 * payment will be refunded as a compensating transaction.
 */
public class OrderWorkflowImpl implements OrderWorkflow {

    private final PaymentActivity paymentActivity;
    private final ShippingActivity shippingActivity;
    private final Logger logger = Workflow.getLogger(OrderWorkflowImpl.class);

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
        logger.info("=== SAGA START: Processing order {} with productId {} ===", order.getOrderId(), order.getProductId());
        Long paymentId = null;
        try {
            // Step 1: Process payment
            logger.info("SAGA Step 1: Processing payment for order {}", order.getOrderId());
            paymentId = paymentActivity.debitPayment(order);
            logger.info("SAGA Step 1 Complete: Payment processed successfully with ID: {}", paymentId);
            
            try {
                // Step 2: Ship the order
                logger.info("SAGA Step 2: Shipping order {}", order.getOrderId());
                shippingActivity.shipOrder(order);
                logger.info("SAGA Step 2 Complete: Order {} shipped successfully", order.getOrderId());
            } catch (Exception e) {
                // Compensating transaction: If shipping fails, refund the payment
                logger.error("SAGA Compensation Triggered: Shipping failed for order {}. Error: {}", order.getOrderId(), e.getMessage());
                if (paymentId != null) {
                    logger.info("SAGA Compensation Action: Initiating refund for payment {}", paymentId);
                    paymentActivity.refundPayment(paymentId);
                    logger.info("SAGA Compensation Complete: Payment {} refunded due to shipping failure", paymentId);
                }
                throw e; // Re-throw to mark workflow as failed
            }
        } catch (Exception e) {
            // Handle overall workflow failure
            logger.error("SAGA Failed: Order workflow failed for order {}. Error: {}", order.getOrderId(), e.getMessage());
            throw e;
        }
        
        logger.info("=== SAGA COMPLETE: Order {} processed successfully ===", order.getOrderId());
    }
}