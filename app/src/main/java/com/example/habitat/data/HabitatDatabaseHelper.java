package com.example.habitat.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.habitat.data.models.DiaryEntry;
import com.example.habitat.data.models.Habit;
import com.example.habitat.data.models.HabitRecord;
import com.example.habitat.data.models.Task;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Database helper class for SQLite operations
 */
public class HabitatDatabaseHelper extends SQLiteOpenHelper {

    // Database Info
    private static final String DATABASE_NAME = "habitat.db";
    private static final int DATABASE_VERSION = 1;

    // Table Names
    private static final String TABLE_HABITS = "habits";
    private static final String TABLE_TASKS = "tasks";
    private static final String TABLE_DIARY = "diary";
    private static final String TABLE_HABIT_RECORDS = "habit_records";

    // Common Column Names
    private static final String KEY_ID = "id";
    private static final String KEY_CREATED_AT = "created_at";

    // HABITS Table - column names
    private static final String KEY_HABIT_TITLE = "title";
    private static final String KEY_NOTIFICATION_TIME = "notification_time";
    private static final String KEY_DURATION_DAYS = "duration_days";
    private static final String KEY_STREAK = "streak";

    // TASKS Table - column names
    private static final String KEY_TASK_TITLE = "title";
    private static final String KEY_TASK_DESCRIPTION = "description";
    private static final String KEY_TASK_COMPLETED = "completed";

    // DIARY Table - column names
    private static final String KEY_DIARY_TITLE = "title";
    private static final String KEY_DIARY_CONTENT = "content";

    // HABIT_RECORDS Table - column names
    private static final String KEY_HABIT_ID = "habit_id";
    private static final String KEY_DATE = "date";

    // Table Create Statements
    // Habits table create statement
    private static final String CREATE_TABLE_HABITS = "CREATE TABLE " + TABLE_HABITS + "("
            + KEY_ID + " INTEGER PRIMARY KEY,"
            + KEY_HABIT_TITLE + " TEXT,"
            + KEY_NOTIFICATION_TIME + " TEXT,"
            + KEY_DURATION_DAYS + " INTEGER,"
            + KEY_STREAK + " INTEGER DEFAULT 0,"
            + KEY_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP"
            + ")";

    // Tasks table create statement
    private static final String CREATE_TABLE_TASKS = "CREATE TABLE " + TABLE_TASKS + "("
            + KEY_ID + " INTEGER PRIMARY KEY,"
            + KEY_TASK_TITLE + " TEXT,"
            + KEY_TASK_DESCRIPTION + " TEXT,"
            + KEY_TASK_COMPLETED + " INTEGER DEFAULT 0,"
            + KEY_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP"
            + ")";

    // Diary table create statement
    private static final String CREATE_TABLE_DIARY = "CREATE TABLE " + TABLE_DIARY + "("
            + KEY_ID + " INTEGER PRIMARY KEY,"
            + KEY_DIARY_TITLE + " TEXT,"
            + KEY_DIARY_CONTENT + " TEXT,"
            + KEY_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP"
            + ")";

    // Habit records table create statement
    private static final String CREATE_TABLE_HABIT_RECORDS = "CREATE TABLE " + TABLE_HABIT_RECORDS + "("
            + KEY_ID + " INTEGER PRIMARY KEY,"
            + KEY_HABIT_ID + " INTEGER,"
            + KEY_DATE + " TEXT,"
            + "UNIQUE(" + KEY_HABIT_ID + ", " + KEY_DATE + ")"
            + ")";

    public HabitatDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Creating required tables
        db.execSQL(CREATE_TABLE_HABITS);
        db.execSQL(CREATE_TABLE_TASKS);
        db.execSQL(CREATE_TABLE_DIARY);
        db.execSQL(CREATE_TABLE_HABIT_RECORDS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // On upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HABITS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DIARY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HABIT_RECORDS);

        // Create new tables
        onCreate(db);
    }

    /*
     * HABIT METHODS
     */

