package com.jkc.microservices.api.core.review;

public class Review {
    private final int productID;
    private final int reviewID;
    private final String author;
    private final String subject;
    private final String content;
    private final String serviceAddress;

    public Review() {
        this.productID = 0;
        this.reviewID = 0;
        this.author = this.subject = this.content = this.serviceAddress = null;
    }

    public Review(int productID, int reviewID, String author, String subject, String content, String serviceAddress) {
        this.productID = productID;
        this.reviewID = reviewID;
        this.author = author;
        this.subject = subject;
        this.content = content;
        this.serviceAddress = serviceAddress;
    }

    public int getProductID() {
        return productID;
    }

    public int getReviewID() {
        return reviewID;
    }

    public String getAuthor() {
        return author;
    }

    public String getSubject() {
        return subject;
    }

    public String getContent() {
        return content;
    }

    public String getServiceAddress() {
        return serviceAddress;
    }
}
