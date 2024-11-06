package com.example.beachplease;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class Review implements Parcelable {
    private String beachId;
    private String date;
    private String reviewText;
    private float stars;
    private String userId;
    private List<String> imageUrls;
    private String username;
    private String reviewId;

    // Default constructor for Firebase
    public Review() {
    }

    public Review(String beachId, String date, String reviewText, float stars, String userId) {
        this.beachId = beachId;
        this.date = date;
        this.reviewText = reviewText;
        this.stars = stars;
        this.userId = userId;
    }

    public Review(String username, String beachId, String date, String reviewText, float stars, String userId) {
        this.beachId = beachId;
        this.date = date;
        this.reviewText = reviewText;
        this.stars = stars;
        this.userId = userId;
        this.username = username;
    }

    // Parcelable implementation
    protected Review(Parcel in) {
        beachId = in.readString();
        date = in.readString();
        reviewText = in.readString();
        stars = in.readFloat();
        userId = in.readString();
        imageUrls = in.createStringArrayList();
        username = in.readString();
        reviewId = in.readString();
    }

    public static final Creator<Review> CREATOR = new Creator<Review>() {
        @Override
        public Review createFromParcel(Parcel in) {
            return new Review(in);
        }

        @Override
        public Review[] newArray(int size) {
            return new Review[size];
        }
    };

    @Override
    public int describeContents() {
        return 0; // No special objects in use
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(beachId);
        dest.writeString(date);
        dest.writeString(reviewText);
        dest.writeFloat(stars);
        dest.writeString(userId);
        dest.writeStringList(imageUrls);
        dest.writeString(username);
        dest.writeString(reviewId);
    }

    // Getters and Setters
    public String getBeachId() {
        return beachId;
    }

    public void setBeachId(String beachId) {
        this.beachId = beachId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public float getStars() {
        return stars;
    }

    public void setStar(float stars) {
        this.stars = stars;
    }

    public String getReviewText() {
        return reviewText;
    }

    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getReviewId() {
        return reviewId;
    }

    public void setReviewId(String reviewId) {
        this.reviewId = reviewId;
    }
}
