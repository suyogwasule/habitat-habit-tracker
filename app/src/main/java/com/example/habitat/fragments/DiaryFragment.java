package com.example.habitat.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.habitat.R;
import com.example.habitat.adapters.DiaryAdapter;
import com.example.habitat.data.HabitatDatabaseHelper;
import com.example.habitat.data.models.DiaryEntry;
import com.example.habitat.utils.FileUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Fragment for the Diary tab - manages diary entries
 */
public class DiaryFragment extends Fragment implements DiaryAdapter.DiaryItemClickListener {

    private RecyclerView recyclerView;
    private View emptyView;
    private DiaryAdapter adapter;
    private HabitatDatabaseHelper dbHelper;
    private List<DiaryEntry> diaryList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_diary, container, false);

        // Initialize views
        recyclerView = view.findViewById(R.id.diary_recycler_view);
        emptyView = view.findViewById(R.id.empty_diary_view);
        FloatingActionButton addButton = view.findViewById(R.id.add_diary_button);

        // Initialize database helper
        dbHelper = new HabitatDatabaseHelper(getContext());

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Load diary entries from database
        loadDiaryEntries();

        // Set up add button
        addButton.setOnClickListener(v -> showAddDiaryDialog());

        return view;
    }

    /**
     * Load diary entries from database and update UI
     */
    private void loadDiaryEntries() {
        diaryList = dbHelper.getAllDiaryEntries();

        // Show empty view if no entries
        if (diaryList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);

            // Set up adapter
            adapter = new DiaryAdapter(getContext(), diaryList, this);
            recyclerView.setAdapter(adapter);
        }
    }

    /**
     * Show dialog to add a new diary entry
     */
    private void showAddDiaryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("New Diary Entry");

        // Inflate custom layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_diary, null);
        builder.setView(dialogView);

        // Get dialog views
        final EditText titleInput = dialogView.findViewById(R.id.diary_title_input);
        final EditText contentInput = dialogView.findViewById(R.id.diary_content_input);

        // Set current date as default title
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());
        titleInput.setText("Entry: " + currentDate);

        // Set up buttons
        builder.setPositiveButton("Save", (dialog, which) -> {
            // Validate input
            String title = titleInput.getText().toString().trim();
            String content = contentInput.getText().toString().trim();

            if (title.isEmpty()) {
                Toast.makeText(getContext(), "Please enter a title", Toast.LENGTH_SHORT).show();
                return;
            }

            if (content.isEmpty()) {
                Toast.makeText(getContext(), "Please enter some content", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save to database
            long entryId = dbHelper.createDiaryEntry(title, content);

            if (entryId != -1) {
                // Also save to file (demonstrating internal storage)
                saveEntryToFile(title, content);

                // Reload entries
                loadDiaryEntries();

                // Show success message
                Toast.makeText(getContext(), "Entry saved successfully", Toast.LENGTH_SHORT).show();
            } else {
                // Show error message
                Toast.makeText(getContext(), "Failed to save entry", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);

        // Create and show dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Save diary entry to internal storage file
     */
    private void saveEntryToFile(String title, String content) {
        // Create filename based on date
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String filename = "diary_" + dateFormat.format(new Date()) + ".txt";

        // Format content
        String fileContent = title + "\n\n" + content;

        // Save to file using FileUtils
        FileUtils.saveToInternalStorage(getContext(), filename, fileContent);
    }

    /**
     * Handle diary item click
     */
    @Override
    public void onDiaryItemClick(DiaryEntry entry) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(entry.getTitle());
        builder.setMessage(entry.getContent());
        builder.setPositiveButton("Close", null);

        // Add option to export entry
        builder.setNeutralButton("Export", (dialog, which) -> exportDiaryEntry(entry));

        // Create and show dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Handle diary item delete
     */
    @Override
    public void onDiaryItemDelete(int entryId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Delete Entry");
        builder.setMessage("Are you sure you want to delete this entry?");
        builder.setPositiveButton("Delete", (dialog, which) -> {
            // Delete from database
            dbHelper.deleteDiaryEntry(entryId);

            // Reload entries
            loadDiaryEntries();

            // Show success message
            Toast.makeText(getContext(), "Entry deleted", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Cancel", null);

        // Create and show dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Export diary entry to external storage
     */
    private void exportDiaryEntry(DiaryEntry entry) {
        String content = entry.getTitle() + "\n\n" + entry.getContent();
        boolean success = FileUtils.saveToExternalStorage(getContext(), content, "DiaryExports");

        if (success) {
            Toast.makeText(getContext(), "Entry exported successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Failed to export entry", Toast.LENGTH_SHORT).show();
        }
    }
}