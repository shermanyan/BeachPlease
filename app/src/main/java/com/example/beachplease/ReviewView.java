package com.example.beachplease;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class  ReviewView extends LinearLayout {

    private static final int MAX_LINES_COLLAPSED = 4;

    public ReviewView(Context context) {
        super(context);
        init();
    }

    public ReviewView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        this.setOrientation(LinearLayout.VERTICAL);
    }

    private void loadImages(List<String> imgUrls, View reviewItem) {

        if (imgUrls == null || imgUrls.isEmpty()) {
            Log.d("ReviewView", "No images to load");
            return;
        }
        LinearLayout imageContainer = reviewItem.findViewById(R.id.image_container);
        reviewItem.findViewById(R.id.image_container_scroll).setVisibility(View.VISIBLE);
        imageContainer.removeAllViews();

        for (String url : imgUrls) {
            View imageViewLayout = LayoutInflater.from(getContext()).inflate(R.layout.review_image, imageContainer, false);
            ImageView imageView = imageViewLayout.findViewById(R.id.review_image);

            // Load the image using Picasso
            Picasso.get()
                    .load(url)
                    .into(imageView);

            imageContainer.addView(imageViewLayout);
        }
    }


    public void addReview(boolean editable, Review review) {

        // Inflate the review layout
        View reviewItem = LayoutInflater.from(getContext()).inflate(R.layout.review_item, this, false);

        if (editable) {
            // Add edit button
            ImageView editButton = reviewItem.findViewById(R.id.edit_review);
            editButton.setVisibility(View.VISIBLE);
            editButton.setOnClickListener(v -> {

                Intent intent = new Intent(getContext(), EditReviewActivity.class);

                intent.putExtra("reviewText", review.getReviewText());
                intent.putExtra("rating", review.getStars());
                getContext().startActivity(intent);
            });
        }
        // Initialize views in the review item
        TextView reviewUsername = reviewItem.findViewById(R.id.review_username);
        TextView reviewDate = reviewItem.findViewById(R.id.review_date);
        TextView reviewTextView = reviewItem.findViewById(R.id.review_text);
        RatingBar reviewStarRatingBar = reviewItem.findViewById(R.id.review_star_rating_bar);
        TextView seeMoreText = reviewItem.findViewById(R.id.see_more);

        // Set the review data
        reviewUsername.setText(String.valueOf(review.getUsername()));
        reviewDate.setText(review.getDate());
        reviewStarRatingBar.setRating(review.getStars());

        // Initial setup of the review text with limited lines
        reviewTextView.setText(review.getReviewText());
        reviewTextView.setMaxLines(MAX_LINES_COLLAPSED);
        seeMoreText.setVisibility(View.VISIBLE);


        // Set up "See More" functionality
        seeMoreText.setOnClickListener(v -> {
            if (seeMoreText.getText().equals("See More")) {
                reviewTextView.setMaxLines(Integer.MAX_VALUE);
                seeMoreText.setText("See Less");
            } else {
                reviewTextView.setMaxLines(MAX_LINES_COLLAPSED);
                seeMoreText.setText("See More");
            }
        });


        loadImages(review.getImageUrls(), reviewItem);

        this.addView(reviewItem);
    }

    public void clearReviews() {
        removeAllViews();

    }

    public void addReview(Review review) {
        addReview(false, review);
    }

}
