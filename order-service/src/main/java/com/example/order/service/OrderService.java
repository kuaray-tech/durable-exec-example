package com.example.order.service;

import com.example.common.dto.OrderDTO;
import com.example.common.workflows.OrderWorkflow;
import com.example.order.entity.Order;
import com.example.order.repository.OrderRepository;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.workflow.Workflow;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * OrderService encapsulates persistence of orders and submission of the
 * corresponding Temporal workflow.  When a new order is created via the
 * REST API this service saves the order to the database and then uses
 * {@link WorkflowClient#newWorkflowStub(Class, WorkflowOptions)} to create
 * a stub for the {@link OrderWorkflow}.  It then starts the workflow
 * asynchronously via {@link io.temporal.client.WorkflowClient#start(Runnable, Object...)}.
 */
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final WorkflowClient workflowClient;

    /**
     * Name of the Temporal task queue on which the OrderWorkflow is
     * scheduled.  This allows workflow workers to be decoupled from
     * workflow clients.  See {@link com.example.order.worker.OrderWorkflowWorker}
     * for the worker implementation.
     */
    @Value("${temporal.order.taskQueue:ORDER_TASK_QUEUE}")
    private String orderTaskQueue;

    /**
     * Prefix used to construct workflow IDs.  This helps avoid collisions.
     */
    @Value("${temporal.order.workflowIdPrefix:order}")
    private String workflowIdPrefix;

    /**
     * Creates and persists a new order, then starts the corresponding
     * workflow using Temporal.  The returned entity includes the generated
     * primary key.
     *
     * @param order the order to create
     * @return the persisted order
     */
    public Order createOrder(Order order) {
        Order saved = orderRepository.save(order);
        // Map the entity to a DTO; this is deliberately simple.
        OrderDTO dto = new OrderDTO(saved.getId(), saved.getProductId(), saved.getPrice(), saved.getQuantity());

        // Generate a unique workflow ID by appending a timestamp to avoid conflicts
        String uniqueWorkflowId = workflowIdPrefix + "-" + saved.getId() + "-" + Instant.now().toEpochMilli();
        
        // Build workflow options specifying a unique workflow ID and the task queue.
        WorkflowOptions options = WorkflowOptions.newBuilder()
                .setWorkflowId(uniqueWorkflowId)
                .setTaskQueue(orderTaskQueue)
                .build();

        // Create a stub for the workflow and start it asynchronously.  Do not
        // call workflow methods directly on this stub outside of the Temporal
        // client context.
        OrderWorkflow workflow = workflowClient.newWorkflowStub(OrderWorkflow.class, options);
        WorkflowClient.start(workflow::placeOrder, dto);
        return saved;
    }
}