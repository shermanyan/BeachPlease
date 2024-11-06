package com.example.beachplease;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EditReviewActivity extends AppCompatActivity {

    private EditText reviewTextInput;
    private RatingBar ratingBar;
    private LinearLayout deleteReviewConfirmation;
    private Button confirmDeleteButton, cancelDeleteButton;

    private DatabaseReference reference;
    private FirebaseDatabase root;

    private final List<Uri> newImageUris = new ArrayList<>(); // For newly uploaded images
    private List<String> imageUrls = new ArrayList<>(); // Store existing image URLs
    private Storage storage;
    private ExecutorService executor;

    private Review review;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_review_view);

        storage = StorageOptions.getDefaultInstance().getService();
        executor = Executors.newSingleThreadExecutor();

        Intent intent = getIntent();
        review = intent.getParcelableExtra("review");



        root = FirebaseDatabase.getInstance("https://beachplease-439517-default-rtdb.firebaseio.com/");
        reference = root.getReference("reviews");

        reviewTextInput = findViewById(R.id.review_text_input);
        ratingBar = findViewById(R.id.review_rating_bar);
        deleteReviewConfirmation = findViewById(R.id.deleteReviewConfirmation);
        confirmDeleteButton = findViewById(R.id.confirmDelete);
        cancelDeleteButton = findViewById(R.id.cancelDelete);

        reviewTextInput.setText(review.getReviewText());
        ratingBar.setRating(review.getStars());

        if (review.getImageUrls() != null) {
            imageUrls = new ArrayList<>(review.getImageUrls());
            loadImages();
        }

        Button uploadImageButton = findViewById(R.id.upload_image_button);
        uploadImageButton.setOnClickListener(v -> openImagePicker());

        findViewById(R.id.review_submit_button).setOnClickListener(v -> {
            Log.d("EditReviewActivity", "Submit button clicked");
            String reviewText = reviewTextInput.getText().toString().trim();
            float rating = ratingBar.getRating();

            if (reviewText.isEmpty()) {
                Toast.makeText(this, "Review text cannot be empty.", Toast.LENGTH_SHORT).show();
            } else {

                        // Upload images first
                        uploadImages(new ImageUploadCallback() {
                            @Override
                            public void onUploadComplete(List<String> newImgUrls) {
                                // Merge existing image URLs with new ones
                                List<String> allImageUrls = new ArrayList<>(imageUrls);
                                allImageUrls.addAll(newImgUrls);

                                // Once images are uploaded, update the database
                                updateDatabase(review.getReviewId(), rating, reviewText, allImageUrls);
                                Log.d("ßEditReviewActivity", "Review updated successfully");
                            }

                            @Override
                            public void onUploadFailed(String errorMessage) {
                                runOnUiThread(() -> Toast.makeText(EditReviewActivity.this, "Image upload failed: " + errorMessage, Toast.LENGTH_SHORT).show());
                            }
                        });
                    }


        });


        findViewById(R.id.review_cancel_button).setOnClickListener(v -> finish());

        findViewById(R.id.delete_review).setOnClickListener(v -> {
            new AlertDialog.Builder(EditReviewActivity.this)
                    .setMessage("Are you sure you want to delete this review?")
                    .setPositiveButton("Yes", (dialog, which) -> deleteReview())
                    .setNegativeButton("No", null)
                    .show();
        });

    }

    private void updateDatabase(String reviewId, float rating, String reviewText, List<String> imgUrls) {
        if (reviewId == null || reviewId.isEmpty()) {
            runOnUiThread(() -> Toast.makeText(this, "Invalid review ID", Toast.LENGTH_SHORT).show());
            return;
        }

        DatabaseReference reviewReference = reference.child(reviewId);

        // Create a map of the fields to update
        Map<String, Object> updates = new HashMap<>();
        updates.put("stars", rating);
        updates.put("reviewText", reviewText);
        updates.put("imageUrls", imgUrls);

        // Update all fields at once
        reviewReference.updateChildren(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                runOnUiThread(() -> Toast.makeText(this, "Review updated successfully", Toast.LENGTH_SHORT).show());
                finish();
            } else {
                runOnUiThread(() -> Toast.makeText(this, "Failed to update review: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }


    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        imagePickerLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            Intent data = result.getData();
            if (data != null) {
                if (data.getClipData() != null) {
                    int count = data.getClipData().getItemCount();
                    for (int i = 0; i < count; i++) {
                        Uri imageUri = data.getClipData().getItemAt(i).getUri();
                        newImageUris.add(imageUri);
                    }
                } else if (data.getData() != null) {
                    Uri imageUri = data.getData();
                    newImageUris.add(imageUri);
                }
                loadImages();
            }
        }
    });

    private void loadImages() {
        LinearLayout imageContainer = findViewById(R.id.image_container);
        imageContainer.removeAllViews();


        if(imageUrls.isEmpty() && newImageUris.isEmpty()) {
            findViewById(R.id.image_container_scroll).setVisibility(View.GONE);
        } else {
            findViewById(R.id.image_container_scroll).setVisibility(View.VISIBLE);
        }

        // Load existing URLs
        for (String url : imageUrls) {
            addImageView(url, imageContainer, imageUrls);
        }

        // Load new URIs
        for (Uri uri : newImageUris) {
            addImageView(uri, imageContainer, newImageUris);
        }
    }

    private void addImageView(Object source, LinearLayout container, List<?> list) {
        View imageViewLayout = LayoutInflater.from(this).inflate(R.layout.review_image, container, false);
        ImageView imageView = imageViewLayout.findViewById(R.id.review_image);
        if (source instanceof String) {
            Picasso.get().load((String) source).into(imageView);
        } else if (source instanceof Uri) {
            Picasso.get().load((Uri) source).into(imageView);
        }
        container.addView(imageViewLayout);

        imageView.setOnLongClickListener(v -> {
            new AlertDialog.Builder(EditReviewActivity.this)
                    .setMessage("Do you want to delete this image?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        list.remove(source);
                        loadImages();
                    })
                    .setNegativeButton("No", null)
                    .show();
            return true;
        });
    }

    private void uploadImages(ImageUploadCallback callback) {
        List<String> newImageUrls = new ArrayList<>();
        for (Uri imageUri : newImageUris) {
            executor.submit(() -> {
                try {
                    String objectName = "edit_reviewImg-" + System.currentTimeMillis();
                    String contentType = getContentResolver().getType(imageUri);
                    InputStream inputStream = getContentResolver().openInputStream(imageUri);

                    storage.create(
                            Blob.newBuilder("beachplease_review_images", objectName)
                                    .setContentType(contentType)
                                    .build(),
                            inputStream
                    );
                    inputStream.close();

                    newImageUrls.add("https://storage.googleapis.com/beachplease_review_images/" + objectName);

                    if (newImageUrls.size() == newImageUris.size()) {
                        runOnUiThread(() -> callback.onUploadComplete(newImageUrls));
                    }
                } catch (Exception e) {
                    Log.e("UploadImages", "Image upload failed", e);
                    runOnUiThread(() -> callback.onUploadFailed(e.getMessage()));
                }
            });
        }
    }

    private void deleteReview() {
        String reviewId = review.getReviewId();

        Log.d("EditReviewActivity", "Deleting review with ID: " + reviewId);


        if (reviewId != null && !reviewId.isEmpty()) {
            reference.child(reviewId).removeValue()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(EditReviewActivity.this, "Review deleted successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(EditReviewActivity.this, "Failed to delete review", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(EditReviewActivity.this, "Review ID not found", Toast.LENGTH_SHORT).show();
        }
    }

    interface ImageUploadCallback {
        void onUploadComplete(List<String> newImageUrls);
        void onUploadFailed(String errorMessage);
    }
}
