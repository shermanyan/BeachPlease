package com.example.beachplease;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EditReviewActivity extends AppCompatActivity {

    private EditText reviewTextInput;
    private RatingBar ratingBar;
    private LinearLayout deleteReviewConfirmation; // For delete confirmation
    private Button confirmDeleteButton, cancelDeleteButton; // Confirmation buttons

    private DatabaseReference reference;
    private FirebaseDatabase root;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_review_view);

        Intent intent = getIntent();
        String load_reviewText = intent.getStringExtra("reviewText");
        float load_rating = intent.getFloatExtra("rating", 0);

        root = FirebaseDatabase.getInstance("https://beachplease-439517-default-rtdb.firebaseio.com/");
        reference = root.getReference("reviews");

        // Initialize views
        reviewTextInput = findViewById(R.id.review_text_input);
        ratingBar = findViewById(R.id.review_rating_bar);
        deleteReviewConfirmation = findViewById(R.id.deleteReviewConfirmation);
        confirmDeleteButton = findViewById(R.id.confirmDelete);
        cancelDeleteButton = findViewById(R.id.cancelDelete);

        reviewTextInput.setText(load_reviewText);
        ratingBar.setRating(load_rating);

        findViewById(R.id.review_submit_button).setOnClickListener(v -> {
            String reviewText = reviewTextInput.getText().toString().trim();
            float rating = ratingBar.getRating();

            if (reviewText.isEmpty()) {
                // Show delete confirmation if review text is empty
                deleteReviewConfirmation.setVisibility(View.VISIBLE);
            } else {
                // Proceed with updating or saving the review
                findReviewIdByText(load_reviewText, new ReviewIdCallback() {
                    @Override
                    public void onReviewIdFound(String reviewId) {
                        updateDatabase(reviewId, rating, reviewText);
                    }

                    @Override
                    public void onReviewIdNotFound() {
                        Toast.makeText(EditReviewActivity.this, "Review not found.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        // Set listener for cancel button to finish the activity
        findViewById(R.id.review_cancel_button).setOnClickListener(v -> finish());

        // Confirmation button listeners
        confirmDeleteButton.setOnClickListener(v -> {
            // Proceed with deletion if 'Yes' is clicked
            findReviewIdByText(load_reviewText, new ReviewIdCallback() {
                @Override
                public void onReviewIdFound(String reviewId) {
                    reference.child(reviewId).removeValue()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(EditReviewActivity.this, "Review deleted successfully", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> Toast.makeText(EditReviewActivity.this, "Failed to delete review", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onReviewIdNotFound() {
                    Toast.makeText(EditReviewActivity.this, "Review not found.", Toast.LENGTH_SHORT).show();
                }
            });
        });

        cancelDeleteButton.setOnClickListener(v -> deleteReviewConfirmation.setVisibility(View.GONE));
    }

    // Method to find review ID by text
    private void findReviewIdByText(String reviewText, ReviewIdCallback callback) {
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot reviewSnapshot : snapshot.getChildren()) {
                    String currentReviewText = reviewSnapshot.child("reviewText").getValue(String.class);
                    if (reviewText.equals(currentReviewText)) {
                        String reviewId = reviewSnapshot.getKey();
                        callback.onReviewIdFound(reviewId);
                        return;
                    }
                }
                callback.onReviewIdNotFound();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onReviewIdNotFound();
            }
        });
    }

    // Method to update or delete the review in the database
    private void updateDatabase(String reviewId, float rating, String reviewText) {
        DatabaseReference reviewReference = reference.child(reviewId);
        reviewReference.child("stars").setValue(rating);
        reviewReference.child("reviewText").setValue(reviewText);
        Toast.makeText(this, "Review updated successfully", Toast.LENGTH_SHORT).show();
        finish();
    }

    interface ReviewIdCallback {
        void onReviewIdFound(String reviewId);
        void onReviewIdNotFound();
    }
}

