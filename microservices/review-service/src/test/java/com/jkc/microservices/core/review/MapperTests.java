package com.jkc.microservices.core.review;

import com.jkc.microservices.api.core.review.Review;
import com.jkc.microservices.core.review.mapper.ReviewMapper;
import com.jkc.microservices.core.review.model.ReviewEntity;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MapperTests {
    private final ReviewMapper reviewMapper = Mappers.getMapper(ReviewMapper.class);

    @Test
    void mapperTests() {
        assertNotNull(reviewMapper);
        Review review = new Review(1, 1, "author", "subject", "content", "serviceAddress");
        ReviewEntity reviewEntity = reviewMapper.reviewApiToReviewEntity(review);
        assertEquals(review.getProductID(), reviewEntity.getProductID());
        assertEquals(review.getReviewID(), reviewEntity.getReviewID());
        assertEquals(review.getAuthor(), reviewEntity.getAuthor());
        assertEquals(review.getSubject(), reviewEntity.getSubject());
        assertEquals(review.getContent(), reviewEntity.getContent());

        Review review1 = reviewMapper.reviewEntityToReviewApi(reviewEntity);
        assertEquals(review.getProductID(), review1.getProductID());
        assertEquals(review.getReviewID(), review1.getReviewID());
        assertEquals(review.getAuthor(), review1.getAuthor());
        assertEquals(review.getSubject(), review1.getSubject());
        assertEquals(review.getContent(), review1.getContent());
        assertNull(review1.getServiceAddress());
    }

    @Test
    void mapperListTests() {
        Review review = new Review(1, 1, "author", "subject", "content", "serviceAddress");
        List<Review> reviews = Collections.singletonList(review);
        List<ReviewEntity> reviewEntities = reviewMapper.reviewApisToReviewEntitiesList(reviews);
        assertEquals(reviews.size(), reviewEntities.size());

        ReviewEntity reviewEntity = reviewEntities.get(0);

        assertEquals(review.getProductID(), reviewEntity.getProductID());
        assertEquals(review.getReviewID(), reviewEntity.getReviewID());
        assertEquals(review.getAuthor(), reviewEntity.getAuthor());
        assertEquals(review.getSubject(), reviewEntity.getSubject());
        assertEquals(review.getContent(), reviewEntity.getContent());

        List<Review> reviews1 = reviewMapper.reviewEntitiesToReviewApisList(reviewEntities);
        assertEquals(reviews1.size(), reviewEntities.size());
        Review review1 = reviews1.get(0);

        assertEquals(review1.getProductID(), review.getProductID());
        assertEquals(review1.getReviewID(), review.getReviewID());
        assertEquals(review1.getAuthor(), review.getAuthor());
        assertEquals(review1.getSubject(), review.getSubject());
        assertEquals(review1.getContent(), review.getContent());
        assertNull(review1.getServiceAddress());
    }
}
