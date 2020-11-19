package com.jkc.microservices.core.recommendation;

import com.jkc.microservices.core.recommendation.models.RecommendationEntity;
import com.jkc.microservices.core.recommendation.repositories.RecommendationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@DataMongoTest
class PersistenceTests {

    @Autowired
    private RecommendationRepository recommendationRepository;

    private RecommendationEntity publicRepositoryEntity;

    @BeforeEach
    void setUp() {
        recommendationRepository.deleteAll().block();
        RecommendationEntity recommendationEntity = new RecommendationEntity(1, 2, "a", 3, "c");
        publicRepositoryEntity = recommendationRepository.save(recommendationEntity).block();
        assert publicRepositoryEntity != null;
        assertEqualsRecommendation(recommendationEntity, publicRepositoryEntity);
    }

    @Test
    void create() {
        RecommendationEntity recommendationEntity = new RecommendationEntity(1, 3, "a", 3, "c");
        recommendationRepository.save(recommendationEntity).block();
        RecommendationEntity foundEntity = recommendationRepository.findById(recommendationEntity.getId()).block();
        assert foundEntity != null;
        assertEqualsRecommendation(recommendationEntity, foundEntity);
        assertEquals(2, recommendationRepository.count().block());
    }

    @Test
    void update() {
        publicRepositoryEntity.setAuthor("a2");
        recommendationRepository.save(publicRepositoryEntity).block();
        RecommendationEntity foundEntity = recommendationRepository.findById(publicRepositoryEntity.getId()).block();
        assert foundEntity != null;
        assertEquals(1, foundEntity.getVersion());
        assertEquals("a2", foundEntity.getAuthor());
    }

    @Test
    void delete() {
        recommendationRepository.delete(publicRepositoryEntity).block();
        assertFalse(recommendationRepository.existsById(publicRepositoryEntity.getId()).block());
    }

    @Test
    void getByProductId() {
        List<RecommendationEntity> recommendationEntities = recommendationRepository.findByProductID(publicRepositoryEntity.getProductID()).collectList().block();
        assertThat(recommendationEntities, hasSize(1));
        assertEqualsRecommendation(publicRepositoryEntity, recommendationEntities.get(0));
    }

    @Test
    void duplicateError() {
        RecommendationEntity recommendationEntity = new RecommendationEntity(1, 2, "a", 3, "c");
        assertThrows(DuplicateKeyException.class, () -> {
            recommendationRepository.save(recommendationEntity).block();
        });
    }

    @Test
    void optimisticLockError() {
        RecommendationEntity entity1 = recommendationRepository.findById(publicRepositoryEntity.getId()).block();
        RecommendationEntity entity2 = recommendationRepository.findById(publicRepositoryEntity.getId()).block();
        assert entity1 != null;
        entity1.setAuthor("a1");
        recommendationRepository.save(entity1).block();
        assert entity2 != null;
        entity2.setAuthor("a2");
        try {
            recommendationRepository.save(entity2).block();
            fail("Expected an OptimisticLockingFailureException");
        } catch (OptimisticLockingFailureException e) {
            e.printStackTrace();
        }
        RecommendationEntity updatedEntity = recommendationRepository.findById(publicRepositoryEntity.getId()).block();
        assert updatedEntity != null;
        assertEquals(1, (int) updatedEntity.getVersion());
        assertEquals("a1", updatedEntity.getAuthor());
    }

    private void assertEqualsRecommendation(RecommendationEntity expectedEntity, RecommendationEntity actualEntity) {
        assertEquals(expectedEntity.getId(), actualEntity.getId());
        assertEquals(expectedEntity.getVersion(), actualEntity.getVersion());
        assertEquals(expectedEntity.getProductID(), actualEntity.getProductID());
        assertEquals(expectedEntity.getRecommendationID(), actualEntity.getRecommendationID());
        assertEquals(expectedEntity.getAuthor(), actualEntity.getAuthor());
        assertEquals(expectedEntity.getRating(), actualEntity.getRating());
        assertEquals(expectedEntity.getContent(), actualEntity.getContent());
    }
}
