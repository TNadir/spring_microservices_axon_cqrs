package com.nadir.estore.ProductsService.query;

import com.nadir.estore.ProductsService.core.data.ProducrLookupRepository;
import com.nadir.estore.ProductsService.core.data.ProductLookupEntity;
import com.nadir.estore.ProductsService.core.events.ProductCreatedEvent;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@ProcessingGroup("product-group")
public class ProductLookupEventHandler {


    private final ProducrLookupRepository producrLookupRepository;

    @Autowired
    public ProductLookupEventHandler(ProducrLookupRepository producrLookupRepository) {
        this.producrLookupRepository = producrLookupRepository;
    }

    @EventHandler
    public void on(ProductCreatedEvent productCreatedEvent) {

        ProductLookupEntity productLookupEntity = new ProductLookupEntity(productCreatedEvent.getProductId(),
                productCreatedEvent.getTitle());

        producrLookupRepository.save(productLookupEntity);
    }
}
