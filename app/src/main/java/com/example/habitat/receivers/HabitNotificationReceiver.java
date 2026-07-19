package com.example.habitat.receivers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.habitat.R;
import com.example.habitat.activities.HomeActivity;

/**
 * Broadcast receiver for habit notifications
 */
public class HabitNotificationReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "habit_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Get habit info from intent
        long habitId = intent.getLongExtra("HABIT_ID", -1);
        String habitName = intent.getStringExtra("HABIT_NAME");

        if (habitId == -1 || habitName == null) {
            return;
        }

        // Create notification channel for Android 8.0+
        createNotificationChannel(context);

        // Create intent for when notification is tapped
        Intent tapIntent = new Intent(context, HomeActivity.class);
        tapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                (int) habitId,
                tapIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Habit Reminder")
                .setContentText("Time to complete your habit: " + habitName)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // Show notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        // Check for notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= 33) {
            if (notificationManager.areNotificationsEnabled()) {
                notificationManager.notify((int) habitId, builder.build());
            }
        } else {
            notificationManager.notify((int) habitId, builder.build());
        }

        // Reschedule for tomorrow (handled by AlarmManager's setRepeating in TodayFragment)
    }

    /**
     * Create notification channel for Android 8.0+
     */
    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Habit Reminders";
            String description = "Notifications for habit reminders";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            // Register the channel with the system
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}