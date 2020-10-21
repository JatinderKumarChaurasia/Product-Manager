package com.jkc.microservices.core.product.services;

import com.jkc.microservices.api.core.product.Product;
import com.jkc.microservices.api.core.product.ProductService;
import com.jkc.microservices.util.exceptions.InvalidInputException;
import com.jkc.microservices.util.exceptions.NotFoundException;
import com.jkc.microservices.util.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductServiceImpl implements ProductService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductServiceImpl.class);
    private final ServiceUtil serviceUtil;

    @Autowired
    public ProductServiceImpl(ServiceUtil serviceUtil) {
        this.serviceUtil = serviceUtil;
    }

    /**
     * curl $HOST:$PORT/product/1
     *
     * @param productID "productId : int"
     * @return Product, if found else null
     */
    @Override
    public Product getProduct(int productID) {
        LOGGER.debug("/product return the found product for productId={}", productID);
        if (productID <1) {
            throw new InvalidInputException("Invalid ProductID: "+productID);
        }
        if (productID == 13) {
            throw new NotFoundException("No product found for productID: " + productID);
        }

        return new Product(productID,"name "+productID,123,serviceUtil.getServiceAddress());
    }
}
