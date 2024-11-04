package com.example.beachplease;

import android.util.Log;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class BeachDetailActivity extends AppCompatActivity {

    private DatabaseReference reference;
    private FirebaseDatabase root;

    private Beach beach;

    private WeatherView weatherView;
    private ReviewView reviewView; // Update to use ReviewView
    private OverviewView overviewView; // Update to use ReviewView

    private TextView beachTitle;
    private TextView beachRating;
    private RatingBar starRatingBar;
    private TextView numRatings;
    private View writeReviewButton;

    private TextView overviewTab;
    private TextView reviewsTab;
    private TextView weatherTab;

    //variable caching names to resolve name-not-shown-in-reviews problem
    private HashMap<String, String> userNameCache = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beach_detail);

        beach = getIntent().getParcelableExtra("beach");

        root = FirebaseDatabase.getInstance("https://beachplease-439517-default-rtdb.firebaseio.com/");
        reference = root.getReference("users");

        // Initialize views
        LinearLayout mainContainer = findViewById(R.id.beach_view);
        beachTitle = findViewById(R.id.beach_title);
        beachRating = findViewById(R.id.beach_rating);
        starRatingBar = findViewById(R.id.star_rating_bar);
        numRatings = findViewById(R.id.num_ratings);

        // Tabs
        overviewTab = findViewById(R.id.tab_overview);
        reviewsTab = findViewById(R.id.tab_reviews);
        weatherTab = findViewById(R.id.tab_weather);

        overviewTab.setOnClickListener(this::switchTabs);
        reviewsTab.setOnClickListener(this::switchTabs);
        weatherTab.setOnClickListener(this::switchTabs);

        // Initialize OverviewTab


        overviewView = new OverviewView(mainContainer.getContext());
        mainContainer.addView(overviewView);
        overviewView.setVisibility(View.VISIBLE);

        // Initialize Add review button
        writeReviewButton = LayoutInflater.from(mainContainer.getContext()).inflate(R.layout.add_review_button, mainContainer, false);
        mainContainer.addView(writeReviewButton);
        findViewById(R.id.write_review_button).setOnClickListener(v -> {
            Intent intent = new Intent(this, AddReviewActivity.class);
            startActivity(intent);
        });
        writeReviewButton.setVisibility(View.GONE);

        // Initialize ReviewView
        reviewView = new ReviewView(mainContainer.getContext());
        mainContainer.addView(reviewView);
        reviewView.setVisibility(View.GONE);

        // Initialize WeatherView
        weatherView = new WeatherView(mainContainer.getContext(), beach);
        mainContainer.addView(weatherView);
        weatherView.setVisibility(View.GONE);

        retrieveBeachDetails();
    }

    private void switchTabs(View view) {
        Tab tab;
        int id = view.getId();

        if (id == R.id.tab_reviews) {
            tab = Tab.REVIEWS;
        } else if (id == R.id.tab_weather) {
            tab = Tab.WEATHER;
        } else {
            tab = Tab.OVERVIEW;
        }

        switch (tab) {
            case OVERVIEW:
                overviewTab.setTextColor(ContextCompat.getColor(this, R.color.oceanblue));
                reviewsTab.setTextColor(ContextCompat.getColor(this, R.color.dark_gray));
                weatherTab.setTextColor(ContextCompat.getColor(this, R.color.dark_gray));
                overviewView.setVisibility(View.VISIBLE);
                reviewView.setVisibility(View.GONE);
                weatherView.setVisibility(View.GONE);
                writeReviewButton.setVisibility(View.GONE);
                break;
            case REVIEWS:
                overviewTab.setTextColor(ContextCompat.getColor(this, R.color.dark_gray));
                reviewsTab.setTextColor(ContextCompat.getColor(this, R.color.oceanblue));
                weatherTab.setTextColor(ContextCompat.getColor(this, R.color.dark_gray));
                overviewView.setVisibility(View.GONE);
                reviewView.setVisibility(View.VISIBLE);
                weatherView.setVisibility(View.GONE);
                writeReviewButton.setVisibility(View.VISIBLE);
                break;
            case WEATHER:
                overviewTab.setTextColor(ContextCompat.getColor(this, R.color.dark_gray));
                reviewsTab.setTextColor(ContextCompat.getColor(this, R.color.dark_gray));
                weatherTab.setTextColor(ContextCompat.getColor(this, R.color.oceanblue));
                overviewView.setVisibility(View.GONE);
                reviewView.setVisibility(View.GONE);
                weatherView.setVisibility(View.VISIBLE);
                writeReviewButton.setVisibility(View.GONE);
                break;
        }
    }

    private enum Tab {
        OVERVIEW,
        REVIEWS,
        WEATHER
    }

    private void retrieveBeachDetails() {
//        Set beach details dynamically, fetch these from a server or database
//        beachTitle.setText(beach.getName());
//
//        beachRating.setText("4.5");
//        starRatingBar.setRating(4.5f);
//        numRatings.setText("(11,241)");
//
//        // Dynamically add reviews using ReviewView
//        // Fetch reviews from a server or database
//        reviewView.addReview("John Doe", "Great beach with warm water! The sand is soft and clean. The facilities are well-maintained. Lifeguards are attentive. Plenty of space to relax. Water activities available. Family-friendly environment. Beautiful scenery. Easy access to parking. Highly recommend!", "July 2024", 4.0f);
//        reviewView.addReview("Jane Smith", "Had a fantastic time! The beach was not too crowded. The weather was perfect. The water was clear and refreshing. Lots of food options nearby. Friendly locals. Safe for kids. Clean restrooms. Great for a day trip. Will visit again!", "August 2024", 5.0f);
//        reviewView.addReview("Alice Johnson", "The sunsets here are breathtaking! Perfect spot for evening walks. The beach is very clean. Quiet and peaceful atmosphere. Ideal for photography. Plenty of seating areas. Good for picnics. Nice breeze. Romantic setting. A must-visit!", "June 2024", 5.0f);
//        reviewView.addReview("Bob Lee", "Nice beach, but it can get crowded on weekends. Early mornings are the best time to visit. The water is warm. Good for swimming. Limited shade areas. Bring your own umbrella. Parking can be a challenge. Lots of activities. Great for families. Worth a visit!", "July 2024", 3.5f);
//        reviewView.addReview("Sarah Connor", "Perfect spot for a family picnic. Clean and safe! Plenty of picnic tables. BBQ areas available. Kids loved the playground. The beach is well-maintained. Friendly staff. Easy to find. Good for large groups. Affordable parking. Highly recommend!", "August 2024", 4.5f);
//        reviewView.addReview("Mike Brown", "The water was a bit chilly, but still enjoyable! The beach is clean. Not too crowded. Good for a quick dip. Nice walking paths. Plenty of seating. Good for sunbathing. Close to amenities. Friendly atmosphere. Great for a short visit. Will come back!", "September 2024", 4.0f);
//        reviewView.addReview("Emily Davis", "Great facilities and friendly staff. Will definitely come back! The beach is clean. Lots of activities. Good for families. Safe environment. Easy access. Plenty of parking. Nice picnic areas. Beautiful views. Highly recommend!", "August 2024", 5.0f);
//        reviewView.addReview("Chris Evans", "Very clean beach, but parking can be a hassle. Arrive early to find a spot. The water is clear. Good for swimming. Nice walking paths. Plenty of seating. Friendly locals. Good for families. Lots of activities. Worth a visit!", "July 2024", 3.0f);
//        reviewView.addReview("Jessica White", "Amazing beach volleyball courts and fun activities! The beach is clean. Good for groups. Plenty of space. Friendly atmosphere. Lots of amenities. Easy access. Great for sports. Beautiful views. Highly recommend!", "August 2024", 4.8f);
//        reviewView.addReview("David Smith", "A hidden gem! Perfect for a quiet day by the water. The beach is clean. Not too crowded. Good for relaxation. Nice walking paths. Plenty of seating. Friendly locals. Good for families. Beautiful scenery. Will visit again!", "July 2024", 5.0f);

        beachTitle.setText(beach.getName());
        DatabaseReference reviewsRef = root.getReference("reviews");

        reviewsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                float totalRating = 0f;
                int reviewCount = 0;

                for (DataSnapshot reviewSnapshot : dataSnapshot.getChildren()) {
                    String userId = reviewSnapshot.child("userId").getValue(String.class);
                    String beachId = reviewSnapshot.child("beachId").getValue(String.class);
                    String date = reviewSnapshot.child("date").getValue(String.class);
                    String reviewText = reviewSnapshot.child("reviewText").getValue(String.class);
                    Float stars = reviewSnapshot.child("stars").getValue(Float.class);

                    //check if review match beach
                    if (beachId != null && beachId.equals(beach.getId()) && stars != null) {
                        //find user name in cache
                        if (userNameCache.containsKey(userId)) {
                            String userName = userNameCache.get(userId);
                            reviewView.addReview(userName, reviewText, date, stars);
                        } else {
                            //get user name if not in cache from firebase
                            fetchUserName(userId, (userName) -> {
                                reviewView.addReview(userName, reviewText, date, stars);
                                userNameCache.put(userId, userName); // Cache the fetched name
                                Log.d("BeachDetailActivity", "User full name: "+userName);
                            });
                        }

                        totalRating += stars;
                        reviewCount++;
                    }
                }

                //rating average
                if (reviewCount > 0) {
                    float averageRating = totalRating/reviewCount;
                    beachRating.setText(String.format("%.1f", averageRating));
                    starRatingBar.setRating(averageRating);
                    numRatings.setText("(" + reviewCount + ")");
                } else {
                    beachRating.setText("No Ratings");
                    starRatingBar.setRating(0f);
                    numRatings.setText("(0)");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                beachRating.setText("Error loading ratings");
                starRatingBar.setRating(0f);
                numRatings.setText("(0)");
            }
        });
    }

    //callback user name interface
    interface UserNameCallback {
        void onUserNameFetched(String userName);
    }

    //callback get user name from firebase
    private void fetchUserName(String userId, UserNameCallback callback) {
        DatabaseReference userRef = root.getReference("users").child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String firstName = dataSnapshot.child("firstName").getValue(String.class);
                String lastName = dataSnapshot.child("lastName").getValue(String.class);
                String fullName = (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");

                callback.onUserNameFetched(fullName.trim());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("BeachDetailActivity", "Failed to load user name", databaseError.toException());
                callback.onUserNameFetched("Unknown User");
            }
        });
    }
}