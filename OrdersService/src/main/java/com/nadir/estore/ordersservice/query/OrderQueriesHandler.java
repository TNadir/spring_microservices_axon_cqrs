package com.nadir.estore.ordersservice.query;

import com.nadir.estore.ordersservice.core.data.OrderEntity;
import com.nadir.estore.ordersservice.core.data.OrdersRepository;
import com.nadir.estore.ordersservice.core.model.OrderSummary;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderQueriesHandler {

    private final OrdersRepository ordersRepository;

    @Autowired
    public OrderQueriesHandler(OrdersRepository ordersRepository) {
        this.ordersRepository = ordersRepository;
    }

    @QueryHandler
    public OrderSummary findOrder(FindOrderQuery findOrderQuery) {
        OrderEntity orderEntity = ordersRepository.findByOrderId(findOrderQuery.getOrderId());
        return new OrderSummary(orderEntity.getOrderId(), orderEntity.getOrderStatus());
    }
}
