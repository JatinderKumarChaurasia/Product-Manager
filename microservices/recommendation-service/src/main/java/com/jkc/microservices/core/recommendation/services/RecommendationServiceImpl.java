package com.jkc.microservices.core.recommendation.services;

import com.jkc.microservices.api.core.recommendation.Recommendation;
import com.jkc.microservices.api.core.recommendation.RecommendationService;
import com.jkc.microservices.core.recommendation.mappers.RecommendationMapper;
import com.jkc.microservices.core.recommendation.models.RecommendationEntity;
import com.jkc.microservices.core.recommendation.repositories.RecommendationRepository;
import com.jkc.microservices.util.exceptions.InvalidInputException;
import com.jkc.microservices.util.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class RecommendationServiceImpl implements RecommendationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecommendationServiceImpl.class);
    private final ServiceUtil serviceUtil;
    private final RecommendationRepository recommendationRepository;
    private final RecommendationMapper recommendationMapper;

    @Autowired
    public RecommendationServiceImpl(RecommendationRepository recommendationRepository, RecommendationMapper recommendationMapper, ServiceUtil serviceUtil) {
        this.recommendationRepository = recommendationRepository;
        this.recommendationMapper = recommendationMapper;
        this.serviceUtil = serviceUtil;
    }

    /**
     * usage: curl $HOST:$PORT/recommendation?productID=1
     *
     * @param productID "provided productID: int"
     * @return list of recommendations for that @productID
     */
    @Override
    public Flux<Recommendation> getRecommendations(int productID) {
        if (productID < 1) {
            throw new InvalidInputException("Invalid productID: " + productID);
        }
        return recommendationRepository.findByProductID(productID).log().map(recommendationMapper::recommendationEntityToRecommendationApi).map(recommendation -> {
            recommendation.setServiceAddress(serviceUtil.getServiceAddress());
            return recommendation;
        });
    }

    /**
     * usage: curl -X POST $HOST:$PORT/recommendation \
     * -H "Content-Type: application/json" --data \
     * '{"productID":123,"recommendationID":456,"author":"me","rate":5,"content":"content, serviceAddress: serviceAddress, "}'
     *
     * @param recommendation description: recommendation:Recommendation
     * @return Recommendation
     */
    @Override
    public Recommendation createRecommendation(Recommendation recommendation) {
        if (recommendation.getProductID() < 1) {
            throw new InvalidInputException("Invalid productID: " + recommendation.getProductID());
        }
        RecommendationEntity recommendationEntity = recommendationMapper.recommendationApiToRecommendationEntity(recommendation);
        Mono<Recommendation> recommendationMono = recommendationRepository.save(recommendationEntity).log().onErrorMap(DuplicateKeyException.class, err -> new InvalidInputException("Duplicate key, Product ID: " + recommendation.getProductID() + ", Recommendation ID:" + recommendation.getRecommendationID())).map(recommendationMapper::recommendationEntityToRecommendationApi);
        return recommendationMono.block();
    }

    /**
     * usage: curl -X DELETE $HOST:$PORT/recommendation?productID=1
     *
     * @param productID des
     */
    @Override
    public void deleteRecommendations(int productID) {
        // deleting the recommendation
        if (productID < 1) throw new InvalidInputException("Invalid productID: " + productID);
        LOGGER.debug("deleteRecommendations: tries to delete recommendations for the product with productID: {}", productID);
        recommendationRepository.deleteAll(recommendationRepository.findByProductID(productID)).block();
    }
}
