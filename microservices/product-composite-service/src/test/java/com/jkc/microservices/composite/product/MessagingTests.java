package com.jkc.microservices.composite.product;

import com.jkc.microservices.api.composite.product.ProductAggregate;
import com.jkc.microservices.api.composite.product.RecommendationSummary;
import com.jkc.microservices.api.composite.product.ReviewSummary;
import com.jkc.microservices.api.core.product.Product;
import com.jkc.microservices.api.core.recommendation.Recommendation;
import com.jkc.microservices.api.core.review.Review;
import com.jkc.microservices.api.event.EventModel;
import com.jkc.microservices.composite.product.configurations.MessageSources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.concurrent.BlockingQueue;

import static com.jkc.microservices.composite.product.IsSameEvent.sameEventExceptCreatedAt;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.cloud.stream.test.matcher.MessageQueueMatcher.receivesPayloadThat;

@ExtendWith(value = SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class MessagingTests {
    private static final int PRODUCT_ID_OK = 1;
    private static final int PRODUCT_ID_NOT_FOUND = 2;
    private static final int PRODUCT_ID_INVALID = 3;
    private static final Logger LOGGER = LoggerFactory.getLogger(MessagingTests.class);
    BlockingQueue<Message<?>> queueProducts = null;
    BlockingQueue<Message<?>> queueRecommendations = null;
    BlockingQueue<Message<?>> queueReviews = null;
    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private MessageSources channels;
    @Autowired
    private MessageCollector messageCollector;

    @BeforeEach
    public void setUp() {
        this.queueProducts = getQueue(channels.outputProducts());
        this.queueRecommendations = getQueue(channels.outputRecommendations());
        this.queueReviews = getQueue(channels.outputReviews());
    }

    private BlockingQueue<Message<?>> getQueue(MessageChannel messageChannel) {
        return messageCollector.forChannel(messageChannel);
    }

    @Test
    void createCompositeProduct1() {
        ProductAggregate productAggregate = new ProductAggregate(1, "name", 1, null, null, null);
        postAndVerifyProduct(productAggregate, HttpStatus.OK);
        assertEquals(1, queueProducts.size());
        assertEquals(0, queueRecommendations.size());
        assertEquals(0, queueReviews.size());
        EventModel<Integer, Product> productEventModel = new EventModel<>(EventModel.TYPE.CREATE, productAggregate.getProductID(), new Product(productAggregate.getProductID(), productAggregate.getName(), productAggregate.getWeight(), null));
        assertThat(queueProducts, is(receivesPayloadThat(sameEventExceptCreatedAt(productEventModel))));
        assertEquals(0, queueRecommendations.size());
        assertEquals(0, queueReviews.size());
    }

    @Test
    void createCompositeProduct2() {
        ProductAggregate productAggregate = new ProductAggregate(1, "name", 1, Collections.singletonList(new RecommendationSummary(1, "a", 1, "c")), Collections.singletonList(new ReviewSummary(1, "a", "s", "c")), null);
        postAndVerifyProduct(productAggregate, HttpStatus.OK);
        assertEquals(1, queueProducts.size());
        EventModel<Integer, Product> expectedProductEventModel = new EventModel<>(EventModel.TYPE.CREATE, productAggregate.getProductID(), new Product(productAggregate.getProductID(), productAggregate.getName(), productAggregate.getWeight(), null));
        assertThat(queueProducts, receivesPayloadThat(sameEventExceptCreatedAt(expectedProductEventModel)));
        assertEquals(1, queueRecommendations.size());
        assertEquals(1, queueReviews.size());
        RecommendationSummary recommendationSummary = productAggregate.getRecommendations().get(0);
        EventModel<Integer, Recommendation> expectedRecommendationEventModel = new EventModel<>(EventModel.TYPE.CREATE, productAggregate.getProductID(), new Recommendation(productAggregate.getProductID(), recommendationSummary.getRecommendationID(), recommendationSummary.getAuthor(), recommendationSummary.getRate(), recommendationSummary.getContent(), null));
        assertThat(queueRecommendations, receivesPayloadThat(sameEventExceptCreatedAt(expectedRecommendationEventModel)));
        assertEquals(1, queueReviews.size());
        ReviewSummary reviewSummary = productAggregate.getReviews().get(0);
        EventModel<Integer, Review> expectedReviewEventModel = new EventModel<>(EventModel.TYPE.CREATE, productAggregate.getProductID(), new Review(productAggregate.getProductID(), reviewSummary.getReviewID(), reviewSummary.getAuthor(), reviewSummary.getSubject(), reviewSummary.getContent(), null));
        assertThat(queueReviews, receivesPayloadThat(sameEventExceptCreatedAt(expectedReviewEventModel)));
    }

    @Test
    void deleteCompositeProduct() {
        deleteAndVerifyProduct(1, HttpStatus.OK);
        assertEquals(1, queueProducts.size());
        EventModel<Integer, Product> expectedEvent = new EventModel<>(EventModel.TYPE.DELETE, 1, null);
        System.out.println(expectedEvent);
        LOGGER.info("Expected Event : {}", expectedEvent.toString());
        LOGGER.info("Actual One is: {}", sameEventExceptCreatedAt(expectedEvent).toString());
        assertThat(queueProducts, is(receivesPayloadThat(sameEventExceptCreatedAt(expectedEvent))));
        assertEquals(1, queueRecommendations.size());
        EventModel<Integer, Recommendation> expectedRecommendationEvent = new EventModel<>(EventModel.TYPE.DELETE, 1, null);
        assertThat(queueRecommendations, receivesPayloadThat(sameEventExceptCreatedAt(expectedRecommendationEvent)));
        assertEquals(1, queueReviews.size());
        EventModel<Integer, Review> expectedReviewEvent = new EventModel<>(EventModel.TYPE.DELETE, 1, null);
        assertThat(queueReviews, receivesPayloadThat(sameEventExceptCreatedAt(expectedReviewEvent)));
    }

    private void postAndVerifyProduct(ProductAggregate productAggregate, HttpStatus expectedStatus) {
        webTestClient.post().uri("/product-composite").body(Mono.just(productAggregate), ProductAggregate.class).exchange().expectStatus().isEqualTo(expectedStatus);
    }

    private void deleteAndVerifyProduct(int productID, HttpStatus expectedStatus) {
        webTestClient.delete().uri("/product-composite/" + productID).exchange().expectStatus().isEqualTo(expectedStatus);
    }

}
