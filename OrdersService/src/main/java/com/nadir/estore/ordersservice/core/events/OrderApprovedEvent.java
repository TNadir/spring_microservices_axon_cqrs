package com.nadir.estore.ordersservice.core.events;

import com.nadir.estore.ordersservice.core.model.OrderStatus;
import lombok.Value;

@Value
public class OrderApprovedEvent {
    private final String orderId;
    private final OrderStatus orderStatus = OrderStatus.APPROVED;
}
