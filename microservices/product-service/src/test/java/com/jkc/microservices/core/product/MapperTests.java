package com.jkc.microservices.core.product;

import com.jkc.microservices.api.core.product.Product;
import com.jkc.microservices.core.product.mappers.ProductMapper;
import com.jkc.microservices.core.product.models.ProductEntity;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.*;

class MapperTests {
    private final ProductMapper productMapper = Mappers.getMapper(ProductMapper.class);

    @Test
    void mapperTests() {
        assertNotNull(productMapper);
        Product product = new Product(1, "n", 1, "sa");
        ProductEntity productEntity = productMapper.productApiToProductEntity(product);
        assertEquals(product.getProductID(), productEntity.getProductID());
        assertEquals(product.getProductID(), productEntity.getProductID());
        assertEquals(product.getName(), productEntity.getName());
        assertEquals(product.getWeight(), productEntity.getWeight());
        Product product1 = productMapper.productEntityToProductApi(productEntity);
        assertEquals(product.getProductID(), product1.getProductID());
        assertEquals(product.getProductID(), product1.getProductID());
        assertEquals(product.getName(), product1.getName());
        assertEquals(product.getWeight(), product1.getWeight());
        assertNull(product1.getServiceAddress());
    }
}
