package com.example.beachplease;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class AddReviewActivity extends AppCompatActivity {

    private EditText reviewTextInput;
    private RatingBar ratingBar;
    private final ArrayList<Uri> imageUris = new ArrayList<>();
    private String userId;
    private String beachId;

    private DatabaseReference reference;
    private FirebaseDatabase root;

    private Storage storage;
    private ExecutorService executor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_review_view);

        storage = StorageOptions.getDefaultInstance().getService();
        executor = Executors.newSingleThreadExecutor();

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

        reviewTextInput = findViewById(R.id.review_text_input);
        ratingBar = findViewById(R.id.review_rating_bar);

        Button uploadImageButton = findViewById(R.id.upload_image_button);
        uploadImageButton.setOnClickListener(v -> openImagePicker());

        findViewById(R.id.review_submit_button).setOnClickListener(v -> submitReview());
        findViewById(R.id.review_cancel_button).setOnClickListener(v -> finish());
    }

    private void submitReview() {
        String reviewText = reviewTextInput.getText().toString().trim();
        float rating = ratingBar.getRating();

        if (reviewText.isEmpty() || rating == 0) {
            Toast.makeText(this, "Please complete all fields and provide a rating", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Uploading Images...", Toast.LENGTH_SHORT).show();

        // Start image upload and update database after completion
        uploadImages(new ImageUploadCallback() {
            @Override
            public void onUploadComplete(List<String> imgUrls) {
                updateDatabase(rating, reviewText, imgUrls);
                finish();
            }

            @Override
            public void onUploadFailed(String errorMessage) {
                Toast.makeText(AddReviewActivity.this, "Image upload failed: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateDatabase(float rating, String reviewText, List<String> imageUrls) {
        String date = new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(new Date());
        String reviewId = reference.push().getKey();
        Review review = new Review(beachId, date, reviewText, rating, userId);
        review.setImageUrls(imageUrls);

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

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        imagePickerLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        if (data.getClipData() != null) {
                            int count = data.getClipData().getItemCount();
                            for (int i = 0; i < count; i++) {
                                Uri imageUri = data.getClipData().getItemAt(i).getUri();
                                imageUris.add(imageUri);
                            }
                        } else if (data.getData() != null) {
                            Uri imageUri = data.getData();
                            imageUris.add(imageUri);
                        }
                        loadImages(imageUris, findViewById(R.id.image_container_scroll));
                    }
                }
            });

    private void loadImages(List<Uri> imgUris, View reviewItem) {
        if (imgUris == null || imgUris.isEmpty()) {
            Log.d("ReviewView", "No images to load");
            return;
        }

        LinearLayout imageContainer = reviewItem.findViewById(R.id.image_container);
        reviewItem.findViewById(R.id.image_container_scroll).setVisibility(View.VISIBLE);
        imageContainer.removeAllViews();

        for (Uri uri : imgUris) {
            View imageViewLayout = LayoutInflater.from(this).inflate(R.layout.review_image, imageContainer, false);
            ImageView imageView = imageViewLayout.findViewById(R.id.review_image);

            Picasso.get().load(uri).into(imageView);

            imageView.setOnLongClickListener(v -> {
                new AlertDialog.Builder(this)
                        .setTitle("Delete Image")
                        .setMessage("Are you sure you want to delete this image?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            imageContainer.removeView(imageViewLayout);
                            imageUris.remove(uri);
                            dialog.dismiss();
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                        .show();
                return true;
            });

            imageContainer.addView(imageViewLayout);
        }
    }

    private void uploadImages(ImageUploadCallback callback) {
        executor.execute(() -> {
            List<String> imageUrls = new ArrayList<>();
            try {
                for (Uri imageUri : imageUris) {
                    String objectName = "reviewImg-" + System.currentTimeMillis();
                    String contentType = getContentResolver().getType(imageUri);
                    InputStream inputStream = getContentResolver().openInputStream(imageUri);

                    storage.create(
                            Blob.newBuilder("beachplease_review_images", objectName)
                                    .setContentType(contentType)
                                    .build(),
                            inputStream
                    );
                    inputStream.close();

                    imageUrls.add("https://storage.googleapis.com/beachplease_review_images/" + objectName);
                }
                runOnUiThread(() -> callback.onUploadComplete(imageUrls));
            } catch (Exception e) {
                runOnUiThread(() -> callback.onUploadFailed(e.getMessage()));
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }

    // Callback interface for image upload completion
    private interface ImageUploadCallback {
        void onUploadComplete(List<String> imgUrls);
        void onUploadFailed(String errorMessage);
    }
}

