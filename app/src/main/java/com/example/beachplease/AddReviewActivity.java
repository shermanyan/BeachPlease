package com.example.beachplease;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;



public class AddReviewActivity extends AppCompatActivity {

    private EditText reviewTextInput;
    private RatingBar ratingBar;

    private String userId;
    private String beachId;

    private DatabaseReference reference;
    private FirebaseDatabase root;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_review_view);

        // get beach id and user id
        Intent intent = getIntent();
        beachId = intent.getStringExtra("id");

        try {
            userId = UserSession.getCurrentUser().getId();

        } catch (Exception e) {

            Toast.makeText(this, "Please login to post a review", Toast.LENGTH_SHORT).show();
            finish();
        }

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

            }
        });

        findViewById(R.id.review_cancel_button).setOnClickListener(v -> {
            finish();
        });

    }

    private void updateDatabase(float rating, String reviewText) {
        String date = new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(new Date());
        String reviewId = reference.push().getKey();
        Review review = new Review(beachId, date, reviewText, rating, userId);

        if (reviewId != null) {
            reference.child(reviewId).setValue(review)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(AddReviewActivity.this, "Review posted successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(AddReviewActivity.this, "Failed to post review", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(AddReviewActivity.this, "Error could not fetch ID", Toast.LENGTH_SHORT).show();
        }
    }

}


