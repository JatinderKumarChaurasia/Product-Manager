package com.jkc.microservices.api.composite.product;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

public interface ProductCompositeService {

    /**
     * usage : curl $HOST:$PORT/product-composite/1
     *
     * @param productID "productID: int required"
     * @return composite productInfo , if found else null
     */

    @GetMapping(value = "/product-composite/{productID}", produces = "application/json")
    ProductAggregate getProduct(@PathVariable int productID);
}
