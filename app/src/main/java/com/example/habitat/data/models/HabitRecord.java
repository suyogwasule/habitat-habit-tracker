package com.example.habitat.data.models;

/**
 * Model class for habit completion record
 */
public class HabitRecord {
    private int id;
    private int habitId;
    private String date;

    // Default constructor
    public HabitRecord() {
    }

    // Constructor with fields
    public HabitRecord(int id, int habitId, String date) {
        this.id = id;
        this.habitId = habitId;
        this.date = date;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getHabitId() {
        return habitId;
    }

    public void setHabitId(int habitId) {
        this.habitId = habitId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}