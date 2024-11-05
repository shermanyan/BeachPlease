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

import androidx.appcompat.app.AppCompatActivity;

public class UserProfileActivity extends AppCompatActivity {
    private View darkerBackground;

    private ImageView userImage;

    private LinearLayout logoutConfirmation;

    private User currentUser;

    private TextView profileNameView;
    private String profileName;
    private TextView fullNameView;
    private String fullname;

    private TextView emailView;
    private String email;

    private String userId;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile);

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