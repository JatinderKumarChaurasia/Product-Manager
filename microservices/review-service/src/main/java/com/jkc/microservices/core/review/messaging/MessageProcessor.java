package com.jkc.microservices.core.review.messaging;

import com.jkc.microservices.api.core.review.Review;
import com.jkc.microservices.api.core.review.ReviewService;
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
    private final ReviewService reviewService;

    @Autowired
    public MessageProcessor(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @StreamListener(target = Sink.INPUT)
    public void processMessage(EventModel<Integer, Review> reviewEvent) {
        LOGGER.info("Process message created at {}...", reviewEvent.getEventCreatedAt());
        switch (reviewEvent.getEventType()) {
            case CREATE -> {
                Review review = reviewEvent.getData();
                LOGGER.info("Create review with ID: {}/{}", review.getProductID(), review.getReviewID());
                reviewService.createReview(review);
            }
            case DELETE -> {
                int productID = reviewEvent.getKey();
                LOGGER.info("Delete reviews with ProductID: {}", productID);
                reviewService.deleteReview(productID);
            }
            default -> {
                String errorMessage = "Incorrect event type: " + reviewEvent.getEventType() + ", expected a CREATE or DELETE event";
                LOGGER.warn(errorMessage);
                throw new EventProcessingException(errorMessage);
            }
        }
        LOGGER.info("Message Processing done At: {}", reviewEvent.getEventCreatedAt());
    }


}
