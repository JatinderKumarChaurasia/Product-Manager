package com.jkc.microservices.core.review;

import com.jkc.microservices.core.review.model.ReviewEntity;
import com.jkc.microservices.core.review.repositories.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED;


@ExtendWith(SpringExtension.class)
@DataJpaTest
@Transactional(propagation = NOT_SUPPORTED)
class PersistenceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersistenceTest.class);

    @Autowired
    private ReviewRepository reviewRepository;

    private ReviewEntity reviewEntity;

    @BeforeEach
    void setUp() {
        reviewRepository.deleteAll();
        ReviewEntity entity = new ReviewEntity(1, 2, "author", "subject", "content");
        reviewEntity = reviewRepository.save(entity);
        assertEqualsReview(entity, reviewEntity);
    }

    @Test
    void create() {

        ReviewEntity entity = new ReviewEntity(1, 3, "author", "subject", "content");
        reviewRepository.save(entity);
        ReviewEntity foundEntity = reviewRepository.findById(entity.getId()).get();
        assertEqualsReview(entity, foundEntity);

        assertEquals(2, reviewRepository.count());
    }

    @Test
    void update() {
        reviewEntity.setAuthor("author1");
        reviewRepository.save(reviewEntity);

        ReviewEntity foundEntity = reviewRepository.findById(reviewEntity.getId()).get();
        assertEquals(1, (long) foundEntity.getVersion());
        assertEquals("author1", foundEntity.getAuthor());
    }

    @Test
    void delete() {
        reviewRepository.delete(reviewEntity);
        assertFalse(reviewRepository.existsById(reviewEntity.getId()));
    }

    @Test
    void getProductByID() {
        List<ReviewEntity> reviewEntities = reviewRepository.findByProductID(reviewEntity.getProductID());
        assertThat(reviewEntities, hasSize(1));
        assertEqualsReview(reviewEntity, reviewEntities.get(0));
    }

    @Test
    void duplicateError() {
        ReviewEntity entity = new ReviewEntity(1, 2, "author", "subject", "content");
        assertThrows(DataIntegrityViolationException.class, () -> {
            reviewRepository.save(entity);
        });
    }

    @Test
    void optimisticLockError() {
        LOGGER.debug("Checking Optimistic Lock Error");
        System.out.println("Checking Optimistic Logging");
        ReviewEntity entity1 = reviewRepository.findById(reviewEntity.getId()).get();
        ReviewEntity entity2 = reviewRepository.findById(reviewEntity.getId()).get();
        entity1.setAuthor("author1");
        reviewRepository.save(entity1);
        try {
            entity2.setAuthor("author2");
            reviewRepository.save(entity2);
            fail("expected an OptimisticLockingFailureException");
        } catch (OptimisticLockingFailureException e) {
        }
        ReviewEntity reviewEntity1 = reviewRepository.findById(reviewEntity.getId()).get();
        assertEquals(1, reviewEntity1.getVersion());
        assertEquals("author1", reviewEntity1.getAuthor());
    }

    private void assertEqualsReview(ReviewEntity expectedEntity, ReviewEntity actualEntity) {
        assertEquals(expectedEntity.getId(), actualEntity.getId());
        assertEquals(expectedEntity.getVersion(), actualEntity.getVersion());
        assertEquals(expectedEntity.getProductID(), actualEntity.getProductID());
        assertEquals(expectedEntity.getReviewID(), actualEntity.getReviewID());
        assertEquals(expectedEntity.getAuthor(), actualEntity.getAuthor());
        assertEquals(expectedEntity.getSubject(), actualEntity.getSubject());
        assertEquals(expectedEntity.getContent(), actualEntity.getContent());
    }
}
