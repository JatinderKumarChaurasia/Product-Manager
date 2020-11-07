package com.jkc.microservices.core.product.services;

import com.jkc.microservices.api.core.product.Product;
import com.jkc.microservices.api.core.product.ProductService;
import com.jkc.microservices.core.product.mappers.ProductMapper;
import com.jkc.microservices.core.product.models.ProductEntity;
import com.jkc.microservices.core.product.repositories.ProductRepository;
import com.jkc.microservices.util.exceptions.InvalidInputException;
import com.jkc.microservices.util.exceptions.NotFoundException;
import com.jkc.microservices.util.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductServiceImpl implements ProductService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductServiceImpl.class);
    private final ServiceUtil serviceUtil;
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository, ProductMapper productMapper, ServiceUtil serviceUtil) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
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
        LOGGER.debug("/product return the found product for productID={} serviceAddress: {}", productID, serviceUtil.getServiceAddress());
        if (productID < 1) {
            throw new InvalidInputException("Invalid ProductID: " + productID);
        }
        ProductEntity productEntity = productRepository.findByProductID(productID).orElseThrow(() -> new NotFoundException("No product found for productID: " + productID));
        Product response = productMapper.productEntityToProductApi(productEntity);
        response.setServiceAddress(serviceUtil.getServiceAddress());
        LOGGER.debug("getProduct: found productID: {}", response.getProductID());
        return response;
    }

    /**
     * curl -X POST $HOST:$PORT/product \
     * -H "Content-Type: application/json" --data \
     * '{"productID":123,"name":"product 123","weight":123}'
     *
     * @param product "description: product:Product"
     * @return "Product"
     */
    @Override
    public Product createProduct(Product product) {
        try {
            ProductEntity productEntity = productMapper.productApiToProductEntity(product);
            ProductEntity entity = productRepository.save(productEntity);
            LOGGER.debug("createProduct: entity created for productID: {}", product.getProductID());
            return productMapper.productEntityToProductApi(entity);
        } catch (DuplicateKeyException e) {
            throw new InvalidInputException("Duplicate key, Product ID: " + product.getProductID());
        }
    }

    /**
     * curl -X DELETE $HOST:$PORT/product/1
     *
     * @param productID description="productID:int"
     */
    @Override
    public void deleteProduct(int productID) {
        LOGGER.debug("deleteProduct: tries to delete an entity with productID: {}", productID);
        productRepository.findByProductID(productID).ifPresent(productRepository::delete);
    }
}
