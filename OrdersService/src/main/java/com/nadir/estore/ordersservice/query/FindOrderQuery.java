package com.nadir.estore.ordersservice.query;

import lombok.Value;

@Value
public class FindOrderQuery {
    private final String orderId;
}
