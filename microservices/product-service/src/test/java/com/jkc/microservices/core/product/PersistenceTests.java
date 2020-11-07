package com.jkc.microservices.core.product;

import com.jkc.microservices.core.product.models.ProductEntity;
import com.jkc.microservices.core.product.repositories.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.LongStream.rangeClosed;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@DataMongoTest
class PersistenceTests {

    @Autowired
    private ProductRepository productRepository;

    private ProductEntity savedProductEntity;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        ProductEntity productEntity = new ProductEntity(1, "name", 4.0);
        savedProductEntity = productRepository.save(productEntity);
        assertEqualsProduct(productEntity, savedProductEntity);
    }

    private void assertEqualsProduct(ProductEntity expectedEntity, ProductEntity actualEntity) {
        assertEquals(expectedEntity.getName(), actualEntity.getName());
        assertEquals(expectedEntity.getId(), actualEntity.getId());
        assertEquals(expectedEntity.getVersion(), actualEntity.getVersion());
        assertEquals(expectedEntity.getProductID(), actualEntity.getProductID());
        assertEquals(expectedEntity.getName(), actualEntity.getName());
        assertEquals(expectedEntity.getWeight(), actualEntity.getWeight());
    }

    @Test
    void create() {
        ProductEntity productEntity = new ProductEntity(2, "name2", 5.0);
        productRepository.save(productEntity);
        ProductEntity foundEntity = productRepository.findById(productEntity.getId()).get();
        assertEqualsProduct(productEntity, foundEntity);
        assertEquals(2, productRepository.count());
    }

    @Test
    void update() {
        savedProductEntity.setName("name3");
        productRepository.save(savedProductEntity);
        ProductEntity foundEntity = productRepository.findById(savedProductEntity.getId()).get();
        assertEquals(1, (long) foundEntity.getVersion());
        assertEquals("name3", foundEntity.getName());
    }

    @Test
    void delete() {
        productRepository.delete(savedProductEntity);
        assertFalse(productRepository.existsById(savedProductEntity.getId()));
    }

    @Test
    void getProductByID() {
        Optional<ProductEntity> productEntity = productRepository.findByProductID(savedProductEntity.getProductID());
        assertTrue(productEntity.isPresent());
        assertEqualsProduct(savedProductEntity, productEntity.get());
    }

    @Test
    void duplicateError() {
        ProductEntity productEntity = new ProductEntity(savedProductEntity.getProductID(), "name", 4.0);
        assertThrows(DataIntegrityViolationException.class, () -> {
            productRepository.save(productEntity);
        });
    }

    @Test
    void optimisticLockError() {
        ProductEntity productEntity1 = productRepository.findByProductID(savedProductEntity.getProductID()).get();
        ProductEntity productEntity2 = productRepository.findByProductID(savedProductEntity.getProductID()).get();
        productEntity1.setName("name4");
        productRepository.save(productEntity1);
        productEntity2.setName("name5");
        try {
            productRepository.save(productEntity2);
        } catch (OptimisticLockingFailureException e) {
            e.printStackTrace();
        }
        ProductEntity updatedEntity = productRepository.findById(savedProductEntity.getId()).get();
        assertEquals(1, (int) updatedEntity.getVersion());
        assertEquals("name4", updatedEntity.getName());
    }

    @Test
    void paging() {
        productRepository.deleteAll();
        List<ProductEntity> productEntities = rangeClosed(1001, 1010).mapToObj(value -> new ProductEntity((int) value, "name" + value, value)).collect(Collectors.toList());
        productRepository.saveAll(productEntities);

        Pageable nextPage = PageRequest.of(0, 4, Sort.Direction.ASC, "productID");
        nextPage = testNextPage(nextPage, "[1001, 1002, 1003, 1004]", true);
        nextPage = testNextPage(nextPage, "[1005, 1006, 1007, 1008]", true);
        nextPage = testNextPage(nextPage, "[1009, 1010]", false);
    }

    private Pageable testNextPage(Pageable nextPage, String expectedProductIDs, boolean expectNextPage) {
        Page<ProductEntity> productEntityPage = productRepository.findAll(nextPage);
        assertEquals(expectedProductIDs, productEntityPage.getContent().stream().map(ProductEntity::getProductID).collect(Collectors.toList()).toString());
        assertEquals(expectNextPage, productEntityPage.hasNext());
        return productEntityPage.nextPageable();
    }

}
