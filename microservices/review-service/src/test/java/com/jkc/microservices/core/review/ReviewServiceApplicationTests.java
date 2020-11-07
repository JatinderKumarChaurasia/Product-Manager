package com.jkc.microservices.core.review;

import com.jkc.microservices.api.core.review.Review;
import com.jkc.microservices.core.review.repositories.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {"spring.datasource.url=jdbc:h2:mem:review-db"})
@AutoConfigureWebTestClient
class ReviewServiceApplicationTests {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReviewServiceApplicationTests.class);

	@Autowired
	private WebTestClient webTestClient;

	@Autowired
	private ReviewRepository reviewRepository;

	@BeforeEach
	void setUp() {
		reviewRepository.deleteAll();
	}


	@Test
	void getReviewsByProductId() {
		int productId = 1;

		assertEquals(0, reviewRepository.findByProductID(productId).size());
		LOGGER.debug("Size is: " + reviewRepository.findByProductID(productId).size());
		postAndVerifyReview(productId, 1, HttpStatus.OK);
		postAndVerifyReview(productId, 2, HttpStatus.OK);
		postAndVerifyReview(productId, 3, HttpStatus.OK);
		assertEquals(3, reviewRepository.findByProductID(productId).size());
		LOGGER.debug("After insertion Size is: " + reviewRepository.findByProductID(productId).size());
		getAndVerifyReviewsByProductID(productId, HttpStatus.OK).jsonPath("$.length()").isEqualTo(3).jsonPath("$[2].productID").isEqualTo(productId).jsonPath("$[2].reviewID").isEqualTo(3);
	}

	@Test
	void duplicateError() {
		int productID = 1, reviewID = 1;
		assertEquals(0, reviewRepository.count());
		postAndVerifyReview(productID, reviewID, HttpStatus.OK)
				.jsonPath("$.productID").isEqualTo(productID)
				.jsonPath("$.reviewID").isEqualTo(reviewID);
		assertEquals(1, reviewRepository.count());
		postAndVerifyReview(productID, reviewID, HttpStatus.UNPROCESSABLE_ENTITY).jsonPath("$.path").isEqualTo("/review").jsonPath("$.message").isEqualTo("Duplicate key, Product Id: 1, Review Id:1");
		assertEquals(1, reviewRepository.count());
	}

	@Test
	void deleteReviews() {
		int productID = 1;
		int reviewID = 1;
		postAndVerifyReview(productID, reviewID, HttpStatus.OK);
		assertEquals(1, reviewRepository.findByProductID(productID).size());
		deleteAndVerifyReviewsByProductID(productID, HttpStatus.OK);
		assertEquals(0, reviewRepository.findByProductID(productID).size());
		deleteAndVerifyReviewsByProductID(productID, HttpStatus.OK);
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

	private WebTestClient.BodyContentSpec postAndVerifyReview(int productID, int reviewID, HttpStatus httpStatusExpected) {
		Review review = new Review(productID, reviewID, "Author " + reviewID, "Subject " + reviewID, "Content " + reviewID, "serviceAddress");
		return webTestClient.post()
				.uri("/review")
				.body(Mono.just(review), Review.class)
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(httpStatusExpected)
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody();
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

	private WebTestClient.BodyContentSpec deleteAndVerifyReviewsByProductID(int productID, HttpStatus expectedHttpStatus) {
		return webTestClient.delete()
				.uri("/review?productID=" + productID)
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(expectedHttpStatus)
				.expectBody();
	}
}
