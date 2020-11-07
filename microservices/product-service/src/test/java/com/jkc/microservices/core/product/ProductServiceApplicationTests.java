package com.jkc.microservices.core.product;

import com.jkc.microservices.api.core.product.Product;
import com.jkc.microservices.core.product.models.ProductEntity;
import com.jkc.microservices.core.product.repositories.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {"spring.data.mongodb.port: 0"})
@AutoConfigureWebTestClient
class ProductServiceApplicationTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductServiceApplicationTests.class);

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    public void setupDb() {
        productRepository.deleteAll();
    }

    @Test
    void getProductByID() {
        int productID = 1;
        postAndVerifyProduct(productID, HttpStatus.OK);
        assertTrue(productRepository.findByProductID(productID).isPresent());
        getAndVerifyProduct(productID, HttpStatus.OK).jsonPath("$.productID").isEqualTo(productID);
//        webTestClient.get().uri("/product/" + productID).accept(APPLICATION_JSON).exchange().expectStatus().isOk().expectHeader().contentType(APPLICATION_JSON).expectBody().jsonPath("$.productID").isEqualTo(productID);
    }

    @Test
    void duplicateError() {
        int productID = 1;
        postAndVerifyProduct(productID, HttpStatus.OK);
        if (productRepository.findByProductID(productID).isPresent()) {
            System.out.println("iS PRESENT : " + productRepository.findByProductID(productID).get());
        }
        ProductEntity product = productRepository.findByProductID(productID).get();
        LOGGER.debug("Product : {}", product.getProductID());
        assertTrue(productRepository.findByProductID(productID).isPresent());
        postAndVerifyProduct(productID, HttpStatus.UNPROCESSABLE_ENTITY)
                .jsonPath("$.path").isEqualTo("/product");
//                .jsonPath("$.message").isEqualTo("Duplicate key, Product ID: " + productID);
    }

    @Test
    void deleteProduct() {
        int productID = 1;
        postAndVerifyProduct(productID, HttpStatus.OK);
        assertTrue(productRepository.findByProductID(productID).isPresent());
        deleteAndVerifyProduct(productID, HttpStatus.OK);
        assertFalse(productRepository.findByProductID(productID).isPresent());
        deleteAndVerifyProduct(productID, HttpStatus.OK);
    }

    @Test
    void getProductInvalidParameterString() {
        getAndVerifyProduct("/no-integer", HttpStatus.BAD_REQUEST)
                .jsonPath("$.path").isEqualTo("/product/no-integer")
                .jsonPath("$.message").isEqualTo("Type mismatch.");
    }

    @Test
    void getProductNotFound() {
        int productIdNotFound = 13;
        getAndVerifyProduct(productIdNotFound, HttpStatus.NOT_FOUND)
                .jsonPath("$.path").isEqualTo("/product/" + productIdNotFound)
                .jsonPath("$.message").isEqualTo("No product found for productID: " + productIdNotFound);
    }

    @Test
    void getProductInvalidParameterNegativeValue() {
        int productIdInvalid = -1;
        getAndVerifyProduct(productIdInvalid, HttpStatus.UNPROCESSABLE_ENTITY)
                .jsonPath("$.path").isEqualTo("/product/" + productIdInvalid)
                .jsonPath("$.message").isEqualTo("Invalid ProductID: " + productIdInvalid);
    }

    private WebTestClient.BodyContentSpec getAndVerifyProduct(int productID, HttpStatus expectedStatus) {
        return getAndVerifyProduct("/" + productID, expectedStatus);
    }

    private WebTestClient.BodyContentSpec getAndVerifyProduct(String productIDPath, HttpStatus expectedStatus) {
        return webTestClient.get().uri("/product" + productIDPath).accept(APPLICATION_JSON).exchange().expectStatus().isEqualTo(expectedStatus).expectHeader().contentType(APPLICATION_JSON).expectBody();
    }

    private WebTestClient.BodyContentSpec postAndVerifyProduct(int productID, HttpStatus expectedStatus) {
        Product product = new Product(productID, "Name " + productID, productID, "SA");
        return webTestClient.post()
                .uri("/product")
                .body(Mono.just(product).log(), Product.class)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody();
    }

    private WebTestClient.BodyContentSpec deleteAndVerifyProduct(int productID, HttpStatus expectedStatus) {
        return webTestClient.delete().uri("/product/" + productID).accept(APPLICATION_JSON).exchange().expectStatus().isEqualTo(expectedStatus).expectBody();
    }
}
