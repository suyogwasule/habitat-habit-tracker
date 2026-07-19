package com.example.habitat.data.preferences;


import android.content.Context;
import android.content.SharedPreferences;

/**
 * Wrapper class for SharedPreferences to provide easier access to app preferences
 */
public class PreferenceManager {

    // Shared preferences file name
    private static final String PREF_NAME = "HabitatPrefs";

    // Shared preferences keys
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_PASSWORD = "user_password";

    // Timer preferences
    private static final String KEY_TIMER_RUNNING = "timer_running";
    private static final String KEY_IS_FOCUS_TIME = "is_focus_time";
    private static final String KEY_TIME_LEFT = "time_left";
    private static final String KEY_TOTAL_TIME = "total_time";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;

    // Singleton instance
    private static PreferenceManager instance;

    /**
     * Get singleton instance of PreferenceManager
     */
    public static synchronized PreferenceManager getInstance(Context context) {
        if (instance == null) {
            instance = new PreferenceManager(context);
        }
        return instance;
    }

    /**
     * Constructor
     */
    private PreferenceManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    /**
     * Set login status
     */
    public void setLoggedIn(boolean isLoggedIn) {
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);
        editor.commit();
    }

    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * Save user credentials
     */
    public void saveUserCredentials(String userName, String password) {
        editor.putString(KEY_USER_NAME, userName);
        editor.putString(KEY_USER_PASSWORD, password);
        editor.commit();
    }

    /**
     * Get saved user name
     */
    public String getUserName() {
        return pref.getString(KEY_USER_NAME, null);
    }

    /**
     * Get saved password
     */
    public String getUserPassword() {
        return pref.getString(KEY_USER_PASSWORD, null);
    }

    /**
     * Clear all preferences (for logout)
     */
    public void clearPreferences() {
        editor.clear();
        editor.commit();
    }

    /**
     * Save timer state
     */
    public void saveTimerState(boolean isRunning, boolean isFocusTime, long timeLeft, long totalTime) {
        editor.putBoolean(KEY_TIMER_RUNNING, isRunning);
        editor.putBoolean(KEY_IS_FOCUS_TIME, isFocusTime);
        editor.putLong(KEY_TIME_LEFT, timeLeft);
        editor.putLong(KEY_TOTAL_TIME, totalTime);
        editor.commit();
    }

    /**
     * Check if timer was running
     */
    public boolean wasTimerRunning() {
        return pref.getBoolean(KEY_TIMER_RUNNING, false);
    }

    /**
     * Get timer focus state
     */
    public boolean isFocusTime() {
        return pref.getBoolean(KEY_IS_FOCUS_TIME, true);
    }

    /**
     * Get time left in timer
     */
    public long getTimeLeft() {
        return pref.getLong(KEY_TIME_LEFT, 25 * 60 * 1000); // Default 25 min
    }

    /**
     * Get total timer duration
     */
    public long getTotalTime() {
        return pref.getLong(KEY_TOTAL_TIME, 25 * 60 * 1000); // Default 25 min
    }
}