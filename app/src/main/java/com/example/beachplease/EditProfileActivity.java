package com.example.beachplease;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditProfileActivity extends AppCompatActivity {

    private String userID;

    private TextInputLayout newUsernameLayout;
    private TextInputEditText newUsernameText;
    private TextInputLayout newEmailLayout;
    private TextInputEditText newEmailText;
    private TextInputLayout newPasswordLayout;
    private TextInputEditText newPassText;
    private PasswordHash passwordHash;
    private User currentUser;
    private TextView successMessage;
    private FirebaseDatabase root;
    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile);

        // Initialize Firebase
        root = FirebaseDatabase.getInstance("https://beachplease-439517-default-rtdb.firebaseio.com/");
        reference = root.getReference("users");

        // Initialize PasswordHash
        passwordHash = new PasswordHash();

        // Initialize Views
        findViewById(R.id.returnIcon).setOnClickListener(this::returnToProfile);
        successMessage = findViewById(R.id.success);

        // Initialize Input Fields
        newUsernameLayout = findViewById(R.id.editUsername);
        newUsernameText = (TextInputEditText) newUsernameLayout.getEditText();

        newEmailLayout = findViewById(R.id.editEmail);
        newEmailText = (TextInputEditText) newEmailLayout.getEditText();

        newPasswordLayout = findViewById(R.id.editPassword);
        newPassText = (TextInputEditText) newPasswordLayout.getEditText();

        // receive userId from intent
        Intent intent = getIntent();
        userID = intent.getStringExtra("com.example.beachplease.MESSAGE");
        Log.e("EditProfileActivity", "Current user id: " + userID);

        // Get current user
        currentUser = UserSession.getCurrentUser();
        if (currentUser == null) {
            Log.e("EditProfileActivity", "Current User is null!");
            // Handle the error - maybe redirect to login
            return;
        }

        // Optional: Pre-fill fields with current values
        if (currentUser.getUserName() != null) {
            newUsernameText.setHint(currentUser.getUserName());
        }
        if (currentUser.getEmail() != null) {
            newEmailText.setHint(currentUser.getEmail());
        }
    }

    private void returnToProfile(View view) {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }

    public void saveChanges(View view) {
        if (currentUser == null || currentUser.getId() == null) {
            Log.e("EditProfileActivity", "Current user or user ID is null!");
            return;
        }

        String newUsername = newUsernameText.getText() != null ? newUsernameText.getText().toString().trim() : "";
        String newEmail = newEmailText.getText() != null ? newEmailText.getText().toString().trim() : "";
        String newPassword = "";

        if (newPassText.getText() != null) {
            String tempPass = newPassText.getText().toString().trim();
            if (!tempPass.isEmpty()) {
                newPassword = passwordHash.hashPassword(tempPass);
            }
        }

        DatabaseReference userRef = reference.child(currentUser.getId());
        boolean successful = false;

        if (!newUsername.isEmpty()) {
            userRef.child("userName").setValue(newUsername);
            currentUser.setUserName(newUsername);
            successful = true;
        }

        if (!newEmail.isEmpty()) {
            userRef.child("email").setValue(newEmail);
            currentUser.setEmail(newEmail);
            successful = true;
        }

//        if (!newPassword.isEmpty()) {
//            userRef.child("password").setValue(newPassword);
//            currentUser.setPassword(newPassword);
//            successful = true;
//        }

        if (successful) {
            displayMessage();

        }
    }

    public void displayMessage() {
        successMessage.setVisibility(View.VISIBLE);
    }

    // ... rest of your methods ...
}

//public class EditProfileActivity extends AppCompatActivity {
//
//    private TextInputLayout newUsernameLayout;
//    private TextInputEditText newUsernameText;
//
//    private TextInputLayout newEmailLayout;
//    private TextInputEditText newEmailText;
//
//    private TextInputLayout newPasswordLayout;
//    private TextInputEditText newPassText;
//
//    private PasswordHash passwordHash; // Declare PasswordHash
//    private User currentUser;
//    private TextView successMessage;
//
//    // Firebase
//    private FirebaseDatabase root;
//    private DatabaseReference reference;
//
//    @SuppressLint("MissingInflatedId")
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.edit_profile);
//
//        // Initialize the PasswordHash object
//        passwordHash = new PasswordHash(); // Initialize passwordHash first
//
//        findViewById(R.id.returnIcon).setOnClickListener(this::returnToProfile);
//
//
//        successMessage = findViewById(R.id.success);
//
//        currentUser = UserSession.getCurrentUser();
//        // Debug log to see the current user
//        Log.d("ProfileActivity", "Current User: " + currentUser);
//
//    }
//
//    private void returnToProfile(View view) {
//        Intent intent = new Intent(this, ProfileActivity.class);
//        startActivity(intent);
//    }
//
//    public void saveChanges(View view) {
//
//
//
//        // Initialize the TextInputLayouts and EditTexts
//        newUsernameLayout = findViewById(R.id.editUsername);
//        newUsernameText = (TextInputEditText) newUsernameLayout.getEditText();
//
//        newEmailLayout = findViewById(R.id.editEmail);
//        newEmailText = (TextInputEditText) newEmailLayout.getEditText();
//
//        newPasswordLayout = findViewById(R.id.editPassword);
//        newPassText = (TextInputEditText) newPasswordLayout.getEditText();
//
//        root = FirebaseDatabase.getInstance("https://beachplease-439517-default-rtdb.firebaseio.com/");
//        reference = root.getReference("users");
//
//        if (currentUser == null) {
//            Log.e("EditProfileActivity", "Current user is null!");
//            return; // Exit the method if currentUser is not set
//        }
//
//        String newUsername = newUsernameText.getText() != null ? newUsernameText.getText().toString() : "";
//        String newEmail = newEmailText.getText() != null ? newEmailText.getText().toString() : "";
//        String newPassword = "";
//
//        if (newPassText.getText() != null) {
//            String tempPass = newPassText.getText().toString();
//            if (!tempPass.isEmpty()) {
//                newPassword = passwordHash.hashPassword(tempPass);
//            }
//        }
//
//        boolean successful = false;
//
//        if (!newUsername.isEmpty()) {
//            reference.child(currentUser.getId()).child("userName").setValue(newUsername);
//            currentUser.setUserName(newUsername);
//            successful = true;
//        }
//
//        if (!newEmail.isEmpty()) {
//            reference.child(currentUser.getId()).child("email").setValue(newEmail);
//            currentUser.setEmail(newEmail);
//            successful = true;
//        }
//
//        if (!newPassword.isEmpty()) {
//            reference.child(currentUser.getId()).child("password").setValue(newPassword);
//            currentUser.setPassword(newPassword);
//            successful = true;
//        }
//
//        if (successful) {
//            displayMessage();
//        }
//
//        Log.d("ProfileActivity", "CURRENT NAME: " + newUsername);
//    }
//
//    public void displayMessage() {
//        successMessage.setVisibility(View.VISIBLE);
//    }
//}