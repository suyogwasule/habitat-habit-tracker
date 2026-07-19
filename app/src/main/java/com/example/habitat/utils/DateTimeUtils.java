package com.example.habitat.utils;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Utility class for date and time operations
 */
public class DateTimeUtils {

    // Date format constants
    private static final String DATE_FORMAT_DISPLAY = "EEEE, MMMM d, yyyy";
    private static final String DATE_FORMAT_DB = "yyyy-MM-dd HH:mm:ss";
    private static final String TIME_FORMAT_DISPLAY = "h:mm a";

    /**
     * Get current date formatted for display
     */
    public static String getCurrentDateFormatted() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_DISPLAY, Locale.getDefault());
        return dateFormat.format(new Date());
    }

    /**
     * Get current time formatted for display
     */
    public static String getCurrentTimeFormatted() {
        SimpleDateFormat timeFormat = new SimpleDateFormat(TIME_FORMAT_DISPLAY, Locale.getDefault());
        return timeFormat.format(new Date());
    }

    /**
     * Format date from database format to display format
     */
    public static String formatDateForDisplay(String dbDateString) {
        try {
            SimpleDateFormat dbFormat = new SimpleDateFormat(DATE_FORMAT_DB, Locale.getDefault());
            SimpleDateFormat displayFormat = new SimpleDateFormat(DATE_FORMAT_DISPLAY, Locale.getDefault());
            Date date = dbFormat.parse(dbDateString);
            return displayFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return dbDateString;
        }
    }

    /**
     * Format time (hours:minutes) to display format
     */
    public static String formatTimeForDisplay(int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);

        SimpleDateFormat timeFormat = new SimpleDateFormat(TIME_FORMAT_DISPLAY, Locale.getDefault());
        return timeFormat.format(calendar.getTime());
    }

    /**
     * Calculate next occurrence of a specific time
     */
    public static Calendar getNextOccurrence(int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // If time has already passed today, set for tomorrow
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        return calendar;
    }

    /**
     * Format milliseconds to timer display format (MM:SS)
     */
    public static String formatMillisToTimerDisplay(long millis) {
        int minutes = (int) (millis / 1000) / 60;
        int seconds = (int) (millis / 1000) % 60;

        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }
}