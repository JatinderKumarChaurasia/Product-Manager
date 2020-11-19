package com.jkc.microservices.composite.product.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jkc.microservices.api.core.product.Product;
import com.jkc.microservices.api.core.product.ProductService;
import com.jkc.microservices.api.core.recommendation.Recommendation;
import com.jkc.microservices.api.core.recommendation.RecommendationService;
import com.jkc.microservices.api.core.review.Review;
import com.jkc.microservices.api.core.review.ReviewService;
import com.jkc.microservices.api.event.EventModel;
import com.jkc.microservices.composite.product.configurations.MessageSources;
import com.jkc.microservices.util.exceptions.InvalidInputException;
import com.jkc.microservices.util.exceptions.NotFoundException;
import com.jkc.microservices.util.http.HttpErrorInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;

import static reactor.core.publisher.Flux.empty;

@EnableBinding(MessageSources.class)
@Component
public class ProductCompositeIntegration implements ProductService, RecommendationService, ReviewService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductCompositeIntegration.class);
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final String productServiceUrl;
    private final String recommendationServiceUrl;
    private final String reviewServiceUrl;

    private final MessageSources messageSources;

    @Autowired
    public ProductCompositeIntegration(
            WebClient.Builder webClientBuilder,
            ObjectMapper objectMapper,
            MessageSources messageSources,
            @Value("${app.product-service.host}") String productServiceHost,
            @Value("${app.product-service.port}") int productServicePort,
            @Value("${app.recommendation-service.host}") String recommendationServiceHost,
            @Value("${app.recommendation-service.port}") int recommendationServicePort,
            @Value("${app.review-service.host}") String reviewServiceHost,
            @Value("${app.review-service.port}") int reviewServicePort) {
        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;
        this.messageSources = messageSources;
        final String HTTP = "http://";
        productServiceUrl = HTTP + productServiceHost + ":" + productServicePort + "/product";
        recommendationServiceUrl = HTTP + recommendationServiceHost + ":" + recommendationServicePort + "/recommendation";
        reviewServiceUrl = HTTP + reviewServiceHost + ":" + reviewServicePort + "/review";
    }

    /**
     * curl $HOST:$PORT/product/1
     *
     * @param productID "productID : int"
     * @return Product, if found else null
     */
    @Override
    public Mono<Product> getProduct(int productID) {
        String url = productServiceUrl + "/" + productID;
        LOGGER.debug("will call getProduct api on url:{}", url);
        return webClient.get().uri(url).retrieve().bodyToMono(Product.class).log().onErrorMap(WebClientResponseException.class, this::handleException);
    }

    /**
     * curl -X POST $HOST:$PORT/product \
     * -H "Content-Type: application/json" --data \
     * '{"productID":123,"name":"product 123","weight":123}'
     *
     * @param product "description: product:Product"
     * @return "Product"
     */
    @Override
    public Product createProduct(Product product) {
        messageSources.outputProducts().send(MessageBuilder.withPayload(new EventModel<>(EventModel.TYPE.CREATE, product.getProductID(), product)).build());
        return product;
    }

    /**
     * curl -X DELETE $HOST:$PORT/product/1
     *
     * @param productID description="productID:int"
     */
    @Override
    public void deleteProduct(int productID) {
        messageSources.outputProducts().send(MessageBuilder.withPayload(new EventModel<>(EventModel.TYPE.DELETE, productID, null)).build());
    }

    /**
     * usage: curl $HOST:$PORT/recommendation?productID=1
     *
     * @param productID "provided productID: int"
     * @return list of recommendations for that @productID
     */
    @Override
    public Flux<Recommendation> getRecommendations(int productID) {
        String url = recommendationServiceUrl + "?productID=" + productID;
        LOGGER.debug("Will call getRecommendations API on URL: {}", url);
        return webClient.get().uri(url).retrieve().bodyToFlux(Recommendation.class).log().onErrorResume(error -> empty());
    }

    /**
     * usage: curl -X POST $HOST:$PORT/recommendation \
     * -H "Content-Type: application/json" --data \
     * '{"productID":123,"recommendationID":456,"author":"me","rate":5,"content":"yada, yada, yada"}'
     *
     * @param recommendation description: recommendation:Recommendation
     * @return Recommendation
     */
    @Override
    public Recommendation createRecommendation(Recommendation recommendation) {
        LOGGER.debug("Will post a new recommendation to URL: {}", recommendationServiceUrl);
        messageSources.outputRecommendations().send(MessageBuilder.withPayload(new EventModel<>(EventModel.TYPE.CREATE, recommendation.getProductID(), recommendation)).build());
        LOGGER.debug("Created a recommendation with id: {}", recommendation.getProductID());
        return recommendation;
    }

    /**
     * usage: curl -X DELETE $HOST:$PORT/recommendation?productID=1
     *
     * @param productID description =""
     */
    @Override
    public void deleteRecommendations(int productID) {
        messageSources.outputRecommendations().send(MessageBuilder.withPayload(new EventModel<>(EventModel.TYPE.DELETE, productID, null)).build());
    }

    /**
     * usage: curl $HOST:$PORT/review?productID=1
     *
     * @param productID "required productID int"
     * @return list of reviews for product associated with productID
     */
    @Override
    public Flux<Review> getReviews(int productID) {
        String url = reviewServiceUrl + "?productID=" + productID;
        LOGGER.debug("Will call getReviews API on URL: {}", url);
        return webClient.get().uri(url).retrieve().bodyToFlux(Review.class).onErrorResume(ex -> empty());
    }

    /**
     * usage: curl -X POST $HOST:$PORT/review \
     * -H "Content-Type: application/json" --data \
     * '{"productID":123,"reviewId":456,"author":"me","subject":"yada, yada, yada","content":"yada, yada, yada"}'
     *
     * @param review description: "review:Review"
     * @return Review
     */
    @Override
    public Review createReview(Review review) {
        messageSources.outputReviews().send(MessageBuilder.withPayload(new EventModel<>(EventModel.TYPE.CREATE, review.getProductID(), review)).build());
        LOGGER.debug("Created a review with id: {}", review.getProductID());
        return review;
    }

    /**
     * usage: curl -X DELETE $HOST:$PORT/review?productID=1
     *
     * @param productID description: "please provide productID:int"
     */
    @Override
    public void deleteReview(int productID) {
        messageSources.outputReviews().send(MessageBuilder.withPayload(new EventModel<>(EventModel.TYPE.DELETE, productID, null)).build());
    }

    private String getErrorMessage(HttpClientErrorException exception) {
        try {
            return objectMapper.readValue(exception.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        } catch (JsonProcessingException e) {
            return exception.getMessage();
        }
    }

    private String getErrorMessage(WebClientResponseException exception) {
        try {
            return objectMapper.readValue(exception.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        } catch (IOException e) {
            return exception.getMessage();
        }
    }

    private Throwable handleException(Throwable throwable) {
        if (!(throwable instanceof WebClientResponseException)) {
            LOGGER.warn("Got a unexpected error: {}, will rethrow it", throwable.toString());
            return throwable;
        }
        WebClientResponseException webClientResponseException = (WebClientResponseException) throwable;
        switch (webClientResponseException.getStatusCode()) {
            case NOT_FOUND -> throw new NotFoundException(getErrorMessage(webClientResponseException));
            case UNPROCESSABLE_ENTITY -> throw new InvalidInputException(getErrorMessage(webClientResponseException));
            default -> {
                LOGGER.warn("Got a unexpected HTTP error: {}, will rethrow it", webClientResponseException.getStatusCode());
                LOGGER.warn("Error body: {}", webClientResponseException.getResponseBodyAsString());
                return webClientResponseException;
            }
        }
    }

    public Mono<Health> getProductHealth() {
        return getHealth(productServiceUrl);
    }

    public Mono<Health> getRecommendationHealth() {
        return getHealth(recommendationServiceUrl);
    }

    public Mono<Health> getReviewHealth() {
        return getHealth(reviewServiceUrl);
    }

    private Mono<Health> getHealth(String url) {
        url += "/actuator/health";
        LOGGER.debug("Will call the Health API on URL: {}", url);
        return webClient.get().uri(url).retrieve().bodyToMono(String.class)
                .map(s -> new Health.Builder().up().build())
                .onErrorResume(ex -> Mono.just(new Health.Builder().down(ex).build()))
                .log();
    }
}
