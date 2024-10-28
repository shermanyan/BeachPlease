package com.example.beachplease;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void signupUser(View view) {

        String choice = "signup";
        Intent intent = new Intent(this, SignUpActivity.class);
        intent.putExtra("com.example.samplebeachplease.MESSAGE", choice);

        startActivity(intent);
    }


    public void loginUser(View view) {

        String choice = "Login";
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("com.example.samplebeachplease.MESSAGE", choice);

        startActivity(intent);
    }

    public void openMap(View view) {
        Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);
    }
}