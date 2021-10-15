package com.nadir.estore.ordersservice.command.commands;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Value
@AllArgsConstructor
public class RejectOrderCommand {

    @TargetAggregateIdentifier
    private final String orderId;
    private final String reason;

}
