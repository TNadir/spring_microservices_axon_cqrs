package com.nadir.estore.ProductsService.query;

import com.nadir.estore.ProductsService.core.data.ProductEntity;
import com.nadir.estore.ProductsService.core.data.ProductsRepository;
import com.nadir.estore.ProductsService.query.rest.ProductRestModel;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProductsQueryHandler {

    private final ProductsRepository productsRepository;

    @Autowired
    public ProductsQueryHandler(ProductsRepository productsRepository) {
        this.productsRepository = productsRepository;
    }

    @QueryHandler
    public List<ProductRestModel> findProducts(FindProductsQuery findProductsQuery) {

        List<ProductEntity> storedProducts = productsRepository.findAll();
        return storedProducts.stream().map(x -> {
            var product = new ProductRestModel();
            BeanUtils.copyProperties(x,product);
            return product;
        }).collect(Collectors.toList());
    }

}
