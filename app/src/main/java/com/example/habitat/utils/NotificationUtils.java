package com.example.habitat.utils;


import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.example.habitat.R;
import com.example.habitat.activities.HomeActivity;
import com.example.habitat.receivers.HabitNotificationReceiver;
import java.util.Calendar;

/**
 * Utility class for notification operations
 */
public class NotificationUtils {

    // Notification channel IDs
    public static final String CHANNEL_HABITS = "habit_channel";
    public static final String CHANNEL_TIMER = "timer_channel";

    /**
     * Create all notification channels (should be called at app startup)
     */
    public static void createNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);

            // Create habit notifications channel
            NotificationChannel habitChannel = new NotificationChannel(
                    CHANNEL_HABITS,
                    "Habit Reminders",
                    NotificationManager.IMPORTANCE_DEFAULT);
            habitChannel.setDescription("Notifications for habit reminders");

            // Create timer notifications channel
            NotificationChannel timerChannel = new NotificationChannel(
                    CHANNEL_TIMER,
                    "Timer Alerts",
                    NotificationManager.IMPORTANCE_HIGH);
            timerChannel.setDescription("Notifications for timer completion");

            // Register channels
            notificationManager.createNotificationChannels(
                    java.util.Arrays.asList(habitChannel, timerChannel));
        }
    }

    /**
     * Show a simple notification
     */
    public static void showNotification(Context context, String channelId, int notificationId,
                                        String title, String message) {
        // Create intent to open app when notification is tapped
        Intent intent = new Intent(context, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // Show notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(notificationId, builder.build());
    }

    /**
     * Schedule habit notification with AlarmManager
     */
    public static void scheduleHabitNotification(Context context, long habitId, String habitName,
                                                 int hour, int minute) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Create intent for notification
        Intent intent = new Intent(context, HabitNotificationReceiver.class);
        intent.putExtra("HABIT_ID", habitId);
        intent.putExtra("HABIT_NAME", habitName);

        // Create unique pending intent
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                (int) habitId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Set alarm time
        Calendar calendar = DateTimeUtils.getNextOccurrence(hour, minute);

        // Schedule repeating alarm
        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                pendingIntent);
    }

    /**
     * Cancel scheduled habit notification
     */
    public static void cancelHabitNotification(Context context, long habitId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, HabitNotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                (int) habitId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Cancel the alarm
        alarmManager.cancel(pendingIntent);
    }
}