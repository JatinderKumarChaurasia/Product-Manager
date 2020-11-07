package com.jkc.microservices.core.review.repositories;

import com.jkc.microservices.core.review.model.ReviewEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ReviewRepository extends CrudRepository<ReviewEntity, Integer> {

    @Transactional(readOnly = true)
    List<ReviewEntity> findByProductID(int productID);
}
