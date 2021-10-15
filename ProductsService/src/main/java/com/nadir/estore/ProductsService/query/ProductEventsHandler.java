package com.nadir.estore.ProductsService.query;

import com.nadir.estore.ProductsService.core.data.ProductEntity;
import com.nadir.estore.ProductsService.core.data.ProductsRepository;
import com.nadir.estore.ProductsService.core.events.ProductCreatedEvent;
import com.nadir.estore.core.events.ProductReservationCanceledEvent;
import com.nadir.estore.core.events.ProductReservedEvent;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.messaging.interceptors.ExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@ProcessingGroup("product-group")
public class ProductEventsHandler {

    private final ProductsRepository productsRepository;

    private final Logger LOGGER = LoggerFactory.getLogger(ProductEventsHandler.class);

    @Autowired
    public ProductEventsHandler(ProductsRepository productsRepository) {
        this.productsRepository = productsRepository;
    }

    @ExceptionHandler(resultType = Exception.class)
    public void handle(Exception ex) throws Exception {
        throw ex;
    }

    @ExceptionHandler(resultType = IllegalArgumentException.class)
    public void handle(IllegalArgumentException ex) {

    }


    @EventHandler
    public void on(ProductCreatedEvent event) {

        ProductEntity productEntity = new ProductEntity();
        BeanUtils.copyProperties(event, productEntity);

        productsRepository.save(productEntity);
    }

    @EventHandler
    public void on(ProductReservedEvent productReservedEvent) {

        LOGGER.info("ProductReservedEvent is published: " + productReservedEvent);

        ProductEntity productEntity = productsRepository.findByProductId(productReservedEvent.getProductId());
        productEntity.setQuantity(productEntity.getQuantity()-productReservedEvent.getQuantity());
        productsRepository.save(productEntity);

        LOGGER.debug("Product quantity: " + productEntity.getQuantity());
    }

    @EventHandler
    public void on(ProductReservationCanceledEvent productReservationCanceledEvent) {

        ProductEntity productEntity = productsRepository.findByProductId(productReservationCanceledEvent.getProductId());
        productEntity.setQuantity(productEntity.getQuantity() + productReservationCanceledEvent.getQuantity());
        productsRepository.save(productEntity);
        LOGGER.debug("Product quantity: " + productEntity.getQuantity());
    }
}
