package com.example.habitat.adapters;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.habitat.R;
import com.example.habitat.data.models.Task;
import java.util.List;

/**
 * Adapter for tasks RecyclerView
 */
public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.ViewHolder> {

    private final Context context;
    private final List<Task> taskList;
    private final TaskCheckListener listener;

    public TaskAdapter(Context context, List<Task> taskList, TaskCheckListener listener) {
        this.context = context;
        this.taskList = taskList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_task, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Task task = taskList.get(position);

        holder.taskTitle.setText(task.getTitle());
        holder.taskDescription.setText(task.getDescription());

        // Set checkbox state and listener
        holder.taskCheckbox.setOnCheckedChangeListener(null); // Remove previous listener
        holder.taskCheckbox.setChecked(task.isCompleted());
        holder.taskCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) {
                listener.onTaskChecked(task.getId(), isChecked);
            }
        });

        // Apply strikethrough and alpha for completed tasks
        if (task.isCompleted()) {
            holder.taskTitle.setPaintFlags(holder.taskTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.taskDescription.setPaintFlags(holder.taskDescription.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.taskTitle.setAlpha(0.6f);
            holder.taskDescription.setAlpha(0.6f);
            holder.itemView.setBackgroundResource(R.drawable.task_completed_background);
        } else {
            holder.taskTitle.setPaintFlags(holder.taskTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.taskDescription.setPaintFlags(holder.taskDescription.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.taskTitle.setAlpha(1.0f);
            holder.taskDescription.setAlpha(1.0f);
            holder.itemView.setBackgroundResource(R.drawable.task_background);
        }
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    /**
     * ViewHolder class for task items
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox taskCheckbox;
        TextView taskTitle;
        TextView taskDescription;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            taskCheckbox = itemView.findViewById(R.id.task_checkbox);
            taskTitle = itemView.findViewById(R.id.task_title);
            taskDescription = itemView.findViewById(R.id.task_description);
        }
    }

    /**
     * Interface for task check events
     */
    public interface TaskCheckListener {
        void onTaskChecked(int taskId, boolean isCompleted);
    }
}