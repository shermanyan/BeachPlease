package com.example.beachplease;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

public class ReviewView extends LinearLayout {

    public ReviewView(Context context) {
        super(context);
        init();
    }

    private void init() {
        // Inflate the layout for the review view
        this.setOrientation(LinearLayout.VERTICAL);
        this.setVisibility(View.VISIBLE);
    }

    public void addReview(String username, String reviewText, String date, float rating) {
        // Inflate the review layout
        View reviewItem = LayoutInflater.from(getContext()).inflate(R.layout.review_item, this, false);

        // Initialize views in the review item
        TextView reviewUsername = reviewItem.findViewById(R.id.review_username);
        TextView reviewDate = reviewItem.findViewById(R.id.review_date);
        TextView reviewTextView = reviewItem.findViewById(R.id.review_text);
        RatingBar reviewStarRatingBar = reviewItem.findViewById(R.id.review_star_rating_bar);

        // Set the review data
        reviewUsername.setText(username);
        reviewDate.setText(date);
        reviewTextView.setText(reviewText);
        reviewStarRatingBar.setRating(rating);

        // Add the review item to the container
        this.addView(reviewItem);
    }
}