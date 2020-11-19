package com.jkc.microservices.core.product.messaging;

import com.jkc.microservices.api.core.product.Product;
import com.jkc.microservices.api.core.product.ProductService;
import com.jkc.microservices.api.event.EventModel;
import com.jkc.microservices.util.exceptions.EventProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;

@EnableBinding(value = {Sink.class})
public class MessageProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageProcessor.class);
    private final ProductService productService;

    @Autowired
    public MessageProcessor(ProductService productService) {
        this.productService = productService;
    }

    @StreamListener(value = Sink.INPUT)
    public void processMessage(EventModel<Integer, Product> productEventModel) {
        LOGGER.info("Process message created at {}...", productEventModel.getEventCreatedAt());
        switch (productEventModel.getEventType()) {
            case CREATE -> {
                Product product = productEventModel.getData();
                LOGGER.info("Create product with ID: {}", product.getProductID());
                productService.createProduct(product);
            }
            case DELETE -> {
                int productID = productEventModel.getKey();
                LOGGER.info("Delete recommendations with ProductID: {}", productID);
                productService.deleteProduct(productID);
            }
            default -> {
                String errorMessage = "Incorrect event type: " + productEventModel.getEventType() + ", expected a CREATE or DELETE event";
                LOGGER.warn(errorMessage);
                throw new EventProcessingException(errorMessage);
            }
        }
        LOGGER.info("Message processing done!");
    }
}
