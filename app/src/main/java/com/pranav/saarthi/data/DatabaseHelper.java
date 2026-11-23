package com.pranav.saarthi.data;


import com.pranav.saarthi.model.Note;
import com.pranav.saarthi.model.Task;
import com.pranav.saarthi.notifications.NotificationModel;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Local SQLite store and the source of truth for the app. Notes and tasks keep a
 * server_id and a sync_state; SyncManager uploads pending rows and pulls server rows.
 *
 * Note: DATABASE_VERSION 8 adds server_id/sync_state. A sibling build that still
 * uses version 7 on the same DB name will not be able to open this database.
 */
public class DatabaseHelper extends SQLiteOpenHelper implements NotesRepository {

    private static final String DATABASE_NAME = "SaarthiDB";
    private static final int DATABASE_VERSION = 8;

    /** sync_state values for notes and tasks. */
    public static final int SYNC_SYNCED = 0;
    public static final int SYNC_PENDING_CREATE = 1;
    public static final int SYNC_PENDING_UPDATE = 2;
    public static final int SYNC_PENDING_DELETE = 3;

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
    public static final String COLUMN_SERVER_ID = "server_id";
    public static final String COLUMN_SYNC_STATE = "sync_state";

    // Tasks Table
    public static final String TABLE_TASKS = "tasks";
    public static final String COLUMN_TASK_NAME = "name";
    public static final String COLUMN_TASK_TEXT = "text";
    public static final String COLUMN_TASK_TITLE = "title";
    public static final String COLUMN_TASK_STATUS = "status";
    public static final String COLUMN_TASK_REMARKS = "remarks";
    public static final String COLUMN_TASK_DATE_TIME = "date_time";
    public static final String COLUMN_TASK_CREATED_AT = "created_at";
    public static final String COLUMN_TASK_COMPLETED_AT = "completed_at";

    public static final String TABLE_NOTIFICATIONS = "notifications";
    public static final String COLUMN_NOTIF_PACKAGE = "package_name";
    public static final String COLUMN_NOTIF_TITLE = "title";
    public static final String COLUMN_NOTIF_TEXT = "text";
    public static final String COLUMN_NOTIF_BIG_TEXT = "big_text";
    public static final String COLUMN_NOTIF_TIME = "post_time";
    public static final String COLUMN_NOTIF_REPLY = "reply";
    public static final String COLUMN_NOTIF_KEY = "notification_key";

    private static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NOTES + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_TEXT + " TEXT, " +
            COLUMN_TITLE + " TEXT, " +
            COLUMN_DATE_TIME + " TEXT, " +
            COLUMN_CATEGORY + " TEXT, " +
            COLUMN_SUB_CATEGORY + " TEXT, " +
            COLUMN_STATUS + " TEXT, " +
            COLUMN_REMARKS + " TEXT, " +
            COLUMN_COMMENT + " TEXT, " +
            COLUMN_SERVER_ID + " INTEGER, " +
            COLUMN_SYNC_STATE + " INTEGER NOT NULL DEFAULT " + SYNC_PENDING_CREATE +
            ");";

    private static final String TABLE_TASKS_CREATE = "CREATE TABLE " + TABLE_TASKS + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_TASK_NAME + " TEXT, " +
            COLUMN_TASK_TEXT + " TEXT, " +
            COLUMN_TASK_TITLE + " TEXT, " +
            COLUMN_TASK_STATUS + " TEXT, " +
            COLUMN_TASK_REMARKS + " TEXT, " +
            COLUMN_TASK_DATE_TIME + " TEXT, " +
            COLUMN_TASK_CREATED_AT + " TEXT, " +
            COLUMN_TASK_COMPLETED_AT + " TEXT, " +
            COLUMN_SERVER_ID + " INTEGER, " +
            COLUMN_SYNC_STATE + " INTEGER NOT NULL DEFAULT " + SYNC_PENDING_CREATE +
            ");";

    private static final String TABLE_NOTIFICATIONS_CREATE = "CREATE TABLE " + TABLE_NOTIFICATIONS + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_NOTIF_PACKAGE + " TEXT, " +
            COLUMN_NOTIF_TITLE + " TEXT, " +
            COLUMN_NOTIF_TEXT + " TEXT, " +
            COLUMN_NOTIF_BIG_TEXT + " TEXT, " +
            COLUMN_NOTIF_TIME + " TEXT, " +
            COLUMN_NOTIF_REPLY + " INTEGER, " +
            COLUMN_NOTIF_KEY + " TEXT" +
            ");";

