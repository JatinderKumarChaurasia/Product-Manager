package com.jkc.microservices.composite.product.services;

import com.jkc.microservices.api.composite.product.*;
import com.jkc.microservices.api.core.product.Product;
import com.jkc.microservices.api.core.recommendation.Recommendation;
import com.jkc.microservices.api.core.review.Review;
import com.jkc.microservices.util.exceptions.NotFoundException;
import com.jkc.microservices.util.http.ServiceUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class ProductCompositeServiceImpl implements ProductCompositeService {

    private final ServiceUtil serviceUtil;
    private final ProductCompositeIntegration productCompositeIntegration;

    @Autowired
    public ProductCompositeServiceImpl(ServiceUtil serviceUtil,ProductCompositeIntegration productCompositeIntegration) {
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
    public ProductAggregate getProduct(int productID) {
        Product product = productCompositeIntegration.getProduct(productID);
        if (product == null) {
            throw new NotFoundException("No product found for productId: "+productID);
        }
        List<Recommendation> recommendations = productCompositeIntegration.getRecommendations(productID);
        List<Review> reviews = productCompositeIntegration.getReviews(productID);
        return createProductAggregate(product, recommendations, reviews, serviceUtil.getServiceAddress());
    }

    private ProductAggregate createProductAggregate(Product product,List<Recommendation> recommendations,List<Review> reviews,String serviceAddress) {
        int productID = product.getProductID();
        String name = product.getName();
        double weight = product.getWeight();
        List<RecommendationSummary> recommendationSummaries = (recommendations == null)?null:recommendations.stream().map(recommendation -> new RecommendationSummary(recommendation.getRecommendationID(),recommendation.getAuthor(),recommendation.getRate())).collect(Collectors.toList());
        List<ReviewSummary> reviewSummaries = (reviews == null)?null:reviews.stream().map(review -> new ReviewSummary(review.getReviewID(),review.getAuthor(),review.getSubject())).collect(Collectors.toList());
        String serviceProductAddress = product.getServiceAddress();
        String reviewAddress = (reviews != null && reviews.size() >0)? reviews.get(0).getServiceAddress():"";
        String recommendationAddress = (recommendations != null && recommendations.size() > 0) ? recommendations.get(0).getServiceAddress() : "";
        ServiceAddresses serviceAddresses = new ServiceAddresses(serviceAddress,serviceProductAddress,reviewAddress,recommendationAddress);
        return new ProductAggregate(productID,name,weight,recommendationSummaries,reviewSummaries,serviceAddresses);
    }
}
