package com.jkc.microservices.core.recommendation.messaging;

import com.jkc.microservices.api.core.recommendation.Recommendation;
import com.jkc.microservices.api.core.recommendation.RecommendationService;
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
    private final RecommendationService recommendationService;

    @Autowired
    public MessageProcessor(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @StreamListener(target = Sink.INPUT)
    public void processMessage(EventModel<Integer, Recommendation> recommendationEventModel) {
        LOGGER.info("Process message created at {}...", recommendationEventModel.getEventCreatedAt());
        switch (recommendationEventModel.getEventType()) {
            case CREATE -> {
                Recommendation recommendation = recommendationEventModel.getData();
                LOGGER.info("Create recommendation with ID: {}/{}", recommendation.getProductID(), recommendation.getRecommendationID());
                recommendationService.createRecommendation(recommendation);
            }
            case DELETE -> {
                int productID = recommendationEventModel.getKey();
                LOGGER.info("Delete recommendations with ProductID: {}", productID);
                recommendationService.deleteRecommendations(productID);
            }
            default -> {
                String errorMessage = "Incorrect event type: " + recommendationEventModel.getEventType() + ", expected a CREATE or DELETE event";
                LOGGER.warn(errorMessage);
                throw new EventProcessingException(errorMessage);
            }
        }
        LOGGER.info("Message Processing Done!");
    }
}
