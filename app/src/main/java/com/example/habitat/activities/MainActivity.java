package com.example.habitat.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

import com.example.habitat.R;

public class MainActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Using Handler to delay the transition
        new Handler().postDelayed(() -> {
            // Check if user is already logged in using SharedPreferences
            SharedPreferences prefs = getSharedPreferences("HabitatPrefs", MODE_PRIVATE);
            boolean isLoggedIn = prefs.getBoolean("is_logged_in", false);

            // Using Intent for explicit navigation
            Intent intent;
            if (isLoggedIn) {
                // If logged in, go directly to home
                intent = new Intent(MainActivity.this, HomeActivity.class);
            } else {
                // If not logged in, go to login screen
                intent = new Intent(MainActivity.this, LoginActivity.class);
            }

            startActivity(intent);
            finish(); // Close this activity
        }, SPLASH_DURATION);
    }
}