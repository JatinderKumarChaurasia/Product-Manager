package com.jkc.microservices.api.core.recommendation;

public class Recommendation {
    private int productID;
    private int recommendationID;
    private String author;
    private double rate;
    private String content;
    private String serviceAddress;

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

    public void setProductID(int productID) {
        this.productID = productID;
    }

    public int getRecommendationID() {
        return recommendationID;
    }

    public void setRecommendationID(int recommendationID) {
        this.recommendationID = recommendationID;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getServiceAddress() {
        return serviceAddress;
    }

    public void setServiceAddress(String serviceAddress) {
        this.serviceAddress = serviceAddress;
    }
}
