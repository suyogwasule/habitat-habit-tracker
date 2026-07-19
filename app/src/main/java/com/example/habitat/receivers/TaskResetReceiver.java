package com.example.habitat.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import com.example.habitat.data.HabitatDatabaseHelper;

/**
 * Broadcast receiver to reset tasks at midnight
 */
public class TaskResetReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Reset all tasks (delete completed ones)
        HabitatDatabaseHelper dbHelper = new HabitatDatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Delete all completed tasks
        int deletedRows = db.delete("tasks", "completed = ?", new String[] { "1" });

        // Show notification if tasks were deleted
        if (deletedRows > 0) {
            Toast.makeText(context,
                    "Daily reset: " + deletedRows + " completed tasks removed",
                    Toast.LENGTH_SHORT).show();
        }
    }
}