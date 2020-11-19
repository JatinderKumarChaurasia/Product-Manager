package com.jkc.microservices.core.product;

import com.jkc.microservices.core.product.models.ProductEntity;
import com.jkc.microservices.core.product.repositories.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.test.StepVerifier;

@ExtendWith(SpringExtension.class)
@DataMongoTest
class PersistenceTests {

    @Autowired
    private ProductRepository productRepository;

    private ProductEntity savedProductEntity;

    @BeforeEach
    void setUp() {
        StepVerifier.create(productRepository.deleteAll()).verifyComplete();
        ProductEntity productEntity = new ProductEntity(1, "name", 4.0);
        StepVerifier.create(productRepository.save(productEntity)).expectNextMatches(createdEntity -> {
            savedProductEntity = createdEntity;
            return areProductsEqual(createdEntity, savedProductEntity);
        }).verifyComplete();
    }

    @Test
    void create() {
        ProductEntity productEntity = new ProductEntity(2, "name2", 5.0);
        StepVerifier.create(productRepository.save(productEntity)).expectNextMatches(createdEntity -> productEntity.getProductID() == createdEntity.getProductID()).verifyComplete();
        StepVerifier.create(productRepository.findById(productEntity.getId())).expectNextMatches(foundEntity -> areProductsEqual(productEntity, foundEntity)).verifyComplete();
        StepVerifier.create(productRepository.count()).expectNext((long) 2).verifyComplete();
    }

    @Test
    void update() {
        savedProductEntity.setName("name3");
        StepVerifier.create(productRepository.save(savedProductEntity)).expectNextMatches(productEntity -> productEntity.getName().equals("name3")).verifyComplete();
        StepVerifier.create(productRepository.findById(savedProductEntity.getId())).expectNextMatches(foundEntity -> foundEntity.getVersion() == 1 && foundEntity.getName().equals("name3")).verifyComplete();
    }

    @Test
    void delete() {
        StepVerifier.create(productRepository.delete(savedProductEntity)).verifyComplete();
        StepVerifier.create(productRepository.existsById(savedProductEntity.getId())).expectNext(false).verifyComplete();
    }

    @Test
    void getProductByID() {
        StepVerifier.create(productRepository.findByProductID(savedProductEntity.getProductID())).expectNextMatches(productEntity -> areProductsEqual(savedProductEntity, productEntity)).verifyComplete();
    }

    @Test
    void duplicateError() {
        ProductEntity productEntity = new ProductEntity(savedProductEntity.getProductID(), "name", 4.0);
        StepVerifier.create(productRepository.save(productEntity)).expectError(DuplicateKeyException.class).verify();
    }

    @Test
    void optimisticLockError() {
        ProductEntity productEntity1 = productRepository.findByProductID(savedProductEntity.getProductID()).block();
        ProductEntity productEntity2 = productRepository.findByProductID(savedProductEntity.getProductID()).block();
        assert productEntity1 != null;
        productEntity1.setName("name4");
        productRepository.save(productEntity1).block();
        assert productEntity2 != null;
        productEntity2.setName("name5");
        StepVerifier.create(productRepository.save(productEntity2)).expectError(OptimisticLockingFailureException.class).verify();
        StepVerifier.create(productRepository.findById(savedProductEntity.getId())).expectNextMatches(productEntity -> productEntity.getVersion() == 1 && productEntity.getName().equals("name4")).verifyComplete();
    }

    private boolean areProductsEqual(ProductEntity expected, ProductEntity actual) {
        return (expected.getId().equals(actual.getId())) && (expected.getProductID() == actual.getProductID()) && (expected.getVersion().equals(actual.getVersion())) && (expected.getName().equals(actual.getName())) && (expected.getWeight() == actual.getWeight());
    }

}
