package com.example.beachplease;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserProfileActivity extends AppCompatActivity {
    private View darkerBackground;

    private ImageView userImage;

    private LinearLayout logoutConfirmation;

    private User currentUser;
    private FirebaseAuth mAuth;

    private TextView profileNameView;
    private String profileName;
    private TextView fullNameView;
    private String fullname;

    private TextView emailView;
    private String email;

    private String userId;
    private String userProfilePicUrl;

    private DatabaseReference reference;
    private FirebaseDatabase root;



    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile);

        mAuth = FirebaseAuth.getInstance();
        root = FirebaseDatabase.getInstance("https://beachplease-439517-default-rtdb.firebaseio.com/");
        reference = root.getReference("users");

        darkerBackground = findViewById(R.id.darkOverlay);


        userImage = findViewById(R.id.userSampleAvatar);
        logoutConfirmation = findViewById(R.id.logoutConfirmation);

        currentUser = UserSession.getCurrentUser();

        if (currentUser == null) {
            Log.e("UserProfileActivity", "User not logged in. Redirecting to login.");
            //redirect to login screen
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        userId = currentUser.getId();

        // Debug log to see the current user
        Log.d("ProfileActivity", "Current User: " + currentUser);

        profileNameView = findViewById(R.id.profileusername);
        fullNameView = findViewById(R.id.profileFullName);
        emailView = findViewById(R.id.profileEmail);

        // Retrieve and Display user's info


        profileName = currentUser.getUserName();
        String fn = currentUser.getFirstName();
        String ln = currentUser.getLastName();
        fullname = fn + " " + ln;
        email = currentUser.getEmail();

        profileNameView.setText(profileName);
        fullNameView.setText(fullname);
        emailView.setText(email);

        getUserProfileImageUrl();
        // Set click listeners
        findViewById(R.id.lockIcon).setOnClickListener(this::showLogoutConfirmation);
        findViewById(R.id.location).setOnClickListener(this::backToMap);
        findViewById(R.id.profileEdit).setOnClickListener(this::editProfile);
        findViewById(R.id.viewReviews).setOnClickListener(this::viewReviews);

        Button confirmLogout = findViewById(R.id.confirmLogout);
        Button cancelLogout = findViewById(R.id.cancelLogout);

        confirmLogout.setOnClickListener(view -> {

            // Remove current user from session
            UserSession.logout();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        cancelLogout.setOnClickListener(view -> {
            // User canceled logout
            toggleView("PROFILE");
        });
    }

    private void backToMap(View view) {
        Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);
    }

    private void viewReviews(View view) {
        Intent intent = new Intent(this, ViewUserReviewsActivity.class);
        startActivity(intent);
    }

    private void editProfile(View view) {


        Intent intent = new Intent(this, EditProfileActivity.class);

        intent.putExtra("com.example.beachplease.MESSAGE", userId);

        startActivity(intent);

    }

    private String getUserProfileImageUrl() {
        reference.child(mAuth.getCurrentUser().getUid()).child("profilePictureUrl").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String imageUrl = snapshot.getValue(String.class);

                    Glide.with(UserProfileActivity.this)
                            .load(imageUrl)
                            .apply(new RequestOptions().circleCrop())
                            .placeholder(R.drawable.profile_pic)
                            .error(R.drawable.profile_pic)
                            .into(userImage);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return null;
    }

    private void showLogoutConfirmation(View view) {
        toggleView("LOGOUT_CONFIRMATION");
    }

    private void toggleView(String view) {
        switch (view) {
            case "LOGOUT_CONFIRMATION":
                darkerBackground.setVisibility(View.VISIBLE);
                userImage.setVisibility(View.INVISIBLE);
                profileNameView.setVisibility(View.INVISIBLE);
                logoutConfirmation.setVisibility(View.VISIBLE);
                break;

            case "PROFILE":
                darkerBackground.setVisibility(View.GONE);
                logoutConfirmation.setVisibility(View.GONE);
                profileNameView.setVisibility(View.VISIBLE);
                userImage.setVisibility(View.VISIBLE);
                break;
        }
    }



}