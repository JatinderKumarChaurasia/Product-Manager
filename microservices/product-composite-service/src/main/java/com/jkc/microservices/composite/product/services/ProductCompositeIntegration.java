package com.jkc.microservices.composite.product.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jkc.microservices.api.core.product.Product;
import com.jkc.microservices.api.core.product.ProductService;
import com.jkc.microservices.api.core.recommendation.Recommendation;
import com.jkc.microservices.api.core.recommendation.RecommendationService;
import com.jkc.microservices.api.core.review.Review;
import com.jkc.microservices.api.core.review.ReviewService;
import com.jkc.microservices.util.exceptions.InvalidInputException;
import com.jkc.microservices.util.exceptions.NotFoundException;
import com.jkc.microservices.util.http.HttpErrorInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Component
public class ProductCompositeIntegration implements ProductService, RecommendationService, ReviewService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductCompositeIntegration.class);
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String productServiceUrl;
    private final String recommendationServiceUrl;
    private final String reviewServiceUrl;

    @Autowired
    public ProductCompositeIntegration(
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            @Value("${app.product-service.host}") String productServiceHost,
            @Value("${app.product-service.port}") int productServicePort,
            @Value("${app.recommendation-service.host}") String recommendationServiceHost,
            @Value("${app.recommendation-service.port}") int recommendationServicePort,
            @Value("${app.review-service.host}") String reviewServiceHost,
            @Value("${app.review-service.port}") int reviewServicePort) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        String HTTP = "http://";
        productServiceUrl = HTTP + productServiceHost + ":" + productServicePort + "/product/";
        recommendationServiceUrl = HTTP + recommendationServiceHost + ":" + recommendationServicePort + "/recommendation?productID=";
        reviewServiceUrl = HTTP + reviewServiceHost + ":" + reviewServicePort + "/review?productID=";
    }

    /**
     * curl $HOST:$PORT/product/1
     *
     * @param productID "productID : int"
     * @return Product, if found else null
     */
    @Override
    public Product getProduct(int productID) {

        try {
            String url = productServiceUrl + productID;
            LOGGER.debug("will call getProduct api on url:{}", url);
            Product product = restTemplate.getForObject(url, Product.class);
            assert product != null;
            LOGGER.debug("found a product with id:{}", product.getProductID());
            return product;
        } catch (HttpClientErrorException exception) {
            switch (exception.getStatusCode()) {
                case NOT_FOUND -> throw new NotFoundException(getErrorMessage(exception));
                case UNPROCESSABLE_ENTITY -> throw new InvalidInputException(getErrorMessage(exception));
                default -> {
                    sonarResolution(exception);
                    throw exception;
                }

            }
        }
    }

    private void sonarResolution(HttpClientErrorException exception) {
        LOGGER.warn("Got a unexpected HTTP error: {}, will rethrow it", exception.getStatusCode());
        LOGGER.warn("Error body: {}", exception.getResponseBodyAsString());
    }

    /**
     * usage: curl $HOST:$PORT/recommendation?productID=1
     *
     * @param productID "provided productID: int"
     * @return list of recommendations for that @productID
     */
    @Override
    public List<Recommendation> getRecommendations(int productID) {
        try {
            String url = recommendationServiceUrl + productID;
            LOGGER.debug("Will call getRecommendations API on URL: {}", url);
            List<Recommendation> recommendations = restTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<List<Recommendation>>() {
            }).getBody();
            assert recommendations != null;
            LOGGER.debug("Found {} recommendations for a product with id: {}", recommendations.size(), productID);
            return recommendations;
        } catch (Exception exception) {
            LOGGER.warn("Got an exception while requesting recommendations, return zero recommendations: {}", exception.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * usage: curl $HOST:$PORT/review?productID=1
     *
     * @param productID "required productID int"
     * @return list of reviews for product associated with productID
     */
    @Override
    public List<Review> getReviews(int productID) {
        try {
            String url = reviewServiceUrl + productID;
            LOGGER.debug("Will call getReviews API on URL: {}", url);
            List<Review> reviews = restTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<List<Review>>() {
            }).getBody();
            assert reviews != null;
            LOGGER.debug("Found {} reviews for a product with id: {}", reviews.size(), productID);
            return reviews;
        } catch (Exception exception) {
            return new ArrayList<>();
        }
    }

    private String getErrorMessage(HttpClientErrorException exception) {
        try {
            return objectMapper.readValue(exception.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        } catch (JsonProcessingException e) {
            return exception.getMessage();
        }
    }
}
