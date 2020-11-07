package com.jkc.microservices.api.composite.product;

public class ReviewSummary {
    private final int reviewID;
    private final String author;
    private final String subject;
    private final String content;

    public ReviewSummary() {
        this.reviewID = 0;
        this.author = null;
        this.subject = null;
        this.content = null;
    }

    public ReviewSummary(int reviewID, String author, String subject, String content) {
        this.reviewID = reviewID;
        this.author = author;
        this.subject = subject;
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

    public String getSubject() {
        return subject;
    }
}
