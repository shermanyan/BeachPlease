package com.example.beachplease;

public class Review {
    private float rating;
    private String comment;
    private String userId;
    private String beachId;

    // default Constructor
    public Review() {}

    public Review(float rating, String comment, String userId, String beachId){
        this.rating = rating;
        this.comment = comment;
        this.userId = userId;
        this.beachId = beachId;
    }

    public float getRating() { return rating; }

    public void setRating(int rating) { this.rating = rating; }

    public String getComment() { return comment; }

    public void setComment(String comment) { this.comment = comment; }

    public String getUserId() { return userId; }

    public void setUserId(String userId) { this.userId = userId; }

    public String getBeachId() { return beachId; }

    public void setBeachId(String beachId) { this.beachId = beachId; }
}
