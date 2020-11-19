package com.jkc.microservices.core.review;

import com.jkc.microservices.api.core.product.Product;
import com.jkc.microservices.api.core.review.Review;
import com.jkc.microservices.api.event.EventModel;
import com.jkc.microservices.core.review.repositories.ReviewRepository;
import com.jkc.microservices.util.exceptions.InvalidInputException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.integration.channel.AbstractMessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {"spring.datasource.url=jdbc:h2:mem:review-db", "logging.level.com.jkc.microservices=DEBUG"})
@AutoConfigureWebTestClient
class ReviewServiceApplicationTests {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReviewServiceApplicationTests.class);

	@Autowired
	private WebTestClient webTestClient;

	@Autowired
	private ReviewRepository reviewRepository;

	@Autowired
	private Sink channels;

	private AbstractMessageChannel abstractMessageChannel = null;

	@BeforeEach
	void setUp() {
		abstractMessageChannel = (AbstractMessageChannel) channels.input();
		reviewRepository.deleteAll();
	}

	@Test
	void getReviewsByProductId() {
		int productId = 1;

		assertEquals(0, reviewRepository.findByProductID(productId).size());
		LOGGER.debug("Size is: " + reviewRepository.findByProductID(productId).size());
		sendCreateReviewEvent(productId, 1);
		sendCreateReviewEvent(productId, 2);
		sendCreateReviewEvent(productId, 3);
		assertEquals(3, reviewRepository.findByProductID(productId).size());
		LOGGER.debug("After insertion Size is: " + reviewRepository.findByProductID(productId).size());
		getAndVerifyReviewsByProductID(productId, HttpStatus.OK).jsonPath("$.length()").isEqualTo(3).jsonPath("$[2].productID").isEqualTo(productId).jsonPath("$[2].reviewID").isEqualTo(3);
	}

	@Test
	void duplicateError() {
		int productID = 1, reviewID = 1;
		assertEquals(0, reviewRepository.count());
		sendCreateReviewEvent(productID, reviewID);
		assertEquals(1, reviewRepository.count());
		try {
			sendCreateReviewEvent(productID, reviewID);
			fail("expected a MessagingException here!");
		} catch (MessagingException e) {
			if (e.getCause() instanceof InvalidInputException) {
				InvalidInputException invalidInputException = (InvalidInputException) e.getCause();
				assertEquals("Duplicate key, Product ID: 1, Review ID:1", invalidInputException.getMessage());
			} else {
				fail("Expected a InvalidInputException as the root cause!");
			}
		}
		assertEquals(1, reviewRepository.count());
	}

	@Test
	void deleteReviews() {
		int productID = 1;
		int reviewID = 1;
		sendCreateReviewEvent(productID, reviewID);
		assertEquals(1, reviewRepository.findByProductID(productID).size());
		sendAndDeleteReviewEvent(productID);
		assertEquals(0, reviewRepository.findByProductID(productID).size());
		sendAndDeleteReviewEvent(productID);
	}

	@Test
	void getReviewsMissingParameter() {
		getAndVerifyReviewsByProductID("", HttpStatus.BAD_REQUEST)
				.jsonPath("$.path").isEqualTo("/review")
				.jsonPath("$.message").isEqualTo("Required int parameter 'productID' is not present");
	}

	@Test
	void getReviewsInvalidParameter() {
		getAndVerifyReviewsByProductID("?productID=no-integer", HttpStatus.BAD_REQUEST)
				.jsonPath("$.path").isEqualTo("/review")
				.jsonPath("$.message").isEqualTo("Type mismatch.");
	}

	@Test
	void getReviewsNotFound() {
		int productIdNotFound = 213;
		getAndVerifyReviewsByProductID("?productID=" + productIdNotFound, HttpStatus.OK).jsonPath("$.length()").isEqualTo(0);
	}

	@Test
	void getReviewsInvalidParameterNegativeValue() {
		int productIdInvalid = -1;
		getAndVerifyReviewsByProductID("?productID=" + productIdInvalid, HttpStatus.UNPROCESSABLE_ENTITY)
				.jsonPath("$.path").isEqualTo("/review")
				.jsonPath("$.message").isEqualTo("Invalid productId: " + productIdInvalid);
	}

	private WebTestClient.BodyContentSpec getAndVerifyReviewsByProductID(int productID, HttpStatus expectedHttpStatus) {
		return getAndVerifyReviewsByProductID("?productID=" + productID, expectedHttpStatus);
	}

	private WebTestClient.BodyContentSpec getAndVerifyReviewsByProductID(String productIDQuery, HttpStatus expectedHttpStatus) {
		return webTestClient.get()
				.uri("/review" + productIDQuery)
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(expectedHttpStatus)
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody();
	}

	private void sendCreateReviewEvent(int productID, int reviewID) {
		Review review = new Review(productID, reviewID, "Author " + reviewID, "Subject " + reviewID, "Content " + reviewID, "SA");
		EventModel<Integer, Review> eventModel = new EventModel<>(EventModel.TYPE.CREATE, productID, review);
		abstractMessageChannel.send(new GenericMessage<>(eventModel));
	}

	private void sendAndDeleteReviewEvent(int productID) {
		EventModel<Integer, Product> productEventModel = new EventModel<>(EventModel.TYPE.DELETE, productID, null);
		abstractMessageChannel.send(new GenericMessage<>(productEventModel));
	}
}
