package com.example.habitat.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.example.habitat.R;
import java.util.Locale;
import android.widget.Toast;
/**
 * Fragment for the Timer tab - implements a pomodoro timer
 * Demonstrates CountDownTimer and SharedPreferences
 */
public class TimerFragment extends Fragment {

    // Timer durations in milliseconds (default values)
    private static final long DEFAULT_FOCUS_TIME = 25 * 60 * 1000; // 25 minutes
    private static final long DEFAULT_BREAK_TIME = 5 * 60 * 1000; // 5 minutes

    private TextView timerText;
    private TextView timerLabel;
    private ProgressBar timerProgress;
    private Button startPauseButton;
    private Button resetButton;
    private Button settingsButton;

    private CountDownTimer countDownTimer;
    private boolean isTimerRunning = false;
    private boolean isFocusTime = true;
    private long focusTimeInMillis = DEFAULT_FOCUS_TIME;
    private long breakTimeInMillis = DEFAULT_BREAK_TIME;
    private long timeLeftInMillis = DEFAULT_FOCUS_TIME;
    private long totalTime = DEFAULT_FOCUS_TIME;

    // SharedPreferences for saving timer state
    private SharedPreferences prefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_timer, container, false);

        // Initialize views
        timerText = view.findViewById(R.id.timer_text);
        timerLabel = view.findViewById(R.id.timer_label);
        timerProgress = view.findViewById(R.id.timer_progress);
        startPauseButton = view.findViewById(R.id.timer_start_pause_button);
        resetButton = view.findViewById(R.id.timer_reset_button);
        settingsButton = view.findViewById(R.id.timer_settings_button);

        // Initialize SharedPreferences
        prefs = getContext().getSharedPreferences("TimerPrefs", Context.MODE_PRIVATE);

        // Load timer settings
        loadTimerSettings();

        // Restore timer state
        restoreTimerState();

        // Set up button click listeners
        startPauseButton.setOnClickListener(v -> {
            if (isTimerRunning) {
                pauseTimer();
            } else {
                startTimer();
            }
        });

        resetButton.setOnClickListener(v -> resetTimer());

        settingsButton.setOnClickListener(v -> showTimerSettingsDialog());

        // Update UI
        updateCountDownText();
        updateProgressBar();

        return view;
    }

    /**
     * Load timer duration settings from SharedPreferences
     */
    private void loadTimerSettings() {
        focusTimeInMillis = prefs.getLong("focus_time", DEFAULT_FOCUS_TIME);
        breakTimeInMillis = prefs.getLong("break_time", DEFAULT_BREAK_TIME);

        // Initialize timeLeftInMillis and totalTime based on current mode
        if (isFocusTime) {
            if (timeLeftInMillis == DEFAULT_FOCUS_TIME) { // Only update if not already running
                timeLeftInMillis = focusTimeInMillis;
            }
            totalTime = focusTimeInMillis;
        } else {
            if (timeLeftInMillis == DEFAULT_BREAK_TIME) { // Only update if not already running
                timeLeftInMillis = breakTimeInMillis;
            }
            totalTime = breakTimeInMillis;
        }
    }

    /**
     * Show dialog to configure timer settings
     */
    private void showTimerSettingsDialog() {
        // If timer is running, pause it
        boolean wasRunning = isTimerRunning;
        if (isTimerRunning) {
            pauseTimer();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Timer Settings");

        // Inflate custom layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_timer_settings, null);
        builder.setView(dialogView);

        // Get number pickers
        NumberPicker focusMinutesPicker = dialogView.findViewById(R.id.focus_minutes_picker);
        NumberPicker breakMinutesPicker = dialogView.findViewById(R.id.break_minutes_picker);

        // Set up focus time picker
        focusMinutesPicker.setMinValue(1);
        focusMinutesPicker.setMaxValue(60);
        focusMinutesPicker.setValue((int) (focusTimeInMillis / 60000));

        // Set up break time picker
        breakMinutesPicker.setMinValue(1);
        breakMinutesPicker.setMaxValue(30);
        breakMinutesPicker.setValue((int) (breakTimeInMillis / 60000));

        // Set up buttons
        builder.setPositiveButton("Save", (dialog, which) -> {
            // Get values from pickers
            int focusMinutes = focusMinutesPicker.getValue();
            int breakMinutes = breakMinutesPicker.getValue();

            // Convert to milliseconds
            focusTimeInMillis = focusMinutes * 60 * 1000;
            breakTimeInMillis = breakMinutes * 60 * 1000;

            // Save settings to SharedPreferences
            SharedPreferences.Editor editor = prefs.edit();
            editor.putLong("focus_time", focusTimeInMillis);
            editor.putLong("break_time", breakTimeInMillis);
            editor.apply();

            // Reset timer with new settings
            resetTimer();

            // Resume timer if it was running
            if (wasRunning) {
                startTimer();
            }

            // Show confirmation
            Toast.makeText(getContext(), "Timer settings updated", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            // Resume timer if it was running
            if (wasRunning) {
                startTimer();
            }
        });

        // Create and show dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Start the countdown timer
     */
    private void startTimer() {
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateCountDownText();
                updateProgressBar();
            }

            @Override
            public void onFinish() {
                isTimerRunning = false;
                startPauseButton.setText("Start");

                // Switch between focus and break time
                isFocusTime = !isFocusTime;
                timeLeftInMillis = isFocusTime ? focusTimeInMillis : breakTimeInMillis;
                totalTime = timeLeftInMillis;

                // Update label
                timerLabel.setText(isFocusTime ? "Focus Time" : "Break Time");

                // Update UI
                updateCountDownText();
                updateProgressBar();

                // Play sound or show notification
                playTimerFinishedSound();

                // Save timer state
                saveTimerState();
            }
        }.start();

        isTimerRunning = true;
        startPauseButton.setText("Pause");

        // Save timer state
        saveTimerState();
    }

    /**
     * Play sound when timer finishes
     */
    private void playTimerFinishedSound() {
        // Play a sound to indicate timer completion
        // This could be implemented using MediaPlayer or ToneGenerator

        // For now, just show a toast
        Toast.makeText(getContext(),
                isFocusTime ? "Focus time started!" : "Break time started!",
                Toast.LENGTH_SHORT).show();
    }

    /**
     * Pause the countdown timer
     */
    private void pauseTimer() {
        countDownTimer.cancel();
        isTimerRunning = false;
        startPauseButton.setText("Resume");

        // Save timer state
        saveTimerState();
    }

    /**
     * Reset the timer to initial state
     */
    private void resetTimer() {
        // Cancel current timer if running
        if (isTimerRunning) {
            countDownTimer.cancel();
            isTimerRunning = false;
        }

        // Reset to focus time
        isFocusTime = true;
        timeLeftInMillis = focusTimeInMillis;
        totalTime = focusTimeInMillis;

        // Update UI
        timerLabel.setText("Focus Time");
        startPauseButton.setText("Start");
        updateCountDownText();
        updateProgressBar();

        // Save timer state
        saveTimerState();
    }

    /**
     * Update the timer text display
     */
    private void updateCountDownText() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;

        String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        timerText.setText(timeFormatted);
    }

    /**
     * Update the progress bar
     */
    private void updateProgressBar() {
        int progress = (int) (100 * timeLeftInMillis / totalTime);
        timerProgress.setProgress(progress);
    }

    /**
     * Save timer state to SharedPreferences
     * Demonstrates SharedPreferences for data persistence
     */
    private void saveTimerState() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("timer_running", isTimerRunning);
        editor.putBoolean("is_focus_time", isFocusTime);
        editor.putLong("time_left", timeLeftInMillis);
        editor.putLong("total_time", totalTime);
        editor.apply();
    }

    /**
     * Restore timer state from SharedPreferences
     */
    private void restoreTimerState() {
        isTimerRunning = prefs.getBoolean("timer_running", false);
        isFocusTime = prefs.getBoolean("is_focus_time", true);
        timeLeftInMillis = prefs.getLong("time_left", focusTimeInMillis);
        totalTime = prefs.getLong("total_time", focusTimeInMillis);

        timerLabel.setText(isFocusTime ? "Focus Time" : "Break Time");

        if (isTimerRunning) {
            startTimer();
            startPauseButton.setText("Pause");
        } else {
            startPauseButton.setText(timeLeftInMillis < totalTime ? "Resume" : "Start");
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        // Save timer state when fragment is paused
        saveTimerState();

        // Cancel the timer if running
        if (isTimerRunning && countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}