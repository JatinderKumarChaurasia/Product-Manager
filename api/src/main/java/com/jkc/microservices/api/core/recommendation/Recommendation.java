package com.jkc.microservices.api.core.recommendation;

public class Recommendation {
    private final int productID;
    private final int recommendationID;
    private final String author;
    private final double rate;
    private final String content;
    private final String serviceAddress;

    public Recommendation() {
        this.productID = 0;
        this.recommendationID = 0;
        this.author = null;
        this.rate = 0;
        this.content = null;
        this.serviceAddress = null;
    }

    public Recommendation(int productID, int recommendationID, String author, double rate, String content, String serviceAddress) {
        this.productID = productID;
        this.recommendationID = recommendationID;
        this.author = author;
        this.rate = rate;
        this.content = content;
        this.serviceAddress = serviceAddress;
    }

    public int getProductID() {
        return productID;
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

    public String getServiceAddress() {
        return serviceAddress;
    }
}
