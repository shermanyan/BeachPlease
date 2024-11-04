package com.example.beachplease;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private String email;
    private String password;
    private DatabaseReference reference;
    private FirebaseDatabase root;
    private FirebaseAuth mAuth;
    private PasswordHash passHash;

    private String firstName;
    private String lastName;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.loginPage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();


    }

    public void createAccount(View view){
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
    }

    public void login(View view) {
        TextInputLayout emailLayout = findViewById(R.id.loginemail);
        TextInputEditText emailLayoutText = (TextInputEditText) emailLayout.getEditText();
        email = emailLayoutText.getText().toString().trim();

        TextInputLayout passwordLayout = findViewById(R.id.loginpassword);
        TextInputEditText passwordLayoutText = (TextInputEditText) passwordLayout.getEditText();
        password = passwordLayoutText.getText().toString().trim();

        root = FirebaseDatabase.getInstance("https://beachplease-439517-default-rtdb.firebaseio.com/");
        reference = root.getReference("users");

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email or password cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();

                        if (user != null) {
                            String userId = user.getUid();
                            Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();

                            // Retrieve user's info from the database
                            reference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        firstName = dataSnapshot.child("firstName").getValue(String.class);
                                        lastName = dataSnapshot.child("lastName").getValue(String.class);
                                        username = dataSnapshot.child("userName").getValue(String.class);

                                        UserSession.login(new User(firstName, lastName, username, email, userId));

                                        Intent intent = new Intent(LoginActivity.this, MapActivity.class);
                                        startActivity(intent);
                                    } else {
                                        Toast.makeText(LoginActivity.this, "User data not found.", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Log.e("DatabaseError", "Error getting user data: " + databaseError.getMessage());
                                }
                            });
                        } else {
                            Toast.makeText(LoginActivity.this, "Login failed.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Login failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}


