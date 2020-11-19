package com.jkc.microservices.core.product;

import com.jkc.microservices.api.core.product.Product;
import com.jkc.microservices.api.event.EventModel;
import com.jkc.microservices.core.product.repositories.ProductRepository;
import com.jkc.microservices.util.exceptions.InvalidInputException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.http.HttpStatus;
import org.springframework.integration.channel.AbstractMessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {"spring.data.mongodb.port: 0"})
@AutoConfigureWebTestClient
class ProductServiceApplicationTests {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private Sink channels;

    private AbstractMessageChannel abstractMessageChannel = null;

    @BeforeEach
    public void setupDb() {
        abstractMessageChannel = (AbstractMessageChannel) channels.input();
        productRepository.deleteAll().block();
    }

    @Test
    void getProductByID() {
        int productID = 1;
        assertNull(productRepository.findByProductID(productID).block());
        assertEquals(0, productRepository.count().block());
        sendCreateProductEvent(productID);
        assertNotNull(productRepository.findByProductID(productID).block());
        assertEquals(1, productRepository.count().block());

        getAndVerifyProduct(productID, HttpStatus.OK).jsonPath("$.productID").isEqualTo(productID);
    }

    @Test
    void duplicateError() {
        int productID = 1;
        assertNull(productRepository.findByProductID(productID).block());
        sendCreateProductEvent(productID);
        assertNotNull(productRepository.findByProductID(productID).block());
        try {
            sendCreateProductEvent(productID);
            fail("Expected a MessagingException here!");
        } catch (MessagingException messagingException) {
            if (messagingException.getCause() instanceof InvalidInputException) {
                InvalidInputException invalidInputException = (InvalidInputException) messagingException.getCause();
                assertEquals("Duplicate key, Product ID: " + productID, invalidInputException.getMessage());
            } else {
                fail("Expected a InvalidInputException as the root cause!");
            }
        }
    }

    @Test
    void deleteProduct() {
        int productID = 1;
        sendCreateProductEvent(productID);
        assertNotNull(productRepository.findByProductID(productID).block());
        sendDeleteProductEvent(productID);
        assertNull(productRepository.findByProductID(productID).block());
        sendDeleteProductEvent(productID);
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

    private void sendCreateProductEvent(int productID) {
        Product product = new Product(productID, "Name " + productID, productID, "SA");
        EventModel<Integer, Product> productEventModel = new EventModel<>(EventModel.TYPE.CREATE, productID, product);
        abstractMessageChannel.send(new GenericMessage<>(productEventModel));
    }

    private void sendDeleteProductEvent(int productID) {
        EventModel<Integer, Product> productEventModel = new EventModel<>(EventModel.TYPE.DELETE, productID, null);
        abstractMessageChannel.send(new GenericMessage<>(productEventModel));
    }
}
