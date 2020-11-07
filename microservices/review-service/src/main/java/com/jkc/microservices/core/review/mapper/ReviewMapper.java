package com.jkc.microservices.core.review.mapper;

import com.jkc.microservices.api.core.review.Review;
import com.jkc.microservices.core.review.model.ReviewEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.springframework.stereotype.Component;

import java.util.List;

// Mapper: Review ↔ ReviewEntity Transformation

@Component
@Mapper(componentModel = "spring")
public interface ReviewMapper {

    @Mappings(value = {
            @Mapping(target = "serviceAddress", ignore = true)
    })
    Review reviewEntityToReviewApi(ReviewEntity reviewEntity);

    @Mappings(value = {
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "version", ignore = true)
    })
    ReviewEntity reviewApiToReviewEntity(Review review);

    List<ReviewEntity> reviewApisToReviewEntitiesList(List<Review> reviews);

    List<Review> reviewEntitiesToReviewApisList(List<ReviewEntity> reviewEntities);
}
