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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.habitat.R;
import com.example.habitat.adapters.TaskAdapter;
import com.example.habitat.data.HabitatDatabaseHelper;
import com.example.habitat.data.models.Task;
import com.example.habitat.receivers.TaskResetReceiver;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Calendar;
import java.util.List;

/**
 * Fragment for the Tasks tab - manages to-do list
 */
public class TasksFragment extends Fragment implements TaskAdapter.TaskCheckListener {

    private RecyclerView recyclerView;
    private View emptyView;
    private TaskAdapter adapter;
    private HabitatDatabaseHelper dbHelper;
    private List<Task> taskList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tasks, container, false);

        // Initialize views
        recyclerView = view.findViewById(R.id.tasks_recycler_view);
        emptyView = view.findViewById(R.id.empty_tasks_view);
        FloatingActionButton addButton = view.findViewById(R.id.add_task_button);

        // Initialize database helper
        dbHelper = new HabitatDatabaseHelper(getContext());

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Load tasks from database
        loadTasks();

        // Set up add button
        addButton.setOnClickListener(v -> showAddTaskDialog());

        // Schedule daily task reset at midnight
        scheduleDailyTaskReset();

        return view;
    }

    /**
     * Schedule daily task reset at midnight
     */
    private void scheduleDailyTaskReset() {
        AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);

        // Create intent for the alarm
        Intent intent = new Intent(getContext(), TaskResetReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                getContext(),
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Set alarm for midnight
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.add(Calendar.DAY_OF_YEAR, 1); // Set for next day

        // Schedule repeating alarm
        alarmManager.setRepeating(
                AlarmManager.RTC,
                calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                pendingIntent);
    }

    /**
     * Load tasks from database and update UI
     */
    private void loadTasks() {
        taskList = dbHelper.getAllTasks();

        // Show empty view if no tasks
        if (taskList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);

            // Set up adapter
            adapter = new TaskAdapter(getContext(), taskList, this);
            recyclerView.setAdapter(adapter);
        }
    }

    /**
     * Show dialog to add a new task
     */
    private void showAddTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Add New Task");

        // Inflate custom layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_task, null);
        builder.setView(dialogView);

        // Get dialog views
        final EditText taskNameInput = dialogView.findViewById(R.id.task_name_input);
        final EditText taskDescriptionInput = dialogView.findViewById(R.id.task_description_input);

        // Set up buttons
        builder.setPositiveButton("Add", (dialog, which) -> {
            // Validate input
            String taskName = taskNameInput.getText().toString().trim();
            String taskDescription = taskDescriptionInput.getText().toString().trim();

            if (taskName.isEmpty()) {
                Toast.makeText(getContext(), "Please enter a task name", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save task to database
            long taskId = dbHelper.createTask(taskName, taskDescription);

            if (taskId != -1) {
                // Reload tasks
                loadTasks();

                // Show success message
                Toast.makeText(getContext(), "Task added successfully", Toast.LENGTH_SHORT).show();
            } else {
                // Show error message
                Toast.makeText(getContext(), "Failed to add task", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);

        // Create and show dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Handle task check/uncheck events
     */
    @Override
    public void onTaskChecked(int taskId, boolean isCompleted) {
        // Update task status in database
        dbHelper.updateTaskStatus(taskId, isCompleted);

        // Reload tasks to update UI
        loadTasks();
    }
}