package com.example.common.workflows;

import com.example.common.dto.OrderDTO;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

/**
 * OrderWorkflow defines the high‑level business process for fulfilling an
 * order.  A workflow orchestrates calls to Activities (which perform side
 * effects such as charging a credit card or shipping items) and can make
 * decisions based on their results.  Workflows are persisted and can
 * survive crashes and restarts without losing progress.
 */
@WorkflowInterface
public interface OrderWorkflow {

    /**
     * Processes an order by charging payment and shipping the goods.  In a
     * more realistic scenario the workflow could include additional steps
     * such as reserving inventory, sending emails or handling failures via
     * compensation.  The method must not call external services directly;
     * instead, it calls Activities which are executed outside the
     * deterministic workflow context.
     *
     * @param order data about the order to process
     */
    @WorkflowMethod
    void placeOrder(OrderDTO order);
}