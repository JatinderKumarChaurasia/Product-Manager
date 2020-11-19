package com.jkc.microservices.core.recommendation.repositories;

import com.jkc.microservices.core.recommendation.models.RecommendationEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface RecommendationRepository extends ReactiveCrudRepository<RecommendationEntity, String> {
    Flux<RecommendationEntity> findByProductID(int productID);
}
