# Durable Execution Example with Temporal.io and Saga Pattern

This project demonstrates the implementation of durable executions using the [Temporal.io](https://temporal.io/) framework in a microservices architecture. It showcases the Saga pattern for distributed transactions with compensating actions to maintain data consistency across services.

## Project Overview

The application simulates an e-commerce order processing system with three microservices:

- **Order Service**: Initiates and orchestrates the order workflow
- **Payment Service**: Handles payment processing (debit and refund)
- **Shipping Service**: Manages order shipment

The system uses Temporal.io to ensure that workflows are durable and can recover from failures, making it ideal for implementing the Saga pattern.

## Key Features Demonstrated

### 1. Durable Workflow Execution

Temporal.io provides durable execution guarantees, ensuring that workflows continue from where they left off after failures or service restarts.

**Key Implementation**: `OrderWorkflowImpl.java`

```java
@WorkflowImplementation
public class OrderWorkflowImpl implements OrderWorkflow {
    // Workflow state is automatically persisted by Temporal
    // Even if the service crashes, execution will resume from the last checkpoint
    @Override
    public void processOrder(OrderDTO order) {
        // Workflow implementation with durable execution guarantees
    }
}
```

### 2. Saga Pattern Implementation

The Saga pattern is implemented to maintain data consistency across distributed services by using compensating transactions when failures occur.

**Key Implementation**: `OrderWorkflowImpl.java`

```java
@Override
public void processOrder(OrderDTO order) {
    logger.info("=== SAGA START: Processing order {} with productId {} ===", 
            order.getOrderId(), order.getProductId());
    
    // Step 1: Process payment
    logger.info("SAGA Step 1: Processing payment for order {}", order.getOrderId());
    Long paymentId = paymentActivity.debitPayment(order);
    
    try {
        // Step 2: Ship order
        logger.info("SAGA Step 2: Shipping order {}", order.getOrderId());
        shippingActivity.shipOrder(order);
        
        logger.info("=== SAGA COMPLETE: Order {} processed successfully ===", order.getOrderId());
    } catch (Exception e) {
        // Compensation logic for Saga pattern
        logger.error("=== SAGA COMPENSATION: Order {} failed, initiating compensation ===", 
                order.getOrderId());
        logger.info("SAGA Compensation: Refunding payment {} for order {}", 
                paymentId, order.getOrderId());
        
        // Compensating transaction to maintain consistency
        paymentActivity.refundPayment(paymentId);
        
        logger.info("=== SAGA COMPENSATION COMPLETE: Order {} ===", order.getOrderId());
        throw e;
    }
}
```

### 3. Service Orchestration

Temporal.io orchestrates the workflow across multiple services using activities.

**Key Files**:
- Activity interfaces: `PaymentActivity.java`, `ShippingActivity.java`
- Activity stubs in workflow: `OrderWorkflowImpl.java`

```java
// Activity stubs in OrderWorkflowImpl
@ActivityStub
private final PaymentActivity paymentActivity;

@ActivityStub
private final ShippingActivity shippingActivity;

// Activity calls that orchestrate across services
Long paymentId = paymentActivity.debitPayment(order);
shippingActivity.shipOrder(order);
```

### 4. Failure Handling

The system demonstrates how to handle failures and trigger compensating transactions.

**Simulated Failure**: `ShippingActivityImpl.java`
```java
// Simulate a failure for testing the Saga pattern's compensation mechanism
if (order.getProductId() == 999) {
    logger.error("[SAGA Shipping] Simulating a shipping failure for product ID 999 in order {}", 
            order.getOrderId());
    throw new RuntimeException("Simulated shipping failure for testing Saga compensation");
}
```

**Compensation Logic**: `PaymentService.java`
```java
public void refund(Long paymentId) {
    Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));
    
    String refundId = UUID.randomUUID().toString();
    logger.info("Refunding payment {} for amount {} (refundId={})", 
            paymentId, payment.getAmount(), refundId);
    
    // In a real system, you might update the payment status or create a refund record
    payment.setRefunded(true);
    payment.setRefundId(refundId);
    paymentRepository.save(payment);
}
```

### 5. Microservice Architecture

The project demonstrates how to integrate Temporal.io with Spring Boot microservices.

**Service Applications**:
- `OrderServiceApplication.java`
- `PaymentServiceApplication.java`
- `ShippingServiceApplication.java`

**Temporal Workers**:
- `OrderWorkflowWorker.java`
- `PaymentWorker.java`
- `ShippingWorker.java`

## Technical Stack

- **Java 21**: Latest LTS version
- **Spring Boot**: Microservices framework
- **Temporal.io**: Durable workflow engine
- **Maven**: Dependency management and build tool
- **H2 Database**: In-memory database for persistence
- **Docker**: Container for running Temporal server

## Project Structure

```
durable-exec-example/
├── common/                  # Shared DTOs and interfaces
├── order-service/           # Order management service
├── payment-service/         # Payment processing service
├── shipping-service/        # Shipping management service
└── docker-compose.yml       # Docker setup for Temporal server
```

## Running the Application

### Prerequisites

- Java 21
- Maven
- Docker and Docker Compose

### Setup Steps

1. **Start Temporal Server**:
   ```
   docker-compose up -d
   ```

2. **Build the Project**:
   ```
   mvn clean install
   ```

3. **Start the Microservices**:
   ```
   # In separate terminals
   cd order-service && mvn spring-boot:run
   cd payment-service && mvn spring-boot:run
   cd shipping-service && mvn spring-boot:run
   ```

### Testing the Saga Pattern

1. **Normal Flow (Successful Order)**:
   ```
   curl -X POST http://localhost:8081/orders -H "Content-Type: application/json" -d '{"productId": 123, "price": 99.99, "quantity": 1}'
   ```

2. **Failure Flow (Compensation)**:
   ```
   curl -X POST http://localhost:8081/orders -H "Content-Type: application/json" -d '{"productId": 999, "price": 99.99, "quantity": 1}'
   ```
   This will trigger the simulated failure in the shipping service and activate the compensation mechanism to refund the payment.

3. **Verify Workflow Execution**:
   - Check the application logs for each service
   - Check the file-based logs at `/tmp/saga_payment_log.txt` and `/tmp/saga_shipping_log.txt`
   - Access the Temporal UI at `http://localhost:8233` to view workflow executions

## Key Benefits of This Architecture

1. **Reliability**: Workflows continue execution even after service failures or restarts
2. **Consistency**: The Saga pattern ensures data consistency across services
3. **Visibility**: Temporal provides visibility into workflow execution and history
4. **Scalability**: Services can scale independently while maintaining workflow integrity
5. **Maintainability**: Clear separation of concerns between workflow orchestration and business logic

## Advanced Features

### Enhanced Logging

The project includes enhanced logging to track the Saga pattern execution and compensation:

- **Console Logging**: Detailed logs in each service's console output
- **File-Based Logging**: Additional logs written to `/tmp/saga_payment_log.txt` and `/tmp/saga_shipping_log.txt`

### Unique Workflow IDs

Each order creates a unique workflow ID to avoid conflicts:

```java
// In OrderService.java
String workflowId = ORDER_WORKFLOW_ID_PREFIX + order.getId() + "-" + System.currentTimeMillis();
```

## Conclusion

This project demonstrates how to implement reliable distributed transactions using the Saga pattern with Temporal.io's durable execution framework. It provides a practical example of maintaining data consistency across microservices in the face of failures.

## References

- [Temporal.io Documentation](https://docs.temporal.io/)
- [Saga Pattern](https://microservices.io/patterns/data/saga.html)
- [Spring Boot](https://spring.io/projects/spring-boot)
