package com.jkc.microservices.core.recommendation;

import com.jkc.microservices.api.core.recommendation.Recommendation;
import com.jkc.microservices.core.recommendation.mappers.RecommendationMapper;
import com.jkc.microservices.core.recommendation.models.RecommendationEntity;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MapperTests {
    private final RecommendationMapper recommendationMapper = Mappers.getMapper(RecommendationMapper.class);

    @Test
    void mapperTests() {
        assertNotNull(recommendationMapper);
        Recommendation recommendation = new Recommendation(1, 2, "a", 4, "C", "adr");
        RecommendationEntity recommendationEntity = recommendationMapper.recommendationApiToRecommendationEntity(recommendation);
        assertEquals(recommendation.getProductID(), recommendationEntity.getProductID());
        assertEquals(recommendation.getRecommendationID(), recommendationEntity.getRecommendationID());
        assertEquals(recommendation.getAuthor(), recommendationEntity.getAuthor());
        assertEquals(recommendation.getRate(), recommendationEntity.getRating());
        assertEquals(recommendation.getContent(), recommendationEntity.getContent());
        Recommendation recommendation1 = recommendationMapper.recommendationEntityToRecommendationApi(recommendationEntity);
        assertEquals(recommendation.getProductID(), recommendation1.getProductID());
        assertEquals(recommendation.getRecommendationID(), recommendation1.getRecommendationID());
        assertEquals(recommendation.getAuthor(), recommendation1.getAuthor());
        assertEquals(recommendation.getRate(), recommendation1.getRate());
        assertEquals(recommendation.getContent(), recommendation1.getContent());
        assertNull(recommendation1.getServiceAddress());
    }

    @Test
    void mapperListTests() {

        assertNotNull(recommendationMapper);

        Recommendation recommendation1 = new Recommendation(1, 2, "a", 4, "C", "adr");
        List<Recommendation> recommendations = Collections.singletonList(recommendation1);

        List<RecommendationEntity> recommendationEntities = recommendationMapper.recommendationApisToRecommendationEntities(recommendations);
        assertEquals(recommendations.size(), recommendationEntities.size());

        RecommendationEntity recommendationEntity = recommendationEntities.get(0);

        assertEquals(recommendation1.getProductID(), recommendationEntity.getProductID());
        assertEquals(recommendation1.getRecommendationID(), recommendationEntity.getRecommendationID());
        assertEquals(recommendation1.getAuthor(), recommendationEntity.getAuthor());
        assertEquals(recommendation1.getRate(), recommendationEntity.getRating());
        assertEquals(recommendation1.getContent(), recommendationEntity.getContent());
        List<Recommendation> recommendationList = recommendationMapper.recommendationEntitiesToRecommendationApis(recommendationEntities);
        assertEquals(recommendations.size(), recommendationList.size());
        Recommendation recommendation2 = recommendationList.get(0);
        assertEquals(recommendation1.getProductID(), recommendation2.getProductID());
        assertEquals(recommendation1.getRecommendationID(), recommendation2.getRecommendationID());
        assertEquals(recommendation1.getAuthor(), recommendation2.getAuthor());
        assertEquals(recommendation1.getRate(), recommendation2.getRate());
        assertEquals(recommendation1.getContent(), recommendation2.getContent());
        assertNull(recommendation2.getServiceAddress());
    }
}
