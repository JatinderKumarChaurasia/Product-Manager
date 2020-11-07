package com.jkc.microservices.core.recommendation.mappers;

import com.jkc.microservices.api.core.recommendation.Recommendation;
import com.jkc.microservices.core.recommendation.models.RecommendationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Mapper(componentModel = "spring")
public interface RecommendationMapper {


    @Mappings(value = {
            @Mapping(target = "rate", source = "recommendationEntity.rating"),
            @Mapping(target = "serviceAddress", ignore = true)
    })
    Recommendation recommendationEntityToRecommendationApi(RecommendationEntity recommendationEntity);

    @Mappings(value = {
            @Mapping(target = "rating", source = "recommendation.rate"),
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "version", ignore = true)
    })
    RecommendationEntity recommendationApiToRecommendationEntity(Recommendation recommendation);

    List<Recommendation> recommendationEntitiesToRecommendationApis(List<RecommendationEntity> recommendationEntities);

    List<RecommendationEntity> recommendationApisToRecommendationEntities(List<Recommendation> recommendations);
}
