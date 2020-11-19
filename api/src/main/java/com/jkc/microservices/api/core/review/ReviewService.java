package com.jkc.microservices.api.core.review;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

public interface ReviewService {
    /**
     * usage: curl $HOST:$PORT/review?productID=1
     *
     * @param productID "required productID int"
     * @return list of reviews for product associated with productID
     */

    @GetMapping(value = "/review", produces = "application/json")
    Flux<Review> getReviews(@RequestParam(value = "productID", required = true) int productID);

    /**
     * usage: curl -X POST $HOST:$PORT/review \
     * -H "Content-Type: application/json" --data \
     * '{"productID":123,"reviewId":456,"author":"me","subject":"yada, yada, yada","content":"yada, yada, yada"}'
     *
     * @param review description: "review:Review"
     * @return Review
     */
    @PostMapping(value = "/review", consumes = {"application/json"}, produces = {"application/json"})
    Review createReview(@RequestBody Review review);

    /**
     * usage: curl -X DELETE $HOST:$PORT/review?productID=1
     *
     * @param productID description: "please provide productID:int"
     */
    @DeleteMapping(value = "/review")
    void deleteReview(@RequestParam(value = "productID", required = true) int productID);

}
