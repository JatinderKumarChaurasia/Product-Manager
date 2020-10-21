package com.jkc.microservices.api.composite.product;

public class RecommendationSummary {
    private final int recommendationID;
    private final String author;
    private final double rate;

    public RecommendationSummary(int recommendationID, String author, double rate) {
        this.recommendationID = recommendationID;
        this.author = author;
        this.rate = rate;
    }

    public int getRecommendationID() {
        return recommendationID;
    }

    public String getAuthor() {
        return author;
    }

    public double getRate() {
        return rate;
    }
}
