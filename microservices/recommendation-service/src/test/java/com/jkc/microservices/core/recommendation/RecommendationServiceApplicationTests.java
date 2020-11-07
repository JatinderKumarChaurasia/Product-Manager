package com.jkc.microservices.core.recommendation;

import com.jkc.microservices.api.core.recommendation.Recommendation;
import com.jkc.microservices.core.recommendation.repositories.RecommendationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith({SpringExtension.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {"spring.data.mongodb.port: 0"})
@AutoConfigureWebTestClient
class RecommendationServiceApplicationTests {

	@Autowired
	private WebTestClient webTestClient;

	@Autowired
	private RecommendationRepository recommendationRepository;

	@BeforeEach
	void setup() {
		recommendationRepository.deleteAll();
	}

	@Test
	void getRecommendationsByProductId() {
		int productID = 1;
		postAndVerifyRecommendation(productID, 1, HttpStatus.OK);
		postAndVerifyRecommendation(productID, 2, HttpStatus.OK);
		postAndVerifyRecommendation(productID, 3, HttpStatus.OK);
		assertEquals(3, recommendationRepository.findByProductID(productID).size());
		getAndVerifyRecommendationsByProductID(productID, HttpStatus.OK)
				.jsonPath("$.length()").isEqualTo(3)
				.jsonPath("$[2].productID").isEqualTo(productID)
				.jsonPath("$[2].recommendationID").isEqualTo(3);

//		webTestClient.get()
//				.uri("/recommendation?productID=" + productID)
//				.accept(MediaType.APPLICATION_JSON)
//				.exchange()
//				.expectStatus().isOk()
//				.expectHeader().contentType(MediaType.APPLICATION_JSON)
//				.expectBody()
//				.jsonPath("$.length()").isEqualTo(3)
//				.jsonPath("$[0].productID").isEqualTo(productID);
	}

	@Test
	void duplicateError() {
		int productID = 1, recommendationID = 1;
		postAndVerifyRecommendation(productID, recommendationID, HttpStatus.OK).jsonPath("$.productID").isEqualTo(productID)
				.jsonPath("$.recommendationID").isEqualTo(recommendationID);
		assertEquals(1, recommendationRepository.count());
		postAndVerifyRecommendation(productID, recommendationID, HttpStatus.UNPROCESSABLE_ENTITY)
				.jsonPath("$.path").isEqualTo("/recommendation")
				.jsonPath("$.message").isEqualTo("Duplicate key, Product ID: 1, Recommendation ID:1");
		assertEquals(1, recommendationRepository.count());
	}


	@Test
	void deleteRecommendations() {
		int productID = 1;
		int recommendationID = 1;
		postAndVerifyRecommendation(productID, recommendationID, HttpStatus.OK);
		assertEquals(1, recommendationRepository.findByProductID(productID).size());
		deleteAndVerifyRecommendationsByProductID(productID, HttpStatus.OK);
		assertEquals(0, recommendationRepository.findByProductID(productID).size());
		deleteAndVerifyRecommendationsByProductID(productID, HttpStatus.OK);
	}

	@Test
	void getRecommendationsMissingParameter() {
		getAndVerifyRecommendationsByProductID("", HttpStatus.BAD_REQUEST)
				.jsonPath("$.path").isEqualTo("/recommendation")
				.jsonPath("$.message").isEqualTo("Required int parameter 'productID' is not present");
//		webTestClient.get()
//				.uri("/recommendation")
//				.accept(MediaType.APPLICATION_JSON)
//				.exchange()
//				.expectStatus().isEqualTo(HttpStatus.BAD_REQUEST)
//				.expectHeader().contentType(MediaType.APPLICATION_JSON)
//				.expectBody()
//				.jsonPath("$.path").isEqualTo("/recommendation")
//				.jsonPath("$.message").isEqualTo("Required int parameter 'productID' is not present");
	}

	@Test
	void getRecommendationsInvalidParameter() {
		getAndVerifyRecommendationsByProductID("?productID=no-integer", HttpStatus.BAD_REQUEST).jsonPath("$.path").isEqualTo("/recommendation")
				.jsonPath("$.message").isEqualTo("Type mismatch.");
//		webTestClient.get()
//				.uri("/recommendation?productID=no-integer")
//				.accept(MediaType.APPLICATION_JSON)
//				.exchange()
//				.expectStatus().isEqualTo(HttpStatus.BAD_REQUEST)
//				.expectHeader().contentType(MediaType.APPLICATION_JSON)
//				.expectBody()
//				.jsonPath("$.path").isEqualTo("/recommendation")
//				.jsonPath("$.message").isEqualTo("Type mismatch.");
	}

	@Test
	void getRecommendationsNotFound() {
		int productIdNotFound = 13;
		getAndVerifyRecommendationsByProductID(productIdNotFound, HttpStatus.OK).jsonPath("$.length()").isEqualTo(0);
//		webTestClient.get()
//				.uri("/recommendation?productID=" + productIdNotFound)
//				.accept(MediaType.APPLICATION_JSON)
//				.exchange()
//				.expectStatus().isOk()
//				.expectHeader().contentType(MediaType.APPLICATION_JSON)
//				.expectBody()
//				.jsonPath("$.length()").isEqualTo(0);
	}

	@Test
	void getRecommendationsInvalidParameterNegativeValue() {
		int productIdInvalid = -1;
		getAndVerifyRecommendationsByProductID("?productID=" + productIdInvalid, HttpStatus.UNPROCESSABLE_ENTITY).jsonPath("$.path").isEqualTo("/recommendation").jsonPath("$.message").isEqualTo("Invalid productID: " + productIdInvalid);
//		webTestClient.get()
//				.uri("/recommendation?productID=" + productIdInvalid)
//				.accept(MediaType.APPLICATION_JSON)
//				.exchange()
//				.expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
//				.expectHeader().contentType(MediaType.APPLICATION_JSON)
//				.expectBody()
//				.jsonPath("$.path").isEqualTo("/recommendation")
//				.jsonPath("$.message").isEqualTo("Invalid productID: " + productIdInvalid);
	}

	private WebTestClient.BodyContentSpec getAndVerifyRecommendationsByProductID(int productID, HttpStatus expectedStatus) {
		return getAndVerifyRecommendationsByProductID("?productID=" + productID, expectedStatus);
	}

	private WebTestClient.BodyContentSpec getAndVerifyRecommendationsByProductID(String productIDQuery, HttpStatus expectedStatus) {
		return webTestClient.get().uri("/recommendation" + productIDQuery).accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isEqualTo(expectedStatus).expectHeader().contentType(MediaType.APPLICATION_JSON).expectBody();
	}

	private WebTestClient.BodyContentSpec postAndVerifyRecommendation(int productID, int recommendationID, HttpStatus expectedStatus) {
		Recommendation recommendation = new Recommendation(productID, recommendationID, "Author " + recommendationID, recommendationID, "Content " + recommendationID, "SA");
		return webTestClient.post().uri("/recommendation").body(Mono.just(recommendation), Recommendation.class).accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isEqualTo(expectedStatus).expectHeader().contentType(MediaType.APPLICATION_JSON).expectBody();
	}


	private WebTestClient.BodyContentSpec deleteAndVerifyRecommendationsByProductID(int productID, HttpStatus expectedStatus) {
		return webTestClient.delete().uri("/recommendation?productID=" + productID).accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isEqualTo(expectedStatus).expectBody();
	}
}
