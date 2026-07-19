package com.example.habitat.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.habitat.R;
import com.example.habitat.data.models.DiaryEntry;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for diary entries RecyclerView
 */
public class DiaryAdapter extends RecyclerView.Adapter<DiaryAdapter.ViewHolder> {

    private final Context context;
    private final List<DiaryEntry> diaryList;
    private final DiaryItemClickListener listener;

    public DiaryAdapter(Context context, List<DiaryEntry> diaryList, DiaryItemClickListener listener) {
        this.context = context;
        this.diaryList = diaryList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_diary, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DiaryEntry entry = diaryList.get(position);

        holder.diaryTitle.setText(entry.getTitle());

        // Format and set date
        String formattedDate = formatDate(entry.getCreatedAt());
        holder.diaryDate.setText(formattedDate);

        // Set preview of content (first few characters)
        String content = entry.getContent();
        if (content.length() > 100) {
            content = content.substring(0, 97) + "...";
        }
        holder.diaryPreview.setText(content);

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDiaryItemClick(entry);
            }
        });

        // Set delete button click listener
        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDiaryItemDelete(entry.getId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return diaryList.size();
    }

    /**
     * Format the date from database format to user-friendly format
     */
    private String formatDate(String dateString) {
        try {
            SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat displayFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
            Date date = dbFormat.parse(dateString);
            return displayFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return dateString;
        }
    }

    /**
     * ViewHolder class for diary items
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView diaryDate;
        TextView diaryTitle;
        TextView diaryPreview;
        ImageButton deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            diaryDate = itemView.findViewById(R.id.diary_date);
            diaryTitle = itemView.findViewById(R.id.diary_title);
            diaryPreview = itemView.findViewById(R.id.diary_preview);
            deleteButton = itemView.findViewById(R.id.diary_delete_button);
        }
    }

    /**
     * Interface for diary item click events
     */
    public interface DiaryItemClickListener {
        void onDiaryItemClick(DiaryEntry entry);
        void onDiaryItemDelete(int entryId);
    }
}