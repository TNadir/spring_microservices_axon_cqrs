package com.nadir.estore.ProductsService.core.data;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProducrLookupRepository extends JpaRepository<ProductLookupEntity, String> {
    ProductLookupEntity findByProductIdOrTitle(String productId, String title);
}
