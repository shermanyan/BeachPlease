package com.example.beachplease;

public interface ReviewIdCallback {
    void onReviewIdFound(String reviewId);
    void onReviewIdNotFound();
}