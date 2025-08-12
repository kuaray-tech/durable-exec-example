package com.example.common.activities;

import com.example.common.dto.OrderDTO;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

/**
 * ShippingActivity encapsulates the logic for shipping goods to the customer.
 * In this simplified example the Activity just logs the shipment, but in a
 * production system it might call a fulfilment microservice or external
 * warehouse API.  Like any Temporal Activity, it is safe to call external
 * systems here because retries and failure handling are managed by Temporal.
 */
@ActivityInterface
public interface ShippingActivity {

    /**
     * Ships the items associated with the given order.  Temporal will
     * automatically retry this Activity if it throws an exception, according
     * to configured retry policies.
     *
     * @param order the order to ship
     */
    @ActivityMethod
    void shipOrder(OrderDTO order);
}