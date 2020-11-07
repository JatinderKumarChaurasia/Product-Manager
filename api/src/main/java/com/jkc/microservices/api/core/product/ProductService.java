package com.jkc.microservices.api.core.product;

import org.springframework.web.bind.annotation.*;

public interface ProductService {
    /**
     * curl $HOST:$PORT/product/1
     *
     * @param productID "productID : int"
     * @return Product, if found else null
     */

    @GetMapping(value = "/product/{productID}", produces = "application/json")
    Product getProduct(@PathVariable int productID);

    /**
     * curl -X POST $HOST:$PORT/product \
     * -H "Content-Type: application/json" --data \
     * '{"productID":123,"name":"product 123","weight":123}'
     *
     * @param product "description: product:Product"
     * @return "Product"
     */
    @PostMapping(value = "/product", consumes = "application/json", produces = "application/json")
    Product createProduct(@RequestBody Product product);

    /**
     * curl -X DELETE $HOST:$PORT/product/1
     *
     * @param productID description="productID:int"
     */
    @DeleteMapping(value = "/product/{productID}")
    void deleteProduct(@PathVariable int productID);
}
