package com.jkc.microservices.composite.product.services;

import com.jkc.microservices.api.composite.product.*;
import com.jkc.microservices.api.core.product.Product;
import com.jkc.microservices.api.core.recommendation.Recommendation;
import com.jkc.microservices.api.core.review.Review;
import com.jkc.microservices.util.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

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
    public Mono<ProductAggregate> getProductComposite(int productID) {
        LOGGER.debug("getCompositeProduct: lookup a product aggregate for productId: {}", productID);

        return Mono.zip(monoElement -> createProductAggregate((Product) monoElement[0], ((List<Recommendation>) monoElement[1]), (List<Review>) (monoElement[2]), serviceUtil.getServiceAddress()), productCompositeIntegration.getProduct(productID), productCompositeIntegration.getRecommendations(productID).collectList(), productCompositeIntegration.getReviews(productID).collectList()).doOnError(ex -> LOGGER.warn("get Composite Product failed: {}", ex.toString())).log();
    }

    private ProductAggregate createProductAggregate(Product product, List<Recommendation> recommendations, List<Review> reviews, String serviceAddress) {
        int productID = product.getProductID();
        String name = product.getName();
        double weight = product.getWeight();
        List<RecommendationSummary> recommendationSummaries = (recommendations == null) ? null :
                recommendations.stream().
                        map(recommendation -> new RecommendationSummary(recommendation.getRecommendationID(),
                                recommendation.getAuthor(), recommendation.getRate(), recommendation.getContent())).collect(Collectors.toList());
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
            LOGGER.debug("createCompositeProduct: composite entities created for productId: {}", productAggregate.getProductID());
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
        try {
            LOGGER.debug("deleteCompositeProduct: Deletes a product aggregate for productId: {}", productID);
            productCompositeIntegration.deleteProduct(productID);
            productCompositeIntegration.deleteRecommendations(productID);
            productCompositeIntegration.deleteReview(productID);
            LOGGER.debug("getCompositeProduct: aggregate entities deleted for productId: {}", productID);
        } catch (RuntimeException runtimeException) {
            LOGGER.warn("deleteCompositeProduct failed: {}", runtimeException.toString());
            throw runtimeException;
        }
    }
}
