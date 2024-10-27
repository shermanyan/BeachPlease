package com.example.beachplease;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.EditText;


import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.regex.Pattern;
import java.util.regex.Matcher;


public class SignUpActivity extends Activity{

    private String firstName;
    private String lastName;
    private String userName;
    private String email;
    private String password;
    private String confirmPassword;
    private static final String emailRegex = "^(([^<>()\\[\\]\\\\.,;:\\s@\"]+(\\.[^<>()\\[\\]\\\\.,;:\\s@\"]+)*)|(\".+\"))@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.signupPage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();

        String message = intent.getStringExtra("com.example.samplebeachplease.MESSAGE");
    }

    public static boolean isValidEmail(String email){
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);

        return matcher.matches();
    }

    public boolean validateUser() {

        boolean canRegister = true;

        // Get info from user input and check for validity
        TextInputLayout firstLayout = findViewById(R.id.firstname);
        TextInputEditText firstLayoutText = (TextInputEditText) firstLayout.getEditText();
        firstName = firstLayoutText.getText().toString();
        if (firstName.isEmpty()) {
            firstLayout.setError("First name is required");
            canRegister = false;
        } else if (!firstName.matches("^[a-zA-Z]+$")){
            firstLayout.setError("must contain only letters");
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
        } else if (!lastName.matches("^[a-zA-Z]+$")){
            lastLayout.setError("must contain only letters");
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
        if (password.isEmpty()) {
            passwordLayout.setError("Password is required");
            canRegister = false;
        } else {
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
    public void setupUserInfo() {
        user.setFirstName(firstName);

        user.setLastName(lastName);

        // Set username to User's first name is user did not input a username
        if (userName.isEmpty()) {
            user.setUserName(firstName);
        }else {
            user.setUserName(userName);
        }
        user.setEmail(email);

        user.setPassword(password);
    }

    public void registerUser(View view) {

        boolean isUserInfoValid = validateUser();

        EditText message = findViewById(R.id.success);

        if (!isUserInfoValid) {
            message.setText("Try Again");
        } else {
            message.setText("Sign Up was successful!");
            setupUserInfo();
        }
    }
}