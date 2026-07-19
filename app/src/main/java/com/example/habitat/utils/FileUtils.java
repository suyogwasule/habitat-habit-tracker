package com.example.habitat.utils;


import android.content.Context;
import android.os.Environment;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Utility class for file operations
 */
public class FileUtils {

    private static final String TAG = "FileUtils";

    /**
     * Save text to a file in internal storage
     */
    public static boolean saveToInternalStorage(Context context, String filename, String content) {
        try {
            FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
            fos.write(content.getBytes());
            fos.close();
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Error saving to internal storage", e);
            return false;
        }
    }

    /**
     * Read text from a file in internal storage
     */
    public static String readFromInternalStorage(Context context, String filename) {
        try {
            FileInputStream fis = context.openFileInput(filename);
            int size = fis.available();
            byte[] buffer = new byte[size];
            fis.read(buffer);
            fis.close();
            return new String(buffer);
        } catch (IOException e) {
            Log.e(TAG, "Error reading from internal storage", e);
            return null;
        }
    }

    /**
     * Save text to a file in external storage
     */
    public static boolean saveToExternalStorage(Context context, String content, String folderName) {
        // Get external files directory (requires no special permissions in modern Android)
        File exportDir = context.getExternalFilesDir(folderName);
        if (exportDir != null && !exportDir.exists()) {
            if (!exportDir.mkdirs()) {
                Log.e(TAG, "Failed to create directory");
                return false;
            }
        }

        try {
            // Create filename with timestamp
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
            String filename = "export_" + dateFormat.format(new Date()) + ".txt";
            File file = new File(exportDir, filename);

            // Write to file
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(content.getBytes());
            fos.close();

            return true;
        } catch (IOException e) {
            Log.e(TAG, "Error saving to external storage", e);
            return false;
        }
    }

    /**
     * Get path to a file in app's external files directory
     */
    public static String getExternalFilePath(Context context, String folderName, String filename) {
        File file = new File(context.getExternalFilesDir(folderName), filename);
        return file.getAbsolutePath();
    }

    /**
     * Check if external storage is available for read and write
     */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /**
     * Copy database file to external storage (for backup)
     */
    public static boolean exportDatabase(Context context, String databaseName) {
        try {
            File backupDir = context.getExternalFilesDir("DatabaseBackup");
            if (backupDir != null && !backupDir.exists()) {
                backupDir.mkdirs();
            }

            File currentDB = context.getDatabasePath(databaseName);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
            String backupName = databaseName + "_" + dateFormat.format(new Date()) + ".db";
            File backupDB = new File(backupDir, backupName);

            if (currentDB.exists()) {
                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
                return true;
            }
            return false;
        } catch (IOException e) {
            Log.e(TAG, "Error exporting database", e);
            return false;
        }
    }
}