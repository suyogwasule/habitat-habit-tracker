package com.example.habitat.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.habitat.R;
import com.example.habitat.data.HabitatDatabaseHelper;
import com.example.habitat.data.models.Habit;
import com.example.habitat.data.models.HabitRecord;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for habits RecyclerView
 */
public class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.ViewHolder> {

    private final Context context;
    private final List<Habit> habitList;
    private final HabitActionListener listener;

    public HabitAdapter(Context context, List<Habit> habitList, HabitActionListener listener) {
        this.context = context;
        this.habitList = habitList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_habit, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Check if habit was already completed today
     */
    private boolean isHabitCompletedToday(int habitId) {
        HabitatDatabaseHelper dbHelper = new HabitatDatabaseHelper(context);
        List<HabitRecord> records = dbHelper.getHabitCompletionRecords(habitId);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = sdf.format(new Date());

        for (HabitRecord record : records) {
            if (record.getDate() != null && record.getDate().equals(today)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Habit habit = habitList.get(position);

        holder.habitTitle.setText(habit.getTitle());
        holder.habitStreak.setText("Streak: " + habit.getStreak() + " days");
        holder.habitTime.setText("Reminder: " + habit.getNotificationTime());

        // Set checkbox state and listener
        holder.habitCheckbox.setOnCheckedChangeListener(null); // Remove previous listener

        // Check if habit was already completed today
        final int habitId = habit.getId();
        boolean completedToday = isHabitCompletedToday(habitId);

        // Set checkbox state based on completion status
        holder.habitCheckbox.setChecked(completedToday);

        // If already completed today, disable checkbox
        if (completedToday) {
            holder.habitCheckbox.setEnabled(false);
            holder.habitCheckbox.setAlpha(0.7f);
            holder.habitTitle.setAlpha(0.8f);
            holder.habitStreak.setAlpha(0.8f);
            holder.habitTime.setAlpha(0.8f);
        } else {
            holder.habitCheckbox.setEnabled(true);
            holder.habitCheckbox.setAlpha(1.0f);
            holder.habitTitle.setAlpha(1.0f);
            holder.habitStreak.setAlpha(1.0f);
            holder.habitTime.setAlpha(1.0f);
        }

        holder.habitCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null && isChecked) { // Only allow checking, not unchecking
                listener.onHabitCompleted(habitId, true);
                // Disable checkbox after checking to prevent multiple checks
                buttonView.setEnabled(false);
                buttonView.setAlpha(0.7f);
                holder.habitTitle.setAlpha(0.8f);
                holder.habitStreak.setAlpha(0.8f);
                holder.habitTime.setAlpha(0.8f);
            }
        });

        // Set item click listener to show calendar
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onHabitItemClick(habit);
            }
        });

        // Set delete button click listener
        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onHabitDeleteClick(habitId);
            }
        });
    }

    @Override
    public int getItemCount() {
        return habitList.size();
    }

    /**
     * ViewHolder class for habit items
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox habitCheckbox;
        TextView habitTitle;
        TextView habitStreak;
        TextView habitTime;
        ImageButton deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            habitCheckbox = itemView.findViewById(R.id.habit_checkbox);
            habitTitle = itemView.findViewById(R.id.habit_title);
            habitStreak = itemView.findViewById(R.id.habit_streak);
            habitTime = itemView.findViewById(R.id.habit_time);
            deleteButton = itemView.findViewById(R.id.habit_delete_button);
        }
    }

    /**
     * Interface for habit actions
     */
    public interface HabitActionListener {
        void onHabitCompleted(int habitId, boolean isCompleted);
        void onHabitItemClick(Habit habit);
        void onHabitDeleteClick(int habitId);
    }
}