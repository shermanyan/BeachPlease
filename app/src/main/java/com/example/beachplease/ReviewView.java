package com.example.beachplease;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ReviewView extends LinearLayout {

    private static final int MAX_LINES_COLLAPSED = 4;

//
//    private final FirebaseDatabase root = FirebaseDatabase.getInstance("https://beachplease-439517-default-rtdb.firebaseio.com/");
//    private final DatabaseReference reference = root.getReference("reviews");


    public ReviewView(Context context) {
        super(context);
        init();
    }

    public ReviewView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private void init() {
        this.setOrientation(LinearLayout.VERTICAL);
        this.setVisibility(View.VISIBLE);
    }

    public void addReview(String username, String reviewText, String date, float rating) {
        addReview(false, username, reviewText, date, rating);
    }

    public void addReview(boolean editable, String username, String reviewText, String date, float rating) {

        // Inflate the review layout
        View reviewItem = LayoutInflater.from(getContext()).inflate(R.layout.review_item, this, false);

        if (editable) {
            // Add edit button
            ImageView editButton = reviewItem.findViewById(R.id.edit_review);
            editButton.setVisibility(View.VISIBLE);
            editButton.setOnClickListener(v -> {

                Intent intent = new Intent(getContext(), EditReviewActivity.class);

                intent.putExtra("reviewText", reviewText);
                intent.putExtra("rating", rating);
                getContext().startActivity(intent);
            });
        }
        // Initialize views in the review item
        TextView reviewUsername = reviewItem.findViewById(R.id.review_username);
        TextView reviewDate = reviewItem.findViewById(R.id.review_date);
        TextView reviewTextView = reviewItem.findViewById(R.id.review_text);
        RatingBar reviewStarRatingBar = reviewItem.findViewById(R.id.review_star_rating_bar);
        TextView seeMoreText = reviewItem.findViewById(R.id.see_more); // Add "See More" TextView in XML

        // Set the review data
        reviewUsername.setText(username);
        reviewDate.setText(date);
        reviewStarRatingBar.setRating(rating);

        // Initial setup of the review text with limited lines
        reviewTextView.setText(reviewText);
        reviewTextView.setMaxLines(MAX_LINES_COLLAPSED);
        seeMoreText.setVisibility(View.VISIBLE);

        // Set up "See More" functionality
        seeMoreText.setOnClickListener(v -> {
            if (seeMoreText.getText().equals("See More")) {
                reviewTextView.setMaxLines(Integer.MAX_VALUE); // Expand to show all lines
                seeMoreText.setText("See Less");
            } else {
                reviewTextView.setMaxLines(MAX_LINES_COLLAPSED); // Collapse to max lines
                seeMoreText.setText("See More");
            }
        });

        // Add the review item to the container
        this.init();
        this.addView(reviewItem);

    }

}
