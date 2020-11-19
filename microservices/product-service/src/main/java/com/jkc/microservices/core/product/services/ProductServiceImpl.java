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
import reactor.core.publisher.Mono;

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
    public Mono<Product> getProduct(int productID) {
        LOGGER.debug("/product return the found product for productID={} serviceAddress: {}", productID, serviceUtil.getServiceAddress());
        if (productID < 1) {
            throw new InvalidInputException("Invalid ProductID: " + productID);
        }
        return productRepository.findByProductID(productID).switchIfEmpty(Mono.error(new NotFoundException("No product found for productID: " + productID))).log().map(productMapper::productEntityToProductApi).map(product -> {
            product.setServiceAddress(serviceUtil.getServiceAddress());
            return product;
        });
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
        if (product.getProductID() < 1) throw new InvalidInputException("Invalid productID: " + product.getProductID());
        ProductEntity productEntity = productMapper.productApiToProductEntity(product);
        Mono<Product> productMono = productRepository.save(productEntity).log().onErrorMap(DuplicateKeyException.class, ex -> new InvalidInputException("Duplicate key, Product ID: " + product.getProductID())).map(productMapper::productEntityToProductApi);
        return productMono.block();
    }

    /**
     * curl -X DELETE $HOST:$PORT/product/1
     *
     * @param productID description="productID:int"
     */
    @Override
    public void deleteProduct(int productID) {
        if (productID < 1) throw new InvalidInputException("Invalid productID: " + productID);
        LOGGER.debug("deleteProduct: tries to delete an entity with productID: {}", productID);
        productRepository.findByProductID(productID).log().map(productRepository::delete).flatMap(e -> e).block();
    }
}