    private final Context appContext;

    /** A row that still needs to be sent to the server, with its payload. */
    public static class Pending<T> {
        public final int localId;
        public final Integer serverId;
        public final int syncState;
        public final T item;

        Pending(int localId, Integer serverId, int syncState, T item) {
            this.localId = localId;
            this.serverId = serverId;
            this.syncState = syncState;
            this.item = item;
        }
    }

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.appContext = context.getApplicationContext();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
        db.execSQL(TABLE_TASKS_CREATE);
        db.execSQL(TABLE_NOTIFICATIONS_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            db.execSQL(TABLE_TASKS_CREATE);
        }
        if (oldVersion < 4) {
            db.execSQL(TABLE_NOTIFICATIONS_CREATE);
        }
        if (oldVersion < 5) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_NOTIFICATIONS + " ADD COLUMN "
                        + COLUMN_NOTIF_REPLY + " INTEGER DEFAULT 0");
            } catch (Exception ignored) {
                // Column may already exist
            }
        }
        if (oldVersion < 6) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_NOTIFICATIONS + " ADD COLUMN "
                        + COLUMN_NOTIF_KEY + " TEXT");
            } catch (Exception ignored) {
                // Column may already exist
            }
        }
        if (oldVersion < 7) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_TASKS + " ADD COLUMN " + COLUMN_TASK_CREATED_AT + " TEXT");
                db.execSQL("ALTER TABLE " + TABLE_TASKS + " ADD COLUMN " + COLUMN_TASK_COMPLETED_AT + " TEXT");
                db.execSQL("UPDATE " + TABLE_TASKS + " SET " + COLUMN_TASK_CREATED_AT + " = "
                        + COLUMN_TASK_DATE_TIME);
            } catch (Exception ignored) {
                // Safe if columns already exist (e.g. upgraded from sibling app)
            }
        }
        if (oldVersion < 8) {
            // Rows that exist before this upgrade were created locally, so they must be uploaded.
            addSyncColumns(db, TABLE_NOTES);
            addSyncColumns(db, TABLE_TASKS);
        }
    }

    private void addSyncColumns(SQLiteDatabase db, String table) {
        try {
            db.execSQL("ALTER TABLE " + table + " ADD COLUMN " + COLUMN_SERVER_ID + " INTEGER");
        } catch (Exception ignored) {
            // Column may already exist
        }
        try {
            db.execSQL("ALTER TABLE " + table + " ADD COLUMN " + COLUMN_SYNC_STATE
                    + " INTEGER NOT NULL DEFAULT " + SYNC_PENDING_CREATE);
        } catch (Exception ignored) {
            // Column may already exist
        }
    }

    // ---------------------------------------------------------------- notes

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
        values.put(COLUMN_SYNC_STATE, SYNC_PENDING_CREATE);

        long id = db.insert(TABLE_NOTES, null, values);
        db.close();
        SyncManager.requestSync(appContext);
        return id;
    }

    @Override
    public List<Note> getAllNotes() {
        List<Note> notes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NOTES, null,
                COLUMN_SYNC_STATE + " != ?", new String[] { String.valueOf(SYNC_PENDING_DELETE) },
                null, null, COLUMN_DATE_TIME + " DESC");

        while (cursor.moveToNext()) {
            notes.add(noteFromCursor(cursor));
        }
        cursor.close();
        db.close();
        return notes;
    }

    @Override
    public void deleteNote(int id) {
        softDelete(TABLE_NOTES, id);
    }

    @Override
    public Note generateTitle(int noteId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Integer serverId = readServerId(db, TABLE_NOTES, noteId);
        int state = readSyncState(db, TABLE_NOTES, noteId);
        db.close();

        // AI titles run on the server, so the note must already exist there.
        if (serverId == null || state != SYNC_SYNCED) {
            return null;
        }
        Note remote = new NetworkNotesRepository().generateTitle(serverId);
        if (remote == null) {
            return null;
        }
        applyTitle(serverId, remote.getTitle());
        return withLocalId(remote, noteId);
    }

    @Override
    public List<Note> generateTitlesAll() {
        return applyRemoteTitles(new NetworkNotesRepository().generateTitlesAll());
    }

    @Override
    public List<Note> generateTitlesToday() {
        return applyRemoteTitles(new NetworkNotesRepository().generateTitlesToday());
    }

    // ---------------------------------------------------------------- tasks

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
        values.put(COLUMN_TASK_CREATED_AT, currentTime);
        values.put(COLUMN_SYNC_STATE, SYNC_PENDING_CREATE);

        long id = db.insert(TABLE_TASKS, null, values);
        db.close();
        SyncManager.requestSync(appContext);
        return id;
    }

    @Override
    public List<Task> getAllTasks() {
        List<Task> tasks = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_TASKS, null,
                COLUMN_SYNC_STATE + " != ?", new String[] { String.valueOf(SYNC_PENDING_DELETE) },
                null, null, COLUMN_TASK_DATE_TIME + " DESC");

        while (cursor.moveToNext()) {
            tasks.add(taskFromCursor(cursor));
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
        // The original date_time is kept on edit.

        // Only a synced row becomes "pending update". A pending create stays a create.
        if (readSyncState(db, TABLE_TASKS, task.getId()) == SYNC_SYNCED) {
            values.put(COLUMN_SYNC_STATE, SYNC_PENDING_UPDATE);
        }

        db.update(TABLE_TASKS, values, COLUMN_ID + " = ?", new String[] { String.valueOf(task.getId()) });
        db.close();
        SyncManager.requestSync(appContext);
    }

    public void updateTaskStatus(int taskId, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TASK_STATUS, status);
        db.update(TABLE_TASKS, values, COLUMN_ID + " = ?", new String[] { String.valueOf(taskId) });
        db.close();
    }

    @Override
    public void deleteTaskFromRepo(int id) {
        softDelete(TABLE_TASKS, id);
    }

    // ------------------------------------------------- sync support (SyncManager)

    public List<Pending<Note>> getPendingNotes() {
        List<Pending<Note>> out = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NOTES, null,
                COLUMN_SYNC_STATE + " != ?", new String[] { String.valueOf(SYNC_SYNCED) },
                null, null, COLUMN_ID + " ASC");
        while (cursor.moveToNext()) {
            out.add(new Pending<>(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    nullableInt(cursor, COLUMN_SERVER_ID),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SYNC_STATE)),
                    noteFromCursor(cursor)));
        }
        cursor.close();
        db.close();
        return out;
    }

    public List<Pending<Task>> getPendingTasks() {
        List<Pending<Task>> out = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_TASKS, null,
                COLUMN_SYNC_STATE + " != ?", new String[] { String.valueOf(SYNC_SYNCED) },
                null, null, COLUMN_ID + " ASC");
        while (cursor.moveToNext()) {
            out.add(new Pending<>(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    nullableInt(cursor, COLUMN_SERVER_ID),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SYNC_STATE)),
                    taskFromCursor(cursor)));
        }
        cursor.close();
        db.close();
        return out;
    }

    public void markNoteSynced(int localId, int serverId) {
        markSynced(TABLE_NOTES, localId, serverId);
    }

    public void markTaskSynced(int localId, int serverId) {
        markSynced(TABLE_TASKS, localId, serverId);
    }

    public void removeNote(int localId) {
        removeRow(TABLE_NOTES, localId);
    }

    public void removeTask(int localId) {
        removeRow(TABLE_TASKS, localId);
    }

    /**
     * Applies a full server list. Synced rows take the server values. Rows with
     * local changes pending are left alone. Synced rows missing from the server are removed.
     */
    public void reconcileNotes(List<Note> remote) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            Set<Integer> remoteIds = new HashSet<>();
            for (Note n : remote) {
                remoteIds.add(n.getId());
                ContentValues values = noteValues(n);
                Integer localId = findLocalId(db, TABLE_NOTES, n.getId());
                if (localId == null) {
                    values.put(COLUMN_SERVER_ID, n.getId());
                    values.put(COLUMN_SYNC_STATE, SYNC_SYNCED);
                    db.insert(TABLE_NOTES, null, values);
                } else if (readSyncState(db, TABLE_NOTES, localId) == SYNC_SYNCED) {
                    db.update(TABLE_NOTES, values, COLUMN_ID + " = ?", new String[] { String.valueOf(localId) });
                }
            }
            removeStaleSynced(db, TABLE_NOTES, remoteIds);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public void reconcileTasks(List<Task> remote) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            Set<Integer> remoteIds = new HashSet<>();
            for (Task t : remote) {
                remoteIds.add(t.getId());
                ContentValues values = taskValues(t);
                Integer localId = findLocalId(db, TABLE_TASKS, t.getId());
                if (localId == null) {
                    values.put(COLUMN_SERVER_ID, t.getId());
                    values.put(COLUMN_SYNC_STATE, SYNC_SYNCED);
                    db.insert(TABLE_TASKS, null, values);
                } else if (readSyncState(db, TABLE_TASKS, localId) == SYNC_SYNCED) {
                    db.update(TABLE_TASKS, values, COLUMN_ID + " = ?", new String[] { String.valueOf(localId) });
                }
            }
            removeStaleSynced(db, TABLE_TASKS, remoteIds);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    // ---------------------------------------------------------------- helpers

    private void softDelete(String table, int localId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String[] args = { String.valueOf(localId) };
        Integer serverId = readServerId(db, table, localId);
        if (serverId == null) {
            // Never uploaded, so nothing exists on the server.
            db.delete(table, COLUMN_ID + " = ?", args);
        } else {
            ContentValues values = new ContentValues();
            values.put(COLUMN_SYNC_STATE, SYNC_PENDING_DELETE);
            db.update(table, values, COLUMN_ID + " = ?", args);
        }
        db.close();
        SyncManager.requestSync(appContext);
    }

    private void markSynced(String table, int localId, int serverId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_SERVER_ID, serverId);
        values.put(COLUMN_SYNC_STATE, SYNC_SYNCED);
        db.update(table, values, COLUMN_ID + " = ?", new String[] { String.valueOf(localId) });
        db.close();
    }

    private void removeRow(String table, int localId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(table, COLUMN_ID + " = ?", new String[] { String.valueOf(localId) });
        db.close();
    }

    private void removeStaleSynced(SQLiteDatabase db, String table, Set<Integer> remoteIds) {
        Cursor cursor = db.query(table, new String[] { COLUMN_ID, COLUMN_SERVER_ID },
                COLUMN_SYNC_STATE + " = ? AND " + COLUMN_SERVER_ID + " IS NOT NULL",
                new String[] { String.valueOf(SYNC_SYNCED) }, null, null, null);
        List<Integer> stale = new ArrayList<>();
        while (cursor.moveToNext()) {
            if (!remoteIds.contains(cursor.getInt(1))) {
                stale.add(cursor.getInt(0));
            }
        }
        cursor.close();
        for (int id : stale) {
            db.delete(table, COLUMN_ID + " = ?", new String[] { String.valueOf(id) });
        }
    }

    private Integer findLocalId(SQLiteDatabase db, String table, int serverId) {
        Cursor cursor = db.query(table, new String[] { COLUMN_ID },
                COLUMN_SERVER_ID + " = ?", new String[] { String.valueOf(serverId) }, null, null, null);
        Integer localId = null;
        if (cursor.moveToFirst()) {
            localId = cursor.getInt(0);
        }
        cursor.close();
        return localId;
    }

    private Integer readServerId(SQLiteDatabase db, String table, int localId) {
        Cursor cursor = db.query(table, new String[] { COLUMN_SERVER_ID },
                COLUMN_ID + " = ?", new String[] { String.valueOf(localId) }, null, null, null);
        Integer serverId = null;
        if (cursor.moveToFirst() && !cursor.isNull(0)) {
            serverId = cursor.getInt(0);
        }
        cursor.close();
        return serverId;
    }

    private int readSyncState(SQLiteDatabase db, String table, int localId) {
        Cursor cursor = db.query(table, new String[] { COLUMN_SYNC_STATE },
                COLUMN_ID + " = ?", new String[] { String.valueOf(localId) }, null, null, null);
        int state = SYNC_SYNCED;
        if (cursor.moveToFirst()) {
            state = cursor.getInt(0);
        }
        cursor.close();
        return state;
    }

    private static Integer nullableInt(Cursor cursor, String column) {
        int index = cursor.getColumnIndexOrThrow(column);
        return cursor.isNull(index) ? null : cursor.getInt(index);
    }

    private void applyTitle(int serverId, String title) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, title);
        db.update(TABLE_NOTES, values, COLUMN_SERVER_ID + " = ?", new String[] { String.valueOf(serverId) });
        db.close();
    }

    private List<Note> applyRemoteTitles(List<Note> remote) {
        List<Note> result = new ArrayList<>();
        if (remote == null) {
            return result;
        }
        SQLiteDatabase db = this.getWritableDatabase();
        for (Note r : remote) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_TITLE, r.getTitle());
            db.update(TABLE_NOTES, values, COLUMN_SERVER_ID + " = ?", new String[] { String.valueOf(r.getId()) });
            Integer localId = findLocalId(db, TABLE_NOTES, r.getId());
            result.add(withLocalId(r, localId != null ? localId : r.getId()));
        }
        db.close();
        return result;
    }

    private static Note withLocalId(Note r, int localId) {
        return new Note(localId, r.getText(), r.getTitle(), r.getDateTime(), r.getCategory(),
                r.getSubCategory(), r.getStatus(), r.getRemarks(), r.getComment());
    }

    private static Note noteFromCursor(Cursor cursor) {
        return new Note(
                cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TEXT)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE_TIME)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SUB_CATEGORY)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REMARKS)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMMENT)));
    }

    private static Task taskFromCursor(Cursor cursor) {
        return new Task(
                cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_NAME)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_TEXT)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_TITLE)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_STATUS)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_REMARKS)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_DATE_TIME)));
    }

    private static ContentValues noteValues(Note n) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_TEXT, n.getText());
        values.put(COLUMN_TITLE, n.getTitle());
        values.put(COLUMN_DATE_TIME, n.getDateTime());
        values.put(COLUMN_CATEGORY, n.getCategory());
        values.put(COLUMN_SUB_CATEGORY, n.getSubCategory());
        values.put(COLUMN_STATUS, n.getStatus());
        values.put(COLUMN_REMARKS, n.getRemarks());
        return values;
    }

    private static ContentValues taskValues(Task t) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_TASK_NAME, t.getName());
        values.put(COLUMN_TASK_TEXT, t.getText());
        values.put(COLUMN_TASK_TITLE, t.getTitle());
        values.put(COLUMN_TASK_STATUS, t.getStatus());
        values.put(COLUMN_TASK_REMARKS, t.getRemarks());
        values.put(COLUMN_TASK_DATE_TIME, t.getDateTime());
        values.put(COLUMN_TASK_CREATED_AT, t.getDateTime());
        return values;
    }

    // ---------------------------------------------------------- notifications

    public long addNotification(NotificationModel notification) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOTIF_PACKAGE, notification.getPackageName());
        values.put(COLUMN_NOTIF_TITLE, notification.getTitle());
        values.put(COLUMN_NOTIF_TEXT, notification.getText());
        values.put(COLUMN_NOTIF_BIG_TEXT, notification.getBigText());
        values.put(COLUMN_NOTIF_TIME, notification.getPostTime());
        values.put(COLUMN_NOTIF_REPLY, notification.getReply() ? 1 : 0);
        values.put(COLUMN_NOTIF_KEY, notification.getNotificationKey());

        long id = db.insert(TABLE_NOTIFICATIONS, null, values);
        db.close();
        return id;
    }

    public List<NotificationModel> getAllNotifications() {
        return getNotificationsPaged(1000, 0, null, null);
    }

    public List<NotificationModel> getNotificationsPaged(
            int limit, int offset, String filterColumn, String filterValue) {
        List<NotificationModel> notifications = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selection = null;
        String[] selectionArgs = null;

        if (filterColumn != null && filterValue != null && !filterValue.isEmpty()) {
            selection = filterColumn + " LIKE ?";
            selectionArgs = new String[] { "%" + filterValue + "%" };
        }

        Cursor cursor = db.query(
                TABLE_NOTIFICATIONS,
                null,
                selection,
                selectionArgs,
                null,
                null,
                COLUMN_ID + " DESC",
                offset + "," + limit);

        if (cursor.moveToFirst()) {
            do {
                notifications.add(new NotificationModel(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTIF_PACKAGE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTIF_TITLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTIF_TEXT)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTIF_BIG_TEXT)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTIF_TIME)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NOTIF_REPLY)) == 1,
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTIF_KEY))));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return notifications;
    }

    public int getTotalNotificationCount(String filterColumn, String filterValue) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = null;
        String[] selectionArgs = null;

        if (filterColumn != null && filterValue != null && !filterValue.isEmpty()) {
            selection = filterColumn + " LIKE ?";
            selectionArgs = new String[] { "%" + filterValue + "%" };
        }

        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_NOTIFICATIONS
                        + (selection != null ? " WHERE " + selection : ""),
                selectionArgs);

        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }
}
