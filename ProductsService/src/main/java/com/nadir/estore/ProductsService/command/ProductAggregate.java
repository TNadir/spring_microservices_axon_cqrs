package com.nadir.estore.ProductsService.command;

import java.math.BigDecimal;

import com.nadir.estore.ProductsService.core.events.ProductCreatedEvent;
import com.nadir.estore.core.commands.CancelProductReservationCommand;
import com.nadir.estore.core.commands.ReserveProductCommand;
import com.nadir.estore.core.events.ProductReservationCanceledEvent;
import com.nadir.estore.core.events.ProductReservedEvent;
import lombok.NoArgsConstructor;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.beans.BeanUtils;


@Aggregate
@NoArgsConstructor
public class ProductAggregate {
	
	@AggregateIdentifier
	private String productId;
	private String title;
	private BigDecimal price;
	private Integer quantity;

	@CommandHandler
	public ProductAggregate(CreateProductCommand createProductCommand) {
		// Validate Create Product Command
		
		if(createProductCommand.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("Price cannot be less or equal than zero");
		}

		if(createProductCommand.getTitle() == null
				|| createProductCommand.getTitle().isBlank()) {
			throw new IllegalArgumentException("Title cannot be empty");
		}

		var productCreatedEvent = new ProductCreatedEvent();
		BeanUtils.copyProperties(createProductCommand, productCreatedEvent);
		AggregateLifecycle.apply(productCreatedEvent);
	}

	@EventSourcingHandler
	public void on(ProductCreatedEvent productCreatedEvent) {
		this.productId = productCreatedEvent.getProductId();
		this.price = productCreatedEvent.getPrice();
		this.title = productCreatedEvent.getTitle();
		this.quantity = productCreatedEvent.getQuantity();
	}


	@CommandHandler
	public void handle(ReserveProductCommand reserveProductCommand) {

		if(quantity < reserveProductCommand.getQuantity())
			throw new IllegalArgumentException("Insufficient number of items in stock");

		ProductReservedEvent productReservedEvent = ProductReservedEvent.builder()
				.productId(reserveProductCommand.getProductId())
				.orderId(reserveProductCommand.getOrderId())
				.quantity(reserveProductCommand.getQuantity())
				.userId(reserveProductCommand.getUserId())
				.build();

		AggregateLifecycle.apply(productReservedEvent);

	}


	@EventSourcingHandler
	public void on(ProductReservedEvent productReservedEvent) {
		this.quantity-=productReservedEvent.getQuantity();
	}

	@CommandHandler
	public void handle(CancelProductReservationCommand cancelProductReservationCommand) {

		ProductReservationCanceledEvent productReservationCanceledEvent =
				ProductReservationCanceledEvent.builder()
						.productId(cancelProductReservationCommand.getProductId())
						.orderId(cancelProductReservationCommand.getOrderId())
						.quantity(cancelProductReservationCommand.getQuantity())
						.userId(cancelProductReservationCommand.getUserId())
						.reason(cancelProductReservationCommand.getReason())
						.build();

		AggregateLifecycle.apply(productReservationCanceledEvent);
	}

	@EventSourcingHandler
	public void on(ProductReservationCanceledEvent productReservationCanceledEvent) {
		this.quantity += productReservationCanceledEvent.getQuantity();
	}
}
