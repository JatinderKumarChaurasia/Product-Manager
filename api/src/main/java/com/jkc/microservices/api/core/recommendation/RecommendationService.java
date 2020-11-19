package com.jkc.microservices.api.core.recommendation;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

public interface RecommendationService {
    /**
     * usage: curl $HOST:$PORT/recommendation?productID=1
     *
     * @param productID "provided productID: int"
     * @return list of recommendations for that @productID
     * @apiNote Get Flux<Recommendations> recommendations
     */

    @GetMapping(value = "/recommendation", produces = "application/json")
    Flux<Recommendation> getRecommendations(@RequestParam(value = "productID", required = true) int productID);

    /**
     * usage: curl -X POST $HOST:$PORT/recommendation \
     * -H "Content-Type: application/json" --data \
     * '{"productID":123,"recommendationID":456,"author":"me","rate":5,"content":"yada, yada, yada"}'
     *
     * @param recommendation description: recommendation:Recommendation
     * @return Recommendation
     * @apiNote Create recommendation
     */
    @PostMapping(value = "/recommendation", consumes = "application/json", produces = "application/json")
    Recommendation createRecommendation(@RequestBody Recommendation recommendation);

    /**
     * usage: curl -X DELETE $HOST:$PORT/recommendation?productID=1
     *
     * @param productID productID
     * @apiNote : Delete Recommendation By ProductID
     */
    @DeleteMapping("/recommendation")
    void deleteRecommendations(@RequestParam(value = "productID", required = true) int productID);
}
