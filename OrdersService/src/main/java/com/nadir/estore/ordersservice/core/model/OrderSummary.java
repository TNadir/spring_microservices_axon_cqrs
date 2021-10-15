package com.nadir.estore.ordersservice.core.model;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class OrderSummary {

    private final String orderId;
    private final OrderStatus orderStatus;
    private final String reason;

    public OrderSummary(String orderId, OrderStatus orderStatus) {
        this.orderId = orderId;
        this.orderStatus = orderStatus;
        this.reason = "";
    }
}
