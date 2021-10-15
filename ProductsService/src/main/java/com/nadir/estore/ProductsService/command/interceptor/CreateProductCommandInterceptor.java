package com.nadir.estore.ProductsService.command.interceptor;

import com.nadir.estore.ProductsService.command.CreateProductCommand;
import com.nadir.estore.ProductsService.core.data.ProducrLookupRepository;
import com.nadir.estore.ProductsService.core.data.ProductsRepository;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.messaging.MessageDispatchInterceptor;
import org.axonframework.modelling.command.CommandHandlerInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.BiFunction;

@Component
public class CreateProductCommandInterceptor
        implements MessageDispatchInterceptor<CommandMessage<?>> {

    private static final Logger logger = LoggerFactory.getLogger(CreateProductCommandInterceptor.class);

    private final ProducrLookupRepository producrLookupRepository;

    @Autowired
    public CreateProductCommandInterceptor(ProducrLookupRepository producrLookupRepository) {
        this.producrLookupRepository = producrLookupRepository;
    }



    @Override
    public BiFunction<Integer, CommandMessage<?>, CommandMessage<?>> handle(List<? extends CommandMessage<?>> messages) {
        return (index, command) -> {

            logger.info(String.valueOf(command.getPayloadType()));

            if (CreateProductCommand.class.equals(command.getPayloadType())) {

                CreateProductCommand createProductCommand = (CreateProductCommand) command.getPayload();

                var productLookupEntity = producrLookupRepository.findByProductIdOrTitle(createProductCommand.getProductId(),
                                                                createProductCommand.getTitle());

                if(productLookupEntity != null) {
                    throw new IllegalStateException(
                      String.format("Product with productId %s or title %s already exists",
                                            createProductCommand.getProductId(),
                                            createProductCommand.getTitle())
                    );
                }

            }

            return command;
        };
    }
}
