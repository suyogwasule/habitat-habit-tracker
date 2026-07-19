package com.example.habitat.activities;

import com.example.habitat.R;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private SharedPreferences prefs;
    private EditText nameInput;
    private EditText passwordInput;
    private TextView welcomeText;
    private TextView subtitleText;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        // Initialize UI components
        nameInput = findViewById(R.id.name_input);
        passwordInput = findViewById(R.id.password_input);
        welcomeText = findViewById(R.id.welcome_text);
        subtitleText = findViewById(R.id.subtitle_text);
        saveButton = findViewById(R.id.save_button);

        // Initialize SharedPreferences for data storage (part of syllabus)
        prefs = getSharedPreferences("HabitatPrefs", MODE_PRIVATE);

        // Check if user exists
        String savedName = prefs.getString("user_name", null);

        if (savedName != null) {
            // Returning user experience
            welcomeText.setText("Welcome Back!");
            subtitleText.setText("Great to see you again, " + savedName);
            nameInput.setVisibility(View.GONE);
            passwordInput.setHint("Enter your password");
            saveButton.setText("Login");
        }

        // Set up button click listener
        saveButton.setOnClickListener(v -> {
            if (savedName == null) {
                // First-time user
                handleFirstTimeLogin();
            } else {
                // Returning user
                handleReturningUserLogin(savedName);
            }
        });
    }

    /**
     * Handle login for first-time users
     * Demonstrates SharedPreferences for data storage
     */
    private void handleFirstTimeLogin() {
        String name = nameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(password)) {
            // Show a Toast notification (part of syllabus)
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save user data in SharedPreferences
        prefs.edit()
                .putString("user_name", name)
                .putString("user_password", password)
                .putBoolean("is_logged_in", true)
                .apply();

        // Navigate to home screen
        startHomeActivity();
    }

    /**
     * Handle login for returning users
     * Demonstrates password validation
     */
    private void handleReturningUserLogin(String savedName) {
        String password = passwordInput.getText().toString().trim();
        String savedPassword = prefs.getString("user_password", "");

        if (password.equals(savedPassword)) {
            // Update login status
            prefs.edit().putBoolean("is_logged_in", true).apply();

            // Navigate to home screen
            startHomeActivity();
        } else {
            // Show error notification
            Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Start the home activity
     * Demonstrates Intent for navigation
     */
    private void startHomeActivity() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
}