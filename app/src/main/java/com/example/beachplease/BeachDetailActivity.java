package com.example.beachplease;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

//Placeholder file
public class BeachDetailActivity extends AppCompatActivity {

    private DatabaseReference reference;
    private FirebaseDatabase root;
    private String userId;
    private TextView welcomeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beach_detail);

        root = FirebaseDatabase.getInstance("https://beachplease-439517-default-rtdb.firebaseio.com/");
        reference = root.getReference("users");

        userId = getIntent().getStringExtra("com.example.beachplease.MESSAGE");

        // Logging for debugging
        Log.d("BeachDetailActivity", "User ID: " + userId);

        welcomeText = findViewById(R.id.welcomeUserMessage);

        // Null check
        if (userId != null) {
            displayUserName();
        } else {
            welcomeText.setText("User ID is not available.");
        }
    }

    public void displayUserName() {
        // Fetch user data based on userId
        reference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String userName = dataSnapshot.child("userName").getValue(String.class);
                    if (userName != null) {
                        welcomeText.setText("Welcome, " + userName + "!");
                    } else {
                        welcomeText.setText("Welcome, User!");
                    }
                } else {
                    Log.e("Firebase", "User does not exist.");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Firebase", "Error fetching user data: " + databaseError.getMessage());
            }
        });
    }
}

