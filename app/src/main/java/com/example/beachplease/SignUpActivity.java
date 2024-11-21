package com.example.beachplease;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.widget.EditText;


import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.UUID;



public class SignUpActivity extends Activity{

    private String firstName = "";
    private String lastName = "";
    private String userName = "";
    private String email = "";
    private String password = "";
    private String confirmPassword = "";
    private static final String emailRegex = "^(([^<>()\\[\\]\\\\.,;:\\s@\"]+(\\.[^<>()\\[\\]\\\\.,;:\\s@\"]+)*)|(\".+\"))@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";

    private User user;

    // Firebase
    private FirebaseDatabase root;
    private DatabaseReference reference;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.signup);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.signupPage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
    }

    public boolean isValidEmail(String email){
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);

        return matcher.matches();
    }

    public boolean validateUser() {

        boolean canRegister = true;

        // name regex
        String regex = "^[A-Z][a-z]*$";

        // Get info from user input and check for validity
        TextInputLayout firstLayout = findViewById(R.id.firstname);
        TextInputEditText firstLayoutText = (TextInputEditText) firstLayout.getEditText();
        firstName = firstLayoutText.getText().toString();
        if (firstName.isEmpty()) {
            firstLayout.setError("First name is required");
            canRegister = false;
        } else if (!firstName.matches(regex)){
            firstLayout.setError("Name must start with an uppercase letter and contain only lowercase letters.");
            canRegister = false;
        } else {
            firstLayout.setError(null);
            firstLayout.setHelperText(null);
        }

        TextInputLayout lastLayout = findViewById(R.id.lastname);
        TextInputEditText lastLayoutText = (TextInputEditText) lastLayout.getEditText();
        lastName = lastLayoutText.getText().toString();
        if (lastName.isEmpty()) {
            lastLayout.setError("Last name is required");
            canRegister = false;
        } else if (!lastName.matches(regex)){
            lastLayout.setError("Name must start with an uppercase letter and contain only lowercase letters.");
            canRegister =false;
        } else {
            lastLayout.setError(null);
            lastLayout.setHelperText(null);
        }


        TextInputLayout userNameLayout = findViewById(R.id.preferredusername);
        TextInputEditText userLayoutText = (TextInputEditText) userNameLayout.getEditText();
        userName = userLayoutText.getText().toString();

        TextInputLayout emailLayout = findViewById(R.id.email);
        TextInputEditText emailLayoutText = (TextInputEditText) emailLayout.getEditText();
        email = emailLayoutText.getText().toString();
        if (email.isEmpty()){
            emailLayout.setError("Email is required");
            canRegister = false;
        }else if ( !isValidEmail(email)){
            emailLayout.setError("Invalid email format");
            canRegister = false;
        }else {
            emailLayout.setError(null);
            emailLayout.setHelperText(null);
        }

        TextInputLayout passwordLayout = findViewById(R.id.password);
        TextInputEditText passwordLayoutText = (TextInputEditText) passwordLayout.getEditText();
        password = passwordLayoutText.getText().toString();
        int minPassLength = 6;
        if (password.isEmpty()) {
            passwordLayout.setError("Password is required");
            canRegister = false;
        }else if(password.length() < minPassLength){
            passwordLayout.setError("Password is too short");
            Toast.makeText(this, "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show();
            canRegister = false;
        }else {
            passwordLayout.setError(null);
            passwordLayout.setHelperText(null);
        }

        TextInputLayout confirmPasswordLayout = findViewById(R.id.confirmpassword);
        TextInputEditText confirmPasswordLayoutText = (TextInputEditText) confirmPasswordLayout.getEditText();
        confirmPassword = confirmPasswordLayoutText.getText().toString();
        if (confirmPassword.isEmpty()) {
            confirmPasswordLayout.setError("Password is required");
            canRegister = false;
        } else if (!confirmPassword.equals(password)){
            confirmPasswordLayout.setError("Passwords do not match");
            canRegister = false;
        } else {
            confirmPasswordLayout.setError(null);
            confirmPasswordLayout.setHelperText(null);
        }

        return canRegister;
    }

    // Method to setup user using User Class
    public void setupUserInfo(String userId) {

        user = new User(firstName, lastName, userName, email, userId);

        user.setFirstName(firstName);
        user.setLastName(lastName);

        // Set username to User's first name is user did not input a username
        if (userName.isEmpty()) {
            user.setUserName(firstName);
        }else {
            user.setUserName(userName);
        }
        user.setEmail(email);

        storeUserInfoInDatabase(user);

    }

    public void registerUser(View view) {

        if (validateUser()){
            mAuth.createUserWithEmailAndPassword(email, password).
                    addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            String userId = mAuth.getCurrentUser().getUid();
                            setupUserInfo(userId);
                            UserSession.login(new User(firstName, lastName, userName, email, userId));
                            Toast.makeText(SignUpActivity.this, "Sign up successful", Toast.LENGTH_SHORT).show();
                            openMapActivity(view);
                        }else{
                            Toast.makeText(SignUpActivity.this, "Failed to sign up: email address is already taken", Toast.LENGTH_SHORT).show();
                            Log.e("FirebaseAuth", "registration failed: " + task.getException().getMessage());
                        }
                    });
        }

    }


    public void storeUserInfoInDatabase(User user) {
        root = FirebaseDatabase.getInstance("https://beachplease-439517-default-rtdb.firebaseio.com/");
        reference = root.getReference("users");

        reference.child(user.getId()).setValue(user).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Successfully written to database
                Log.d("Database", "User info saved successfully.");
            } else {
                // Failed to write to database
                Log.e("Database", "Failed to save user info: " + task.getException().getMessage());
            }
        });
    }

    // method to verify user signup and login
    public void openMapActivity(View view){

        Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);
    }


}
