package com.jkc.microservices.core.recommendation.services;

import com.jkc.microservices.api.core.recommendation.Recommendation;
import com.jkc.microservices.api.core.recommendation.RecommendationService;
import com.jkc.microservices.util.exceptions.InvalidInputException;
import com.jkc.microservices.util.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class RecommendationServiceImpl implements RecommendationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecommendationServiceImpl.class);
    private final ServiceUtil serviceUtil;

    @Autowired
    public RecommendationServiceImpl(ServiceUtil serviceUtil) {this.serviceUtil=serviceUtil;}
    /**
     * usage: curl $HOST:$PORT/recommendation?productID=1
     *
     * @param productID "provided productID: int"
     * @return list of recommendations for that @productID
     */
    @Override
    public List<Recommendation> getRecommendations(int productID) {
        if (productID<1) {throw new InvalidInputException("Invalid productID: " + productID); }
        if (productID == 13) {
            LOGGER.debug("No recommendation found for productID: {}",productID);
            return new ArrayList<>();
        }
        List<Recommendation> recommendations = new ArrayList<>();
        recommendations.add(new Recommendation(productID, 1, "Author 1", 1, "Content 1", serviceUtil.getServiceAddress()));
        recommendations.add(new Recommendation(productID, 2, "Author 2", 2, "Content 2", serviceUtil.getServiceAddress()));
        recommendations.add(new Recommendation(productID, 3, "Author 3", 3, "Content 3", serviceUtil.getServiceAddress()));
        LOGGER.debug("/recommendation response size: {}", recommendations.size());
        return recommendations;
    }
}
