package com.pranav.saarthi;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper implements NotesRepository {

    private static final String DATABASE_NAME = "SaarthiDB";
    private static final int DATABASE_VERSION = 3;

    public static final String TABLE_NOTES = "notes";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TEXT = "text";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_DATE_TIME = "date_time";
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_SUB_CATEGORY = "sub_category";
    public static final String COLUMN_STATUS = "status";
    public static final String COLUMN_REMARKS = "remarks";
    public static final String COLUMN_COMMENT = "comment";

    // Tasks Table
    public static final String TABLE_TASKS = "tasks";
    public static final String COLUMN_TASK_NAME = "name";
    public static final String COLUMN_TASK_TEXT = "text";
    public static final String COLUMN_TASK_TITLE = "title";
    public static final String COLUMN_TASK_STATUS = "status";
    public static final String COLUMN_TASK_REMARKS = "remarks";
    public static final String COLUMN_TASK_DATE_TIME = "date_time";

    private static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NOTES + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_TEXT + " TEXT, " +
            COLUMN_TITLE + " TEXT, " +
            COLUMN_DATE_TIME + " TEXT, " +
            COLUMN_CATEGORY + " TEXT, " +
            COLUMN_SUB_CATEGORY + " TEXT, " +
            COLUMN_STATUS + " TEXT, " +
            COLUMN_REMARKS + " TEXT, " +
            COLUMN_COMMENT + " TEXT" +
            ");";

    private static final String TABLE_TASKS_CREATE = "CREATE TABLE " + TABLE_TASKS + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_TASK_NAME + " TEXT, " +
            COLUMN_TASK_TEXT + " TEXT, " +
            COLUMN_TASK_TITLE + " TEXT, " +
            COLUMN_TASK_STATUS + " TEXT, " +
            COLUMN_TASK_REMARKS + " TEXT, " +
            COLUMN_TASK_DATE_TIME + " TEXT" +
            ");";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
        db.execSQL(TABLE_TASKS_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            db.execSQL(TABLE_TASKS_CREATE);
        } else {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
            onCreate(db);
        }
    }

    @Override
    public long addNote(String text, String title, String category, String subCategory, String status, String remarks,
            String comment) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TEXT, text);
        values.put(COLUMN_TITLE, title);

        String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        values.put(COLUMN_DATE_TIME, currentTime);

        values.put(COLUMN_CATEGORY, category);
        values.put(COLUMN_SUB_CATEGORY, subCategory);
        values.put(COLUMN_STATUS, status);
        values.put(COLUMN_REMARKS, remarks);
        values.put(COLUMN_COMMENT, comment);

        long id = db.insert(TABLE_NOTES, null, values);
        db.close();
        return id;
    }

    @Override
    public List<Note> getAllNotes() {
        List<Note> notes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NOTES, null, null, null, null, null, COLUMN_DATE_TIME + " DESC");

        if (cursor.moveToFirst()) {
            do {
                Note note = new Note(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TEXT)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE_TIME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SUB_CATEGORY)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REMARKS)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMMENT)));
                notes.add(note);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return notes;
    }

    @Override
    public void deleteNote(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NOTES, COLUMN_ID + " = ?", new String[] { String.valueOf(id) });
        db.close();
    }

    @Override
    public Note generateTitle(int noteId) {
        // Not supported locally
        return null;
    }

    @Override
    public List<Note> generateTitlesAll() {
        // Not supported locally
        return new ArrayList<>();
    }

    @Override
    public List<Note> generateTitlesToday() {
        // Not supported locally
        return new ArrayList<>();
    }

    @Override
    public long addTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TASK_NAME, task.getName());
        values.put(COLUMN_TASK_TEXT, task.getText());
        values.put(COLUMN_TASK_TITLE, task.getTitle());
        values.put(COLUMN_TASK_STATUS, task.getStatus());
        values.put(COLUMN_TASK_REMARKS, task.getRemarks());

        String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        values.put(COLUMN_TASK_DATE_TIME, currentTime);

        long id = db.insert(TABLE_TASKS, null, values);
        db.close();
        return id;
    }

    @Override
    public List<Task> getAllTasks() {
        List<Task> tasks = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_TASKS, null, null, null, null, null, COLUMN_TASK_DATE_TIME + " DESC");

        if (cursor.moveToFirst()) {
            do {
                Task task = new Task(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_TEXT)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_TITLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_STATUS)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_REMARKS)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_DATE_TIME)));
                tasks.add(task);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return tasks;
    }

    @Override
    public void updateTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TASK_NAME, task.getName());
        values.put(COLUMN_TASK_TEXT, task.getText());
        values.put(COLUMN_TASK_TITLE, task.getTitle());
        values.put(COLUMN_TASK_STATUS, task.getStatus());
        values.put(COLUMN_TASK_REMARKS, task.getRemarks());
        // We might not want to update the date_time on edit, or maybe we do.
        // For now, let's keep the original timestamp.

        db.update(TABLE_TASKS, values, COLUMN_ID + " = ?", new String[] { String.valueOf(task.getId()) });
        db.close();
    }

    @Override
    public void deleteTaskFromRepo(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TASKS, COLUMN_ID + " = ?", new String[] { String.valueOf(id) });
        db.close();
    }
}
