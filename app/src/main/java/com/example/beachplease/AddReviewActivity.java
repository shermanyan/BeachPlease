package com.example.beachplease;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


public class AddReviewActivity extends AppCompatActivity {

    private EditText reviewTextInput;
    private RatingBar ratingBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_review_view);

        // Initialize views
        reviewTextInput = findViewById(R.id.review_text_input);
        ratingBar = findViewById(R.id.review_rating_bar);

        // Handle submit button click
        findViewById(R.id.review_submit_button).setOnClickListener(v -> {
            // Collect user input
            String reviewText = reviewTextInput.getText().toString().trim();
            float rating = ratingBar.getRating();

            // Validate input
            if (reviewText.isEmpty() || rating == 0) {
                Toast.makeText(this, "Please complete all fields and provide a rating", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Review posted successfully", Toast.LENGTH_SHORT).show();

                updateDatabase(rating, reviewText);
                finish();

            }
        });

        findViewById(R.id.review_cancel_button).setOnClickListener(v -> {
            finish();
        });

    }

    private void updateDatabase(float rating, String reviewText) {

        // TODO - Implement database update logic


    }

}
