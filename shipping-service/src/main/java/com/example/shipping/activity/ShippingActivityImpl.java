package com.example.shipping.activity;

import com.example.common.activities.ShippingActivity;
import com.example.common.dto.OrderDTO;
import com.example.shipping.service.ShippingService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Implementation of the {@link ShippingActivity}.  Delegates to the
 * {@link ShippingService} to create a shipment record.  This Activity
 * executes outside of the workflow thread and can perform blocking I/O.
 */
@Component
@RequiredArgsConstructor
public class ShippingActivityImpl implements ShippingActivity {

    private static final Logger logger = LoggerFactory.getLogger(ShippingActivityImpl.class);
    private final ShippingService shippingService;

    @Override
    public void shipOrder(OrderDTO order) {
        logger.info("[ShippingActivity] Shipping order {}", order.getOrderId());
        shippingService.ship(order.getOrderId(), order.getProductId(), order.getQuantity());
    }
}