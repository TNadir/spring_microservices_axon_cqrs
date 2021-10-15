package com.nadir.estore.ordersservice.saga;

import com.nadir.estore.core.commands.CancelProductReservationCommand;
import com.nadir.estore.core.commands.ProcessPaymentCommand;
import com.nadir.estore.core.commands.ReserveProductCommand;
import com.nadir.estore.core.events.PaymentProcessedEvent;
import com.nadir.estore.core.events.ProductReservationCanceledEvent;
import com.nadir.estore.core.events.ProductReservedEvent;
import com.nadir.estore.core.model.User;
import com.nadir.estore.core.query.FetchUserPaymentDetailsQuery;
import com.nadir.estore.ordersservice.command.commands.ApproveOrderCommand;
import com.nadir.estore.ordersservice.command.commands.RejectOrderCommand;
import com.nadir.estore.ordersservice.core.events.OrderApprovedEvent;
import com.nadir.estore.ordersservice.core.events.OrderCreatedEvent;
import com.nadir.estore.ordersservice.core.events.OrderRejectedEvent;
import com.nadir.estore.ordersservice.core.model.OrderSummary;
import com.nadir.estore.ordersservice.query.FindOrderQuery;
import org.axonframework.commandhandling.CommandCallback;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.commandhandling.CommandResultMessage;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.deadline.DeadlineManager;
import org.axonframework.deadline.annotation.DeadlineHandler;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.queryhandling.QueryUpdateEmitter;
import org.axonframework.spring.stereotype.Saga;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Saga
public class OrderSaga {

    public static final String PROCESS_PAYMENT_DEADLINE = "process-payment-deadline";

    private final Logger LOGGER = LoggerFactory.getLogger(OrderSaga.class);

    private String scheduleId = null;

    private final transient CommandGateway commandGateway;

    private final transient QueryGateway queryGateway;

    private final transient QueryUpdateEmitter queryUpdateEmitter;

    private final transient DeadlineManager deadlineManager;

    @Autowired
    public OrderSaga(CommandGateway commandGateway, QueryGateway queryGateway, QueryUpdateEmitter queryUpdateEmitter, DeadlineManager deadlineManager) {
        this.commandGateway = commandGateway;
        this.queryGateway = queryGateway;
        this.queryUpdateEmitter = queryUpdateEmitter;
        this.deadlineManager = deadlineManager;
    }

    @StartSaga
    @SagaEventHandler(associationProperty = "orderId")
    public void handle(OrderCreatedEvent orderCreatedEvent) {

        ReserveProductCommand reserveProductCommand = ReserveProductCommand.builder()
                .productId(orderCreatedEvent.getProductId())
                .orderId(orderCreatedEvent.getOrderId())
                .quantity(orderCreatedEvent.getQuantity())
                .userId(orderCreatedEvent.getUserId())
                .build();

        LOGGER.info("OrderCreatedEvent is published: " + orderCreatedEvent);

        commandGateway.send(reserveProductCommand, new CommandCallback<ReserveProductCommand, Object>() {

            @Override
            public void onResult(CommandMessage<? extends ReserveProductCommand> commandMessage, CommandResultMessage<?> commandResultMessage) {
                //Start a compensating transaction
                LOGGER.info("Failed ReserveProductCommand");
            }
        });
    }


