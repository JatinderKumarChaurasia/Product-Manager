package com.jkc.microservices.core.recommendation;

import com.jkc.microservices.api.core.recommendation.Recommendation;
import com.jkc.microservices.api.event.EventModel;
import com.jkc.microservices.core.recommendation.repositories.RecommendationRepository;
import com.jkc.microservices.util.exceptions.InvalidInputException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

@ExtendWith({SpringExtension.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {"spring.data.mongodb.port: 0"})
@AutoConfigureWebTestClient
class RecommendationServiceApplicationTests {

	@Autowired
	private WebTestClient webTestClient;

	@Autowired
	private RecommendationRepository recommendationRepository;

	@Autowired
	private Sink channels;

	private AbstractMessageChannel abstractMessageChannelInput = null;

	@BeforeEach
	void setup() {
		abstractMessageChannelInput = (AbstractMessageChannel) channels.input();
		recommendationRepository.deleteAll().block();
	}

	@Test
	void getRecommendationsByProductId() {
		int productID = 1;
		sendCreateRecommendationEvent(productID, 1);
		sendCreateRecommendationEvent(productID, 2);
		sendCreateRecommendationEvent(productID, 3);
		assertEquals(3, recommendationRepository.findByProductID(productID).count().block());
		getAndVerifyRecommendationsByProductID(productID, HttpStatus.OK)
				.jsonPath("$.length()").isEqualTo(3)
				.jsonPath("$[2].productID").isEqualTo(productID)
				.jsonPath("$[2].recommendationID").isEqualTo(3);

	}

	@Test
	void duplicateError() {
		int productID = 1, recommendationID = 1;
		sendCreateRecommendationEvent(productID, recommendationID);
		assertEquals(1, recommendationRepository.count().block());
		try {
			sendCreateRecommendationEvent(productID, recommendationID);
			fail("Expected a MessagingException here!");
		} catch (MessagingException messagingException) {
			if (messagingException.getCause() instanceof InvalidInputException) {
				InvalidInputException invalidInputException = (InvalidInputException) messagingException.getCause();
				assertEquals("Duplicate key, Product ID: 1, Recommendation ID:1", invalidInputException.getMessage());
			} else {
				fail("Expected a InvalidInputException as the root cause!");
			}
		}
		assertEquals(1, recommendationRepository.count().block());
	}


	@Test
	void deleteRecommendations() {
		int productID = 1;
		int recommendationID = 1;
		sendCreateRecommendationEvent(productID, recommendationID);
		assertEquals(1, recommendationRepository.findByProductID(productID).count().block());
		sendDeleteRecommendationEvent(productID);
		assertEquals(0, recommendationRepository.findByProductID(productID).count().block());
		sendDeleteRecommendationEvent(productID);
	}

	@Test
	void getRecommendationsMissingParameter() {
		getAndVerifyRecommendationsByProductID("", HttpStatus.BAD_REQUEST)
				.jsonPath("$.path").isEqualTo("/recommendation")
				.jsonPath("$.message").isEqualTo("Required int parameter 'productID' is not present");
	}

	@Test
	void getRecommendationsInvalidParameter() {
		getAndVerifyRecommendationsByProductID("?productID=no-integer", HttpStatus.BAD_REQUEST).jsonPath("$.path").isEqualTo("/recommendation")
				.jsonPath("$.message").isEqualTo("Type mismatch.");
	}

	@Test
	void getRecommendationsNotFound() {
		int productIdNotFound = 13;
		getAndVerifyRecommendationsByProductID(productIdNotFound, HttpStatus.OK).jsonPath("$.length()").isEqualTo(0);
	}

	@Test
	void getRecommendationsInvalidParameterNegativeValue() {
		int productIdInvalid = -1;
		getAndVerifyRecommendationsByProductID("?productID=" + productIdInvalid, HttpStatus.UNPROCESSABLE_ENTITY).jsonPath("$.path").isEqualTo("/recommendation").jsonPath("$.message").isEqualTo("Invalid productID: " + productIdInvalid);
	}

	private WebTestClient.BodyContentSpec getAndVerifyRecommendationsByProductID(int productID, HttpStatus expectedStatus) {
		return getAndVerifyRecommendationsByProductID("?productID=" + productID, expectedStatus);
	}

	private WebTestClient.BodyContentSpec getAndVerifyRecommendationsByProductID(String productIDQuery, HttpStatus expectedStatus) {
		return webTestClient.get().uri("/recommendation" + productIDQuery).accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isEqualTo(expectedStatus).expectHeader().contentType(MediaType.APPLICATION_JSON).expectBody();
	}

	private void sendCreateRecommendationEvent(int productID, int recommendationID) {
		Recommendation recommendation = new Recommendation(productID, recommendationID, "Author " + recommendationID, recommendationID, "Content " + recommendationID, "SA");
		EventModel<Integer, Recommendation> recommendationEventModel = new EventModel<>(EventModel.TYPE.CREATE, productID, recommendation);
		abstractMessageChannelInput.send(new GenericMessage<>(recommendationEventModel));
	}

	private void sendDeleteRecommendationEvent(int productID) {
		EventModel<Integer, Recommendation> recommendationEventModel = new EventModel<>(EventModel.TYPE.DELETE, productID, null);
		abstractMessageChannelInput.send(new GenericMessage<>(recommendationEventModel));
	}
}
