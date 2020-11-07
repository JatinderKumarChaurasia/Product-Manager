package com.jkc.microservices.core.recommendation.repositories;

import com.jkc.microservices.core.recommendation.models.RecommendationEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface RecommendationRepository extends CrudRepository<RecommendationEntity, String> {
    List<RecommendationEntity> findByProductID(int productID);
}
