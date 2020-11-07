package com.jkc.microservices.composite.product;

import com.jkc.microservices.api.composite.product.ProductAggregate;
import com.jkc.microservices.api.composite.product.RecommendationSummary;
import com.jkc.microservices.api.composite.product.ReviewSummary;
import com.jkc.microservices.api.core.product.Product;
import com.jkc.microservices.api.core.recommendation.Recommendation;
import com.jkc.microservices.api.core.review.Review;
import com.jkc.microservices.composite.product.services.ProductCompositeIntegration;
import com.jkc.microservices.util.exceptions.InvalidInputException;
import com.jkc.microservices.util.exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.when;
import static reactor.core.publisher.Mono.just;

@ExtendWith(SpringExtension.class)
@WebFluxTest
class ProductCompositeServiceApplicationTests {
    private static final int PRODUCT_ID_OK = 1;
    private static final int PRODUCT_ID_NOT_FOUND = 2;
    private static final int PRODUCT_ID_INVALID = 3;

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ProductCompositeIntegration productCompositeIntegration;

    @BeforeEach
    void setUp() {

        when(productCompositeIntegration.getProduct(PRODUCT_ID_OK)).
                thenReturn(new Product(PRODUCT_ID_OK, "name", 1, "mock-address"));
        when(productCompositeIntegration.getRecommendations(PRODUCT_ID_OK)).
                thenReturn(singletonList(new Recommendation(PRODUCT_ID_OK, 1, "author", 1, "content", "mock address")));
        when(productCompositeIntegration.getReviews(PRODUCT_ID_OK)).
                thenReturn(singletonList(new Review(PRODUCT_ID_OK, 1, "author", "subject", "content", "mock address")));

        when(productCompositeIntegration.getProduct(PRODUCT_ID_NOT_FOUND)).thenThrow(new NotFoundException("NOT FOUND: " + PRODUCT_ID_NOT_FOUND));

        when(productCompositeIntegration.getProduct(PRODUCT_ID_INVALID)).thenThrow(new InvalidInputException("INVALID: " + PRODUCT_ID_INVALID));
    }


    @Test
    void createCompositeProduct1() {
        ProductAggregate compositeProduct = new ProductAggregate(1, "name", 1, null, null, null);
        postAndVerifyProduct(compositeProduct, HttpStatus.OK);
    }

    @Test
    void createCompositeProduct2() {
        ProductAggregate compositeProduct = new ProductAggregate(1, "name", 1,
                singletonList(new RecommendationSummary(1, "a", 1, "c")),
                singletonList(new ReviewSummary(1, "a", "s", "c")), null);

        postAndVerifyProduct(compositeProduct, HttpStatus.OK);
    }

    @Test
    void deleteCompositeProduct() {
        ProductAggregate compositeProduct = new ProductAggregate(1, "name", 1,
                singletonList(new RecommendationSummary(1, "a", 1, "c")),
                singletonList(new ReviewSummary(1, "a", "s", "c")), null);
        postAndVerifyProduct(compositeProduct, HttpStatus.OK);
        deleteAndVerifyProduct(compositeProduct.getProductID(), HttpStatus.OK);
        deleteAndVerifyProduct(compositeProduct.getProductID(), HttpStatus.OK);
    }

    @Test
    void getProductById() {
        getAndVerifyProduct(PRODUCT_ID_OK, HttpStatus.OK)
//        webTestClient.get()
//                .uri("/product-composite/" + PRODUCT_ID_OK)
//                .accept(MediaType.APPLICATION_JSON)
//                .exchange()
//                .expectStatus().isOk()
//                .expectHeader().contentType(MediaType.APPLICATION_JSON)
//                .expectBody()
                .jsonPath("$.productID").isEqualTo(PRODUCT_ID_OK)
                .jsonPath("$.recommendations.length()").isEqualTo(1)
                .jsonPath("$.reviews.length()").isEqualTo(1);
    }

    @Test
    void getProductNotFound() {
        getAndVerifyProduct(PRODUCT_ID_NOT_FOUND, HttpStatus.NOT_FOUND)
//        webTestClient.get()
//                .uri("/product-composite/" + PRODUCT_ID_NOT_FOUND)
//                .accept(MediaType.APPLICATION_JSON)
//                .exchange()
//                .expectStatus().isNotFound()
//                .expectHeader().contentType(MediaType.APPLICATION_JSON)
//                .expectBody()
                .jsonPath("$.path").isEqualTo("/product-composite/" + PRODUCT_ID_NOT_FOUND)
                .jsonPath("$.message").isEqualTo("NOT FOUND: " + PRODUCT_ID_NOT_FOUND);
    }

    @Test
    void getProductInvalidInput() {
        getAndVerifyProduct(PRODUCT_ID_INVALID, HttpStatus.UNPROCESSABLE_ENTITY)
//
//        webTestClient.get()
//                .uri("/product-composite/" + PRODUCT_ID_INVALID)
//                .accept(MediaType.APPLICATION_JSON)
//                .exchange()
//                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
//                .expectHeader().contentType(MediaType.APPLICATION_JSON)
//                .expectBody()
                .jsonPath("$.path").isEqualTo("/product-composite/" + PRODUCT_ID_INVALID)
                .jsonPath("$.message").isEqualTo("INVALID: " + PRODUCT_ID_INVALID);
    }

    private WebTestClient.BodyContentSpec getAndVerifyProduct(int productID, HttpStatus expectedStatus) {
        return webTestClient.get()
                .uri("/product-composite/" + productID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();
    }

    private void postAndVerifyProduct(ProductAggregate compositeProduct, HttpStatus expectedStatus) {
        webTestClient.post()
                .uri("/product-composite")
                .body(just(compositeProduct), ProductAggregate.class)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus);
    }

    private void deleteAndVerifyProduct(int productID, HttpStatus expectedStatus) {
        webTestClient.delete()
                .uri("/product-composite/" + productID)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus);
    }

//	@Test
//	void contextLoads() {
//	}

}
