package com.jkc.microservices.api.composite.product;

public class RecommendationSummary {
    private final int recommendationID;
    private final String author;
    private final double rate;
    private final String content;

    public RecommendationSummary() {
        this.recommendationID = 0;
        this.author = null;
        this.rate = 0;
        this.content = null;
    }

    public RecommendationSummary(int recommendationID, String author, double rate, String content) {
        this.recommendationID = recommendationID;
        this.author = author;
        this.rate = rate;
        this.content = content;
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

    public String getContent() {
        return content;
    }
}
