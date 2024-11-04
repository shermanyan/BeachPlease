package com.example.beachplease;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class AddReviewActivity extends AppCompatActivity {

    private EditText reviewTextInput;
    private RatingBar ratingBar;

    private String userId;
    private String beachId;
//    private String comment;
//    private float rating;

    private DatabaseReference reference;
    private FirebaseDatabase root;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_review_view);

        // get beach id and user id
        Intent intent = getIntent();
        beachId = intent.getStringExtra("id");
        userId = UserSession.getCurrentUser().getId();

        root = FirebaseDatabase.getInstance("https://beachplease-439517-default-rtdb.firebaseio.com/");
        reference = root.getReference("reviews");

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

        // Create new Review

        // Create unique Id for each review
        String reviewId = reference.push().getKey();

        Review review = new Review(rating, reviewText, userId, beachId);

        if (reviewId != null){
            reference.child(reviewId).setValue(review).
                    addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // link review id to beach-reviews table
                            DatabaseReference beachReviewReference = FirebaseDatabase.getInstance().
                                    getReference("beach-reviews").child(beachId).child(reviewId);
                            beachReviewReference.setValue(true).
                                    addOnCompleteListener(beachTask -> {
                                        if (beachTask.isSuccessful()) {
                                            Toast.makeText(AddReviewActivity.this, "Review linked to beach successfully", Toast.LENGTH_SHORT).show();
                                        }else {
                                            Toast.makeText(AddReviewActivity.this, "Failed to link review to beach", Toast.LENGTH_SHORT).show();
                                        }
                                    } );
                        }else {
                            Toast.makeText(AddReviewActivity.this, "Failed to post review", Toast.LENGTH_SHORT).show();
                        }
                    });
        }else {
            Toast.makeText(AddReviewActivity.this, "Error generating review ID", Toast.LENGTH_SHORT).show();
        }



    }

}

