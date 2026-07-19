package com.example.habitat.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.habitat.R;
import com.example.habitat.data.models.HabitRecord;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class HabitCalendarAdapter extends BaseAdapter {
    private final Context context;
    private final Calendar calendar;
    private final Set<String> completedDates;
    private final LayoutInflater inflater;

    public HabitCalendarAdapter(Context context, List<HabitRecord> records) {
        this.context = context;
        this.calendar = Calendar.getInstance();
        this.inflater = LayoutInflater.from(context);

        // Get current month and set day to 1
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        // Extract dates from records
        this.completedDates = new HashSet<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        for (HabitRecord record : records) {
            if (record.getDate() != null) {
                completedDates.add(record.getDate());
            }
        }
    }

    public void setMonth(int month, int year) {
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        // Get days in month plus offset for first day of week
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        Calendar firstDay = (Calendar) calendar.clone();
        firstDay.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfWeek = firstDay.get(Calendar.DAY_OF_WEEK) - 1;

        return daysInMonth + firstDayOfWeek;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.custom_calendar_day, parent, false);
        }

        TextView dayText = convertView.findViewById(R.id.calendar_day_text);
        View completionMarker = convertView.findViewById(R.id.completion_marker);

        // Get first day of month and offset
        Calendar firstDay = (Calendar) calendar.clone();
        firstDay.set(Calendar.DAY_OF_MONTH, 1);
        int offset = firstDay.get(Calendar.DAY_OF_WEEK) - 1;

        // Calculate actual day number
        int dayNumber = position - offset + 1;

        if (position < offset || dayNumber > calendar.getActualMaximum(Calendar.DAY_OF_MONTH)) {
            // Empty cell
            dayText.setText("");
            completionMarker.setVisibility(View.GONE);
        } else {
            // Set day number
            dayText.setText(String.valueOf(dayNumber));

            // Check if this day is completed
            Calendar day = (Calendar) calendar.clone();
            day.set(Calendar.DAY_OF_MONTH, dayNumber);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String dateString = sdf.format(day.getTime());

            if (completedDates.contains(dateString)) {
                // This day is completed, show marker
                completionMarker.setVisibility(View.VISIBLE);
                dayText.setTextColor(Color.WHITE);
            } else {
                // Not completed
                completionMarker.setVisibility(View.GONE);
                dayText.setTextColor(Color.LTGRAY);
            }

            // Highlight today
            Calendar today = Calendar.getInstance();
            if (day.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    day.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                    day.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)) {
                dayText.setTextColor(context.getResources().getColor(R.color.colorAccent));
            }
        }

        return convertView;
    }
}