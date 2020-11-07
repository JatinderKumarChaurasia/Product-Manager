package com.jkc.microservices.api.core.review;

public class Review {
    private int productID;
    private int reviewID;
    private String author;
    private String subject;
    private String content;
    private String serviceAddress;

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

    public void setProductID(int productID) {
        this.productID = productID;
    }

    public int getReviewID() {
        return reviewID;
    }

    public void setReviewID(int reviewID) {
        this.reviewID = reviewID;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
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
