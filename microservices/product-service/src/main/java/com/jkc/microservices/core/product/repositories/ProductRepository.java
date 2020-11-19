package com.jkc.microservices.core.product.repositories;

import com.jkc.microservices.core.product.models.ProductEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface ProductRepository extends ReactiveCrudRepository<ProductEntity, String> {
    Mono<ProductEntity> findByProductID(int productId);
}
