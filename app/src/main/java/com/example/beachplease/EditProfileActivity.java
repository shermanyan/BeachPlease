package com.example.beachplease;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class EditProfileActivity extends AppCompatActivity {

    private TextInputLayout newUsernameLayout;
    private TextInputEditText newUsernameText;

    private TextInputLayout newPasswordLayout;
    private TextInputEditText newPasswordText;

    private TextInputLayout confirmNewPasswordLayout;
    private TextInputEditText confirmNewPasswordText;

    private User currentUser;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();

        findViewById(R.id.returnIcon).setOnClickListener(this::returnToProfile);

        // Initialize Input Fields
        newUsernameLayout = findViewById(R.id.editUsername);
        newUsernameText = (TextInputEditText) newUsernameLayout.getEditText();

        newPasswordLayout = findViewById(R.id.editPassword); // For New Password
        newPasswordText = (TextInputEditText) newPasswordLayout.getEditText();

        confirmNewPasswordLayout = findViewById(R.id.confirmPassword); // For Confirm New Password
        confirmNewPasswordText = (TextInputEditText) confirmNewPasswordLayout.getEditText();

        // Get current user
        currentUser = UserSession.getCurrentUser();
        if (currentUser == null) {
            Log.e("EditProfileActivity", "Current User is null!");
            return;
        }

        if (currentUser.getUserName() != null) {
            newUsernameText.setHint(currentUser.getUserName());
        }
    }

    private void returnToProfile(View view) {
        Intent intent = new Intent(this, UserProfileActivity.class);
        startActivity(intent);
    }

    public void saveChanges(View view) {
        if (currentUser == null || currentUser.getId() == null) {
            Log.e("EditProfileActivity", "Current user or user ID is null!");
            return;
        }

        String newUsername = newUsernameText.getText() != null ? newUsernameText.getText().toString().trim() : "";
        String newPassword = newPasswordText.getText() != null ? newPasswordText.getText().toString().trim() : "";
        String confirmNewPassword = confirmNewPasswordText.getText() != null ? confirmNewPasswordText.getText().toString().trim() : "";

        DatabaseReference userRef = FirebaseDatabase.getInstance("https://your-firebase-database-url").getReference("users").child(currentUser.getId());
        boolean successful = false;

        // Update Username in Database
        if (!newUsername.isEmpty() && !newUsername.equals(currentUser.getUserName())) {
            userRef.child("userName").setValue(newUsername);
            currentUser.setUserName(newUsername);
            Toast.makeText(this, "username updated successfully", Toast.LENGTH_SHORT).show();
            successful = true;
        }

        // Update Password
        if (!newPassword.isEmpty() && newPassword.equals(confirmNewPassword)) {
            updatePassword(newPassword, view);
            successful = true;
        } else if (!newPassword.equals(confirmNewPassword)) {
            Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
        }

        if (successful) {
            Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
        }
    }

    private void updatePassword(String newPassword, View view) {
        FirebaseUser user = mAuth.getCurrentUser();

        user.updatePassword(newPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                        returnToProfile(view);
                    } else {
                        Toast.makeText(this, "Failed to update password. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}






