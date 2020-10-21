package com.jkc.microservices.api.composite.product;

public class ReviewSummary {
    private final int reviewID;
    private final String author;
    private final String content;

    public ReviewSummary(int reviewID, String author, String content) {
        this.reviewID = reviewID;
        this.author = author;
        this.content = content;
    }

    public int getReviewID() {
        return reviewID;
    }

    public String getContent() {
        return content;
    }

    public String getAuthor() {
        return author;
    }
}
