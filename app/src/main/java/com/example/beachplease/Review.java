package com.example.beachplease;

public class Review {
    private String beachId;
    private String date;
    private String reviewText;
    private float stars;
    private String userId;


    // Default constructor for Firebase
    public Review() {}

    public Review(String beachId, String date, String reviewText, float stars, String userId) {

        this.beachId = beachId;
        this.date = date;
        this.reviewText = reviewText;
        this.stars = stars;
        this.userId = userId;
    }

    // Getters and Setters
    public String getBeachId() { return beachId; }
    public void setBeachId(String beachId) { this.beachId = beachId; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public float getStars() { return stars; }
    public void setStar(float stars) { this.stars = stars; }

    public String getReviewText() { return reviewText; }
    public void setReviewText(String reviewText) { this.reviewText = reviewText; }





}