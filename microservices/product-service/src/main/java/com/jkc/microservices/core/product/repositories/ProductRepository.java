package com.jkc.microservices.core.product.repositories;

import com.jkc.microservices.core.product.models.ProductEntity;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;

public interface ProductRepository extends PagingAndSortingRepository<ProductEntity, String> {
    Optional<ProductEntity> findByProductID(int productId);
}
