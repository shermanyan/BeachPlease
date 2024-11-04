package com.example.beachplease;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ViewUserReviewsActivity extends AppCompatActivity {

    private DatabaseReference reference;
    private ReviewView reviewView;
    private String userId;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_reviews);

        FirebaseDatabase root = FirebaseDatabase.getInstance("https://beachplease-439517-default-rtdb.firebaseio.com/");
        reference = root.getReference("reviews");


        reviewView = findViewById(R.id.review_view);


        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        }

        loadReviews();
    }

    private void loadReviews() {
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                reviewView.removeAllViews();

                for (DataSnapshot reviewSnapshot : snapshot.getChildren()) {
                    Review review = reviewSnapshot.getValue(Review.class);
                    if (review != null) {

                        if (userId.equals(review.getUserId())) {
                            addReviewToView(review);
                        }
                    } else {
                        Log.d("ViewUserReviewsActivity", "Null review found at snapshot: " + reviewSnapshot.getKey());
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ViewUserReviewsActivity.this, "Failed to load reviews.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addReviewToView(Review review) {
        String username = UserSession.getCurrentUser().getFirstName() + " " + UserSession.getCurrentUser().getLastName();
        reviewView.addReview(username, review.getReviewText(), review.getDate(), review.getStar());
    }
}