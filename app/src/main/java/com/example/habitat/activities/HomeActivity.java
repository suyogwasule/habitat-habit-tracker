package com.example.habitat.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.habitat.R;
import com.example.habitat.fragments.DiaryFragment;
import com.example.habitat.fragments.TasksFragment;
import com.example.habitat.fragments.TimerFragment;
import com.example.habitat.fragments.TodayFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    private TextView dateText;
    private TextView sectionTitle;

    // Navigation constants
    public static final int NAV_TODAY = 1;
    public static final int NAV_TASKS = 2;
    public static final int NAV_TIMER = 3;
    public static final int NAV_DIARY = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize views
        dateText = findViewById(R.id.date_text);
        sectionTitle = findViewById(R.id.section_title);

        // Set current date
        updateDateDisplay();

        // Set up bottom navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            // Convert menu item ID to our constants
            int navConstant = getNavConstantFromItemId(item.getItemId());

            // Handle navigation item selection using our constants
            switch (navConstant) {
                case NAV_TODAY:
                    selectedFragment = new TodayFragment();
                    sectionTitle.setText("Today");
                    break;
                case NAV_TASKS:
                    selectedFragment = new TasksFragment();
                    sectionTitle.setText("Tasks");
                    break;
                case NAV_TIMER:
                    selectedFragment = new TimerFragment();
                    sectionTitle.setText("Timer");
                    break;
                case NAV_DIARY:
                    selectedFragment = new DiaryFragment();
                    sectionTitle.setText("Diary");
                    break;
            }

            // Load the selected fragment
            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content_frame, selectedFragment)
                        .commit();
                return true;
            }

            return false;
        });

        // Set up more options button
        findViewById(R.id.more_options_button).setOnClickListener(v -> showMoreOptionsDialog());

        // Set default fragment
        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_today);
        }
    }

    /**
     * Converts menu item resource ID to our navigation constants
     */
    private int getNavConstantFromItemId(int itemId) {
        if (itemId == R.id.nav_today) {
            return NAV_TODAY;
        } else if (itemId == R.id.nav_tasks) {
            return NAV_TASKS;
        } else if (itemId == R.id.nav_timer) {
            return NAV_TIMER;
        } else if (itemId == R.id.nav_diary) {
            return NAV_DIARY;
        }
        return NAV_TODAY; // Default
    }

    /**
     * Update the date display with current date
     * Demonstrates date formatting
     */
    private void updateDateDisplay() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM d", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());
        dateText.setText(currentDate);
    }

    /**
     * Show more options dialog
     * Demonstrates Dialog implementation (part of syllabus)
     */
    private void showMoreOptionsDialog() {
        String[] options = {"Settings", "Export Data", "About", "Logout"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);
        builder.setTitle("More Options");
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0: // Settings
                    // TODO: Implement settings
                    break;
                case 1: // Export Data
                    exportData();
                    break;
                case 2: // About
                    showAboutDialog();
                    break;
                case 3: // Logout
                    confirmLogout();
                    break;
            }
        });
        builder.show();
    }

    /**
     * Export user data to external storage
     * Demonstrates external storage usage (part of syllabus)
     */
    private void exportData() {
        // Placeholder for data export functionality
        // This would implement external storage access
        Toast.makeText(this, "Data export functionality would go here", Toast.LENGTH_SHORT).show();
    }

    /**
     * Show About dialog
     * Demonstrates custom dialog implementation
     */
    private void showAboutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);
        builder.setTitle("About Habitat");
        builder.setMessage("Habitat v1.0\n\nA habit tracking and productivity app.");
        builder.setPositiveButton("OK", null);
        builder.show();
    }

    /**
     * Confirm logout with dialog
     * Demonstrates confirmation dialog
     */
    private void confirmLogout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);
        builder.setTitle("Logout");
        builder.setMessage("Are you sure you want to logout?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            // Clear login status
            SharedPreferences prefs = getSharedPreferences("HabitatPrefs", MODE_PRIVATE);
            prefs.edit().putBoolean("is_logged_in", false).apply();

            // Navigate to login screen
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
        builder.setNegativeButton("No", null);
        builder.show();
    }
}