    @SagaEventHandler(associationProperty = "orderId")
    public void handle(ProductReservedEvent productReservedEvent) {

        LOGGER.info("ProductReservedEvent is published: " + productReservedEvent);

        FetchUserPaymentDetailsQuery fetchUserPaymentDetailsQuery =
                new FetchUserPaymentDetailsQuery(productReservedEvent.getUserId());

        User userPaymentDetails = null;

        try {
            userPaymentDetails = queryGateway.query(fetchUserPaymentDetailsQuery, ResponseTypes.instanceOf(User.class))
                    .join();
        } catch (Exception exception) {
            LOGGER.info("An exception occurred while fetching user payment details");
            //Start compensation transaction

            cancelProductReservation(productReservedEvent, "Unable fetching payment details!");
            return;
        }

        if (userPaymentDetails == null) {
            LOGGER.info("No user payment details found");
            //Start compensation transaction

            cancelProductReservation(productReservedEvent, "No user payment details found");
            return;
        }

        LOGGER.info("Successfully fetched user payment details: " + userPaymentDetails);


        scheduleId = deadlineManager.schedule(Duration.of(120, ChronoUnit.SECONDS),
                PROCESS_PAYMENT_DEADLINE, productReservedEvent);



        ProcessPaymentCommand processPaymentCommand = ProcessPaymentCommand.builder()
                .paymentId(UUID.randomUUID().toString())
                .orderId(productReservedEvent.getOrderId())
                .paymentDetails(userPaymentDetails.getPaymentDetails())
                .build();

        String result = null;
        try {

            result = commandGateway.sendAndWait(processPaymentCommand, 10, TimeUnit.SECONDS);

        } catch (Exception exception) {
            LOGGER.error(exception.getMessage());
            cancelProductReservation(productReservedEvent, "Unable process payment!");
            return;
        }

        if (result == null) {
            LOGGER.info("Failed Process Payment ...");
            //Start compensation transaction
            cancelProductReservation(productReservedEvent, "No process payment found!");
        }

    }

    private void cancelProductReservation(ProductReservedEvent productReservedEvent, String reason) {

        CancelProductReservationCommand cancelProductReservationCommand = CancelProductReservationCommand.builder()
                .productId(productReservedEvent.getProductId())
                .orderId(productReservedEvent.getOrderId())
                .quantity(productReservedEvent.getQuantity())
                .userId(productReservedEvent.getUserId())
                .reason(reason)
                .build();

        commandGateway.send(cancelProductReservationCommand);
        canceDeadline();
    }


    @SagaEventHandler(associationProperty = "orderId")
    public void handle(PaymentProcessedEvent paymentProcessedEvent) {

        canceDeadline();
        ApproveOrderCommand approveOrderCommand = new ApproveOrderCommand(paymentProcessedEvent.getOrderId());
        commandGateway.send(approveOrderCommand);

    }

    private void canceDeadline() {
        if(scheduleId != null) {
            deadlineManager.cancelSchedule(PROCESS_PAYMENT_DEADLINE, scheduleId);
            scheduleId = null;
        }
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "orderId")
    public void handle(OrderApprovedEvent orderApprovedEvent) {

        LOGGER.info("Order is approved: " + orderApprovedEvent);
        queryUpdateEmitter.emit(FindOrderQuery.class,
                                q -> true,
                                new OrderSummary(orderApprovedEvent.getOrderId(),
                                                 orderApprovedEvent.getOrderStatus()));
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void handle(ProductReservationCanceledEvent productReservationCanceledEvent) {

        RejectOrderCommand rejectOrderCommand = new RejectOrderCommand(productReservationCanceledEvent.getOrderId(),
                productReservationCanceledEvent.getReason());

        commandGateway.send(rejectOrderCommand);
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "orderId")
    public void handle(OrderRejectedEvent orderRejectedEvent) {
        LOGGER.info("Order was successfully rejected");

        queryUpdateEmitter.emit(FindOrderQuery.class,
                q -> true,
                new OrderSummary(orderRejectedEvent.getOrderId(),
                                 orderRejectedEvent.getOrderStatus(),
                                 orderRejectedEvent.getReason()));
    }

    @DeadlineHandler(deadlineName = PROCESS_PAYMENT_DEADLINE)
    public void handlePaymentDeadline(ProductReservedEvent productReservedEvent) {
        LOGGER.info("Timeout process payment service");
        cancelProductReservation(productReservedEvent, "Timeout process payment service");
    }
}
