package com.jkc.microservices.core.review.services;

import com.jkc.microservices.api.core.review.Review;
import com.jkc.microservices.api.core.review.ReviewService;
import com.jkc.microservices.core.review.mapper.ReviewMapper;
import com.jkc.microservices.core.review.model.ReviewEntity;
import com.jkc.microservices.core.review.repositories.ReviewRepository;
import com.jkc.microservices.util.exceptions.InvalidInputException;
import com.jkc.microservices.util.http.ServiceUtil;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;

import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Level;

@RestController
public class ReviewServiceImpl implements ReviewService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReviewServiceImpl.class);

    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;

    private final ServiceUtil serviceUtil;

    private final Scheduler scheduler;

    @Autowired
    public ReviewServiceImpl(ReviewRepository reviewRepository, ReviewMapper reviewMapper, ServiceUtil serviceUtil, Scheduler scheduler) {
        this.reviewRepository = reviewRepository;
        this.reviewMapper = reviewMapper;
        this.serviceUtil = serviceUtil;
        this.scheduler = scheduler;
    }

    /**
     * usage: curl $HOST:$PORT/review?productID=1
     *
     * @param productID "required productID int"
     * @return list of reviews for product associated with productID
     */
    @Override
    public Flux<Review> getReviews(int productID) {
        if (productID < 1) throw new InvalidInputException("Invalid productId: " + productID);
        return asyncFlux(() -> Flux.fromIterable(getByProductID(productID))).log(null, Level.FINE);
    }

    protected List<Review> getByProductID(int productID) {
        List<ReviewEntity> reviewEntities = reviewRepository.findByProductID(productID);
        List<Review> reviews = reviewMapper.reviewEntitiesToReviewApisList(reviewEntities);
        reviews.forEach(review -> review.setServiceAddress(serviceUtil.getServiceAddress()));

        LOGGER.debug("getReviews: response size: {}", reviews.size());
        return reviews;
    }

    /**
     * usage: curl -X POST $HOST:$PORT/review \
     * -H "Content-Type: application/json" --data \
     * '{"productId":123,"reviewId":456,"author":"me","subject":"s1, s2, s3","content":"c1, c2, c3"}'
     *
     * @param review description: "review:Review"
     * @return Review
     */
    @Override
    public Review createReview(Review review) {
        try {
            ReviewEntity reviewEntity = reviewMapper.reviewApiToReviewEntity(review);
            ReviewEntity newEntity = reviewRepository.save(reviewEntity);
            LOGGER.debug("createReview: created a reviewEntity: {}/{}", review.getProductID(), review.getReviewID());
            return reviewMapper.reviewEntityToReviewApi(newEntity);
        } catch (DataIntegrityViolationException exception) {
            throw new InvalidInputException("Duplicate key, Product ID: " + review.getProductID() + ", Review ID:" + review.getReviewID());
        }
    }

    /**
     * usage: curl -X DELETE $HOST:$PORT/review?productID=1
     *
     * @param productID description: "please provide productID:int"
     */
    @Override
    public void deleteReview(int productID) {
        LOGGER.debug("deleteReviews: tries to delete reviews for the product with productId: {}", productID);
        reviewRepository.deleteAll(reviewRepository.findByProductID(productID));
    }

    private <T> Flux<T> asyncFlux(Supplier<? extends Publisher<T>> publisherSupplier) {
        return Flux.defer(publisherSupplier).subscribeOn(scheduler);
    }
}
