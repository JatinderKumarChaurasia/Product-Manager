package com.jkc.microservices.api.core.product;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

public interface ProductService {
    /**
     * curl $HOST:$PORT/product/1
     *
     * @param productID "productId : int"
     * @return Product, if found else null
     */

    @GetMapping(value = "/product/{productID}", produces = "application/json")
    Product getProduct(@PathVariable int productID);
}
