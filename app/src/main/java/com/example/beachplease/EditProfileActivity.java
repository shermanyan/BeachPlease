package com.example.beachplease;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EditProfileActivity extends AppCompatActivity {
    private static final int IMAGE_PICKER_REQUEST_CODE = 1;
    private ImageView profilePicture;
    private ImageView editProfilePictureIcon;

    private TextInputLayout newUsernameLayout;
    private TextInputEditText newUsernameText;

    private TextInputLayout newPasswordLayout;
    private TextInputEditText newPasswordText;

    private TextInputLayout confirmNewPasswordLayout;
    private TextInputEditText confirmNewPasswordText;

    private User currentUser;
    private FirebaseAuth mAuth;

    private DatabaseReference reference;
    private FirebaseDatabase root;

    private Storage storage;
    private ExecutorService executor;


    private static final int PICK_IMAGE_REQUEST = 1001;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        root = FirebaseDatabase.getInstance("https://beachplease-439517-default-rtdb.firebaseio.com/");
        reference = root.getReference("users");

        storage = StorageOptions.getDefaultInstance().getService();
        executor = Executors.newSingleThreadExecutor();

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

        profilePicture = findViewById(R.id.profile_picture_edit);
        editProfilePictureIcon = findViewById(R.id.edit_picture_icon);

        getUserProfileImageUrl();

        editProfilePictureIcon.setOnClickListener(this::editProfilePicture);

        // Set OnLongClickListener to Remove Profile Picture
        profilePicture.setOnLongClickListener(v -> {
            showRemoveImageConfirmationDialog();
            return true;
        });

    }

    private void showRemoveImageConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Remove Profile Picture")
                .setMessage("Are you sure you want to remove your profile picture?")
                .setPositiveButton("Yes", (dialog, which) -> removeProfilePicture())
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void removeProfilePicture() {
        // Set the default profile picture
        profilePicture.setImageResource(R.drawable.profile_pic);

        // Update the Firebase database to remove the profile picture URL
        reference.child(mAuth.getCurrentUser().getUid()).child("profilePictureUrl").setValue(null)
                .addOnSuccessListener(aVoid -> {
                    currentUser.setProfilePictureUrl(null);
                    Toast.makeText(EditProfileActivity.this, "Profile picture removed successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("Remove Profile Picture", "Failed to remove profile picture: " + e.getMessage());
                    Toast.makeText(EditProfileActivity.this, "Failed to remove profile picture", Toast.LENGTH_SHORT).show();
                });
    }


    private void editProfilePicture(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, IMAGE_PICKER_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri selectedImageUri = data.getData();

                uploadProfilePic(selectedImageUri, new ImageUploadCallback() {
                    @Override
                    public void onUploadComplete(String imageUrl) {
                        Log.d("Upload", "Image uploaded successfully: " + imageUrl);
                        Toast.makeText(EditProfileActivity.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();

                        updateProfileImage(imageUrl);
                    }

                    @Override
                    public void onUploadFailed(String errorMessage) {
                        Log.e("Upload", "Image upload failed: " + errorMessage);
                        Toast.makeText(EditProfileActivity.this, "Image upload failed ", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    private void updateProfileImage(String imageUrl) {
        // Update the ImageView with latest profile image
        loadProfileImage(imageUrl);

        saveProfileImageUrl(imageUrl);
    }

    private void loadProfileImage(String url) {

        if (url != null) {
            Glide.with(this)
                    .load(url)
                    .apply(new RequestOptions().circleCrop())
                    .placeholder(R.drawable.profile_pic)
                    .error(R.drawable.profile_pic)
                    .into(profilePicture);
        }
    }

    protected void uploadProfilePic(Uri imageUri, ImageUploadCallback callback) {
        executor.execute(() -> {
            try {
                // Generate a unique name for the file
                String objectName = "profilePic-" + System.currentTimeMillis();
                String contentType = getContentResolver().getType(imageUri);
                InputStream inputStream = getContentResolver().openInputStream(imageUri);

                // Upload to Google Cloud Storage
                Blob blob = storage.create(
                        Blob.newBuilder("beachplease_profile_pics", objectName) // Ensure you're using your bucket name
                                .setContentType(contentType)
                                .build(),
                        inputStream
                );
                inputStream.close();

                // Get the public URL for the uploaded image
                String imageUrl = "https://storage.googleapis.com/beachplease_profile_pics/" + objectName;

                // Notify the callback with the uploaded image URL
                runOnUiThread(() -> callback.onUploadComplete(imageUrl));

            } catch (Exception e) {
                // Handle failure and notify the callback
                runOnUiThread(() -> callback.onUploadFailed(e.getMessage()));
            }
        });
    }


    private void saveProfileImageUrl(String imageUrl) {
        // Update the user object with the new profile image URL
        currentUser.setProfilePictureUrl(imageUrl);

        // Get the reference to the user's profile in Firebase Realtime Database
        reference.child(mAuth.getCurrentUser().getUid()).child("profilePictureUrl").setValue(imageUrl)
                .addOnSuccessListener(aVoid -> {
                    // Notify the user that the profile picture was updated successfully
                    Toast.makeText(EditProfileActivity.this, "Profile picture updated", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                    Log.e("Update Profile", "Failed to update profile image: " + e.getMessage());
                    Toast.makeText(EditProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                });
    }

    private String getUserProfileImageUrl() {
        reference.child(mAuth.getCurrentUser().getUid()).child("profilePictureUrl").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String imageUrl = snapshot.getValue(String.class);

                    Glide.with(EditProfileActivity.this)
                            .load(imageUrl)
                            .apply(new RequestOptions().circleCrop())
                            .placeholder(R.drawable.profile_pic)
                            .error(R.drawable.profile_pic)
                            .into(profilePicture);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return null;
    }

    public interface ImageUploadCallback {
        void onUploadComplete(String imageUrl);
        void onUploadFailed(String errorMessage);
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






