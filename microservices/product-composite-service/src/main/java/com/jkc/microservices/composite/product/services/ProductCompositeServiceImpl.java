package com.jkc.microservices.composite.product.services;

import com.jkc.microservices.api.composite.product.*;
import com.jkc.microservices.api.core.product.Product;
import com.jkc.microservices.api.core.recommendation.Recommendation;
import com.jkc.microservices.api.core.review.Review;
import com.jkc.microservices.util.exceptions.NotFoundException;
import com.jkc.microservices.util.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class ProductCompositeServiceImpl implements ProductCompositeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductCompositeServiceImpl.class);

    private final ServiceUtil serviceUtil;
    private final ProductCompositeIntegration productCompositeIntegration;

    @Autowired
    public ProductCompositeServiceImpl(ServiceUtil serviceUtil, ProductCompositeIntegration productCompositeIntegration) {
        this.serviceUtil = serviceUtil;
        this.productCompositeIntegration = productCompositeIntegration;
    }

    /**
     * usage : curl $HOST:$PORT/product-composite/1
     *
     * @param productID "productID: int required"
     * @return composite productInfo , if found else null
     */
    @Override
    public ProductAggregate getProductComposite(int productID) {
        LOGGER.debug("getCompositeProduct: lookup a product aggregate for productId: {}", productID);
        Product product = productCompositeIntegration.getProduct(productID);
        if (product == null) {
            throw new NotFoundException("No product found for productId: " + productID);
        }
        List<Recommendation> recommendations = productCompositeIntegration.getRecommendations(productID);
        List<Review> reviews = productCompositeIntegration.getReviews(productID);
        LOGGER.debug("getCompositeProduct: aggregate entity found for productId: {}", productID);
        return createProductAggregate(product, recommendations, reviews, serviceUtil.getServiceAddress());
    }

    private ProductAggregate createProductAggregate(Product product, List<Recommendation> recommendations, List<Review> reviews, String serviceAddress) {
        int productID = product.getProductID();
        String name = product.getName();
        double weight = product.getWeight();
        List<RecommendationSummary> recommendationSummaries = (recommendations == null) ? null : recommendations.stream().map(recommendation -> new RecommendationSummary(recommendation.getRecommendationID(), recommendation.getAuthor(), recommendation.getRate(), recommendation.getContent())).collect(Collectors.toList());
        List<ReviewSummary> reviewSummaries = (reviews == null) ? null : reviews.stream().map(review -> new ReviewSummary(review.getReviewID(), review.getAuthor(), review.getSubject(), review.getContent())).collect(Collectors.toList());
        String serviceProductAddress = product.getServiceAddress();
        String reviewAddress = (reviews != null && !reviews.isEmpty()) ? reviews.get(0).getServiceAddress() : "";
        String recommendationAddress = (recommendations != null && !recommendations.isEmpty()) ? recommendations.get(0).getServiceAddress() : "";
        ServiceAddresses serviceAddresses = new ServiceAddresses(serviceAddress, serviceProductAddress, reviewAddress, recommendationAddress);
        return new ProductAggregate(productID, name, weight, recommendationSummaries, reviewSummaries, serviceAddresses);
    }

    /**
     * usage: curl -X POST $HOST:$PORT/product-composite \
     * -H "Content-Type: application/json" --data \
     * '{"productId":123,"name":"product 123","weight":123}'
     *
     * @param productAggregate description: productAggregate:ProductAggregate
     */
    @Override
    public void createProductComposite(ProductAggregate productAggregate) {
        try {
            LOGGER.debug("createProductComposite: creates a composite product aggregate entity for productID: {}", productAggregate.getProductID());
            Product product = new Product(productAggregate.getProductID(), productAggregate.getName(), productAggregate.getWeight(), null);
            productCompositeIntegration.createProduct(product);
            if (productAggregate.getRecommendations() != null) {
                productAggregate.getRecommendations().forEach(recommendationSummary -> {
                    Recommendation recommendation = new Recommendation(productAggregate.getProductID(), recommendationSummary.getRecommendationID(), recommendationSummary.getAuthor(), recommendationSummary.getRate(), recommendationSummary.getContent(), null);
                    productCompositeIntegration.createRecommendation(recommendation);
                });
            }
            if (productAggregate.getReviews() != null) {
                productAggregate.getReviews().forEach(reviewSummary -> {
                    Review review = new Review(productAggregate.getProductID(), reviewSummary.getReviewID(), reviewSummary.getAuthor(), reviewSummary.getSubject(), reviewSummary.getContent(), null);
                    productCompositeIntegration.createReview(review);
                });
            }
            LOGGER.debug("createCompositeProduct: composite entites created for productId: {}", productAggregate.getProductID());
        } catch (RuntimeException ru) {
            LOGGER.warn("createCompositeProduct failed:{}", ru.toString());
            throw ru;
        }

    }

    /**
     * usage: curl -X DELETE $HOST:$PORT/product-composite/1
     *
     * @param productID description:"delete product-composite by id"
     */
    @Override
    public void deleteProductComposite(int productID) {
        LOGGER.debug("deleteCompositeProduct: Deletes a product aggregate for productId: {}", productID);
        productCompositeIntegration.deleteProduct(productID);
        productCompositeIntegration.deleteRecommendation(productID);
        productCompositeIntegration.deleteReview(productID);
        LOGGER.debug("getCompositeProduct: aggregate entities deleted for productId: {}", productID);
    }
}
