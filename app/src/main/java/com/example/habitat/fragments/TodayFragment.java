package com.example.habitat.fragments;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.habitat.R;
import com.example.habitat.adapters.HabitAdapter;
import com.example.habitat.adapters.HabitCalendarAdapter;
import com.example.habitat.data.HabitatDatabaseHelper;
import com.example.habitat.data.models.Habit;
import com.example.habitat.data.models.HabitRecord;
import com.example.habitat.receivers.HabitNotificationReceiver;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Fragment for the Today tab - manages habits
 */
public class TodayFragment extends Fragment implements HabitAdapter.HabitActionListener {

    private static final String TAG = "TodayFragment";

    private RecyclerView recyclerView;
    private View emptyView;
    private HabitAdapter adapter;
    private HabitatDatabaseHelper dbHelper;
    private List<Habit> habitList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_today, container, false);

        // Initialize views
        recyclerView = view.findViewById(R.id.habits_recycler_view);
        emptyView = view.findViewById(R.id.empty_habits_view);
        FloatingActionButton addButton = view.findViewById(R.id.add_habit_button);

        // Initialize database helper
        dbHelper = new HabitatDatabaseHelper(getContext());

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Load habits from database
        loadHabits();

        // Set up add button
        addButton.setOnClickListener(v -> showAddHabitDialog());

        return view;
    }

    /**
     * Load habits from database and update UI
     */
    private void loadHabits() {
        habitList = dbHelper.getAllHabits();

        // Show empty view if no habits
        if (habitList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);

            // Set up adapter
            adapter = new HabitAdapter(getContext(), habitList, this);
            recyclerView.setAdapter(adapter);
        }
    }

    /**
     * Show dialog to add a new habit
     */
    private void showAddHabitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Add New Habit");

        // Inflate custom layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_habit, null);
        builder.setView(dialogView);

        // Get dialog views
        final EditText habitNameInput = dialogView.findViewById(R.id.habit_name_input);
        final TimePicker timePicker = dialogView.findViewById(R.id.notification_time_picker);
        final NumberPicker durationPicker = dialogView.findViewById(R.id.duration_picker);

        // Set 24h format for time picker
        timePicker.setIs24HourView(true);

        // Set up duration picker
        durationPicker.setMinValue(1);
        durationPicker.setMaxValue(365);
        durationPicker.setValue(21); // Default 21 days

        // Set up buttons
        builder.setPositiveButton("Add", (dialog, which) -> {
            // Validate input
            String habitName = habitNameInput.getText().toString().trim();

            if (habitName.isEmpty()) {
                Toast.makeText(getContext(), "Please enter a habit name", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get time from time picker
            int hour, minute;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                hour = timePicker.getHour();
                minute = timePicker.getMinute();
            } else {
                hour = timePicker.getCurrentHour();
                minute = timePicker.getCurrentMinute();
            }

            // Format time
            String notificationTime = String.format("%02d:%02d", hour, minute);

            // Get duration
            int durationDays = durationPicker.getValue();

            // Save habit to database
            long habitId = dbHelper.addHabit(habitName, notificationTime, durationDays);

            if (habitId != -1) {
                // Schedule notification
                scheduleHabitNotification(habitId, habitName, hour, minute);

                // Reload habits
                loadHabits();

                // Show success message
                Toast.makeText(getContext(), "Habit added successfully", Toast.LENGTH_SHORT).show();
            } else {
                // Show error message
                Toast.makeText(getContext(), "Failed to add habit", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);

        // Create and show dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Schedule notification for habit reminder
     * Demonstrates AlarmManager for scheduling notifications
     */
    private void scheduleHabitNotification(long habitId, String habitName, int hour, int minute) {
        try {
            AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);

            // Create intent for notification
            Intent intent = new Intent(getContext(), HabitNotificationReceiver.class);
            intent.putExtra("HABIT_ID", habitId);
            intent.putExtra("HABIT_NAME", habitName);

            // Create unique pending intent for this habit
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    getContext(),
                    (int) habitId, // Use habitId as request code for uniqueness
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            // Calculate time for alarm
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);

            // If time has already passed today, set for tomorrow
            if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }

            // Set exact alarm for reliability
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent);
            } else {
                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent);
            }

            // Also set repeating alarm as backup (might not be exact)
            alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error scheduling notification: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Handle habit completion
     */
    @Override
    public void onHabitCompleted(int habitId, boolean isCompleted) {
        try {
            Habit habit = dbHelper.getHabit(habitId);

            if (isCompleted) {
                // Increment streak
                int newStreak = habit.getStreak() + 1;
                dbHelper.updateHabitStreak(habitId, newStreak);

                // Record habit completion for calendar view
                dbHelper.recordHabitCompletion(habitId);

                Toast.makeText(getContext(),
                        "Streak: " + newStreak + " days",
                        Toast.LENGTH_SHORT).show();
            } else {
                // Reset streak
                dbHelper.updateHabitStreak(habitId, 0);

                // Remove habit completion record
                dbHelper.removeHabitCompletion(habitId);

                Toast.makeText(getContext(),
                        "Streak reset",
                        Toast.LENGTH_SHORT).show();
            }

            // Reload habits
            loadHabits();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error updating habit: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Handle habit item click to show calendar
     */
    @Override
    public void onHabitItemClick(Habit habit) {
        try {
            showHabitCalendarDialog(habit);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error showing calendar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Show calendar dialog with habit completion history
     */
    private void showHabitCalendarDialog(Habit habit) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(habit.getTitle() + " - Completion History");

            // Inflate custom layout
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_habit_calendar, null);
            builder.setView(dialogView);

            // Get views
            TextView streakText = dialogView.findViewById(R.id.habit_streak_text);
            GridView calendarGrid = dialogView.findViewById(R.id.calendar_grid);
            TextView monthYearText = dialogView.findViewById(R.id.month_year_text);
            ImageButton prevButton = dialogView.findViewById(R.id.prev_month_button);
            ImageButton nextButton = dialogView.findViewById(R.id.next_month_button);

            // Set streak text
            streakText.setText("Current streak: " + habit.getStreak() + " days");

            // Get habit completion records
            List<HabitRecord> records = dbHelper.getHabitCompletionRecords(habit.getId());

            // Set up calendar
            final Calendar calendar = Calendar.getInstance();
            final HabitCalendarAdapter adapter = new HabitCalendarAdapter(getContext(), records);
            calendarGrid.setAdapter(adapter);

            // Update month/year display
            updateMonthYearText(monthYearText, calendar);

            // Set up navigation buttons
            prevButton.setOnClickListener(v -> {
                calendar.add(Calendar.MONTH, -1);
                adapter.setMonth(calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR));
                updateMonthYearText(monthYearText, calendar);
            });

            nextButton.setOnClickListener(v -> {
                calendar.add(Calendar.MONTH, 1);
                adapter.setMonth(calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR));
                updateMonthYearText(monthYearText, calendar);
            });

            // Set up buttons
            builder.setPositiveButton("Close", null);

            // Create and show dialog
            AlertDialog dialog = builder.create();
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error showing calendar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Update month and year text
     */
    private void updateMonthYearText(TextView textView, Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        textView.setText(sdf.format(calendar.getTime()));
    }

    /**
     * Handle habit deletion
     */
    @Override
    public void onHabitDeleteClick(int habitId) {
        try {
            // Show confirmation dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Delete Habit");
            builder.setMessage("Are you sure you want to delete this habit? This will remove all progress and history.");
            builder.setPositiveButton("Delete", (dialog, which) -> {
                try {
                    // Cancel any scheduled notifications
                    cancelHabitNotification(habitId);

                    // Delete habit from database
                    dbHelper.deleteHabit(habitId);

                    // Reload habits
                    loadHabits();

                    // Show success message
                    Toast.makeText(getContext(), "Habit deleted", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Error deleting habit: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton("Cancel", null);

            // Create and show dialog
            AlertDialog dialog = builder.create();
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error showing delete dialog: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Cancel scheduled notification for a habit
     */
    private void cancelHabitNotification(int habitId) {
        try {
            AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);

            // Create intent similar to the one used for scheduling
            Intent intent = new Intent(getContext(), HabitNotificationReceiver.class);

            // Create pending intent with the same request code
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    getContext(),
                    habitId, // Same request code used when scheduling
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            // Cancel the alarm
            alarmManager.cancel(pendingIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}