    /**
     * Adding a new habit
     */
    public long addHabit(String title, String notificationTime, int durationDays) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_HABIT_TITLE, title);
        values.put(KEY_NOTIFICATION_TIME, notificationTime);
        values.put(KEY_DURATION_DAYS, durationDays);

        // Insert row
        long habitId = db.insert(TABLE_HABITS, null, values);

        return habitId;
    }


    /**
     * Get a single habit
     */
    public Habit getHabit(long habitId) {
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_HABITS + " WHERE "
                + KEY_ID + " = " + habitId;

        Cursor c = db.rawQuery(selectQuery, null);

        Habit habit = new Habit();

        if (c != null && c.moveToFirst()) {
            // Use column names directly with getColumnIndexOrThrow for safety
            habit.setId(c.getInt(c.getColumnIndexOrThrow(KEY_ID)));
            habit.setTitle(c.getString(c.getColumnIndexOrThrow(KEY_HABIT_TITLE)));
            habit.setNotificationTime(c.getString(c.getColumnIndexOrThrow(KEY_NOTIFICATION_TIME)));
            habit.setDurationDays(c.getInt(c.getColumnIndexOrThrow(KEY_DURATION_DAYS)));
            habit.setStreak(c.getInt(c.getColumnIndexOrThrow(KEY_STREAK)));
            habit.setCreatedAt(c.getString(c.getColumnIndexOrThrow(KEY_CREATED_AT)));

            c.close();
        }

        return habit;
    }

    /**
     * Get all habits
     */
    public List<Habit> getAllHabits() {
        List<Habit> habits = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_HABITS
                + " ORDER BY " + KEY_CREATED_AT + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // Loop through all rows and add to list
        if (c.moveToFirst()) {
            do {
                Habit habit = new Habit();

                // Safely get column indices
                int idIdx = c.getColumnIndexOrThrow(KEY_ID);
                int titleIdx = c.getColumnIndexOrThrow(KEY_HABIT_TITLE);
                int notificationTimeIdx = c.getColumnIndexOrThrow(KEY_NOTIFICATION_TIME);
                int durationDaysIdx = c.getColumnIndexOrThrow(KEY_DURATION_DAYS);
                int streakIdx = c.getColumnIndexOrThrow(KEY_STREAK);
                int createdAtIdx = c.getColumnIndexOrThrow(KEY_CREATED_AT);

                // Use the indices to get values
                habit.setId(c.getInt(idIdx));
                habit.setTitle(c.getString(titleIdx));
                habit.setNotificationTime(c.getString(notificationTimeIdx));
                habit.setDurationDays(c.getInt(durationDaysIdx));
                habit.setStreak(c.getInt(streakIdx));
                habit.setCreatedAt(c.getString(createdAtIdx));

                // Adding to habits list
                habits.add(habit);
            } while (c.moveToNext());
        }

        c.close();

        return habits;
    }

    /**
     * Update a habit's streak
     */
    public int updateHabitStreak(long habitId, int streak) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_STREAK, streak);

        // Updating row
        return db.update(TABLE_HABITS, values, KEY_ID + " = ?",
                new String[] { String.valueOf(habitId) });
    }

    /**
     * Delete a habit
     */
    public void deleteHabit(long habitId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_HABITS, KEY_ID + " = ?",
                new String[] { String.valueOf(habitId) });

        // Also delete associated habit records
        db.delete(TABLE_HABIT_RECORDS, KEY_HABIT_ID + " = ?",
                new String[] { String.valueOf(habitId) });
    }

    /**
     * Record habit completion for a specific date (today by default)
     */
    public long recordHabitCompletion(int habitId, String date) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Use today's date if not specified
        if (date == null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            date = dateFormat.format(new Date());
        }

        ContentValues values = new ContentValues();
        values.put(KEY_HABIT_ID, habitId);
        values.put(KEY_DATE, date);

        // Insert row, will fail silently if record already exists due to UNIQUE constraint
        return db.insertWithOnConflict(TABLE_HABIT_RECORDS, null, values,
                SQLiteDatabase.CONFLICT_IGNORE);
    }

    /**
     * Record habit completion for today
     */
    public long recordHabitCompletion(int habitId) {
        return recordHabitCompletion(habitId, null);
    }

    /**
     * Remove habit completion record for a specific date (today by default)
     */
    public int removeHabitCompletion(int habitId, String date) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Use today's date if not specified
        if (date == null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            date = dateFormat.format(new Date());
        }

        // Delete record
        return db.delete(TABLE_HABIT_RECORDS,
                KEY_HABIT_ID + " = ? AND " + KEY_DATE + " = ?",
                new String[] { String.valueOf(habitId), date });
    }

    /**
     * Remove habit completion record for today
     */
    public int removeHabitCompletion(int habitId) {
        return removeHabitCompletion(habitId, null);
    }

    /**
     * Get all habit completion records for a specific habit
     */
    public List<HabitRecord> getHabitCompletionRecords(int habitId) {
        List<HabitRecord> records = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_HABIT_RECORDS +
                " WHERE " + KEY_HABIT_ID + " = " + habitId +
                " ORDER BY " + KEY_DATE + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // Loop through all rows and add to list
        if (c.moveToFirst()) {
            do {
                HabitRecord record = new HabitRecord();

                // Safely get column indices
                int idIdx = c.getColumnIndexOrThrow(KEY_ID);
                int habitIdIdx = c.getColumnIndexOrThrow(KEY_HABIT_ID);
                int dateIdx = c.getColumnIndexOrThrow(KEY_DATE);

                // Use the indices to get values
                record.setId(c.getInt(idIdx));
                record.setHabitId(c.getInt(habitIdIdx));
                record.setDate(c.getString(dateIdx));

                // Adding to records list
                records.add(record);
            } while (c.moveToNext());
        }

        c.close();

        return records;
    }

    /*
     * TASK METHODS
     */

    /**
     * Create a new task
     */
    public long createTask(String title, String description) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_TASK_TITLE, title);
        values.put(KEY_TASK_DESCRIPTION, description);

        // Insert row
        long taskId = db.insert(TABLE_TASKS, null, values);

        return taskId;
    }

    /**
     * Get all tasks
     */
    public List<Task> getAllTasks() {
        List<Task> tasks = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_TASKS
                + " ORDER BY " + KEY_CREATED_AT + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // Loop through all rows and add to list
        if (c.moveToFirst()) {
            do {
                Task task = new Task();

                // Safely get column indices
                int idIdx = c.getColumnIndexOrThrow(KEY_ID);
                int titleIdx = c.getColumnIndexOrThrow(KEY_TASK_TITLE);
                int descriptionIdx = c.getColumnIndexOrThrow(KEY_TASK_DESCRIPTION);
                int completedIdx = c.getColumnIndexOrThrow(KEY_TASK_COMPLETED);
                int createdAtIdx = c.getColumnIndexOrThrow(KEY_CREATED_AT);

                // Use the indices to get values
                task.setId(c.getInt(idIdx));
                task.setTitle(c.getString(titleIdx));
                task.setDescription(c.getString(descriptionIdx));
                task.setCompleted(c.getInt(completedIdx) == 1);
                task.setCreatedAt(c.getString(createdAtIdx));

                // Adding to tasks list
                tasks.add(task);
            } while (c.moveToNext());
        }

        c.close();

        return tasks;
    }

    /**
     * Update task completion status
     */
    public int updateTaskStatus(long taskId, boolean isCompleted) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_TASK_COMPLETED, isCompleted ? 1 : 0);

        // Updating row
        return db.update(TABLE_TASKS, values, KEY_ID + " = ?",
                new String[] { String.valueOf(taskId) });
    }

    /**
     * Delete a task
     */
    public void deleteTask(long taskId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TASKS, KEY_ID + " = ?",
                new String[] { String.valueOf(taskId) });
    }

    /**
     * Delete all completed tasks
     */
    public int deleteCompletedTasks() {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_TASKS, KEY_TASK_COMPLETED + " = ?",
                new String[] { "1" });
    }

    /*
     * DIARY METHODS
     */

    /**
     * Create a new diary entry
     */
    public long createDiaryEntry(String title, String content) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_DIARY_TITLE, title);
        values.put(KEY_DIARY_CONTENT, content);

        // Insert row
        long entryId = db.insert(TABLE_DIARY, null, values);

        return entryId;
    }

    /**
     * Get all diary entries
     */
    public List<DiaryEntry> getAllDiaryEntries() {
        List<DiaryEntry> entries = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_DIARY
                + " ORDER BY " + KEY_CREATED_AT + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // Loop through all rows and add to list
        if (c.moveToFirst()) {
            do {
                DiaryEntry entry = new DiaryEntry();

                // Safely get column indices
                int idIdx = c.getColumnIndexOrThrow(KEY_ID);
                int titleIdx = c.getColumnIndexOrThrow(KEY_DIARY_TITLE);
                int contentIdx = c.getColumnIndexOrThrow(KEY_DIARY_CONTENT);
                int createdAtIdx = c.getColumnIndexOrThrow(KEY_CREATED_AT);

                // Use the indices to get values
                entry.setId(c.getInt(idIdx));
                entry.setTitle(c.getString(titleIdx));
                entry.setContent(c.getString(contentIdx));
                entry.setCreatedAt(c.getString(createdAtIdx));

                // Adding to entries list
                entries.add(entry);
            } while (c.moveToNext());
        }

        c.close();

        return entries;
    }

    /**
     * Delete a diary entry
     */
    public void deleteDiaryEntry(long entryId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_DIARY, KEY_ID + " = ?",
                new String[] { String.valueOf(entryId) });
    }
}