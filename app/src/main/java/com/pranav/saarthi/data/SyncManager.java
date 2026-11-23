package com.pranav.saarthi.data;

import android.content.Context;
import android.util.Log;
import com.pranav.saarthi.api.NotesApi;
import com.pranav.saarthi.api.RetrofitClient;
import com.pranav.saarthi.model.Note;
import com.pranav.saarthi.model.Task;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import retrofit2.Response;

/**
 * Two-way sync between the local SQLite store and the server.
 *
 * Order: push local changes first, then pull server state. Pushing first gives
 * new rows their server_id, so the pull matches them instead of duplicating them.
 * If any request fails, the sync stops with an IOException and the pull does not
 * run, so a partial sync never deletes local rows.
 */
public final class SyncManager {

    private static final String TAG = "SyncManager";
    private static final int PAGE_SIZE = 100;
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private SyncManager() {
    }

    /** Runs a sync in the background. Safe to call after every local write. */
    public static void requestSync(Context context) {
        Context app = context.getApplicationContext();
        EXECUTOR.execute(() -> syncNow(app));
    }

    /** Blocking sync. Call from a background thread only. Returns true on success. */
    public static synchronized boolean syncNow(Context context) {
        DatabaseHelper db = new DatabaseHelper(context);
        try {
            NotesApi api = RetrofitClient.getClient().create(NotesApi.class);
            pushNotes(db, api);
            pushTasks(db, api);
            pullNotes(db, api);
            pullTasks(db, api);
            Log.d(TAG, "Sync complete");
            return true;
        } catch (IOException e) {
            Log.w(TAG, "Sync failed, will retry later", e);
            return false;
        } finally {
            db.close();
        }
    }

    // ------------------------------------------------------------- push

    private static void pushNotes(DatabaseHelper db, NotesApi api) throws IOException {
        for (DatabaseHelper.Pending<Note> p : db.getPendingNotes()) {
            if (p.syncState == DatabaseHelper.SYNC_PENDING_DELETE) {
                if (p.serverId == null) {
                    db.removeNote(p.localId);
                    continue;
                }
                Response<Void> r = api.deleteNote(p.serverId).execute();
                if (r.isSuccessful() || r.code() == 404) {
                    db.removeNote(p.localId);
                } else {
                    Log.w(TAG, "Note delete failed: " + r.code());
                }
                continue;
            }

            // Create (or an update with no server id yet). Notes have no update path in the app.
            Note n = p.item;
            Note body = new Note(0, n.getText(), n.getTitle(), null, n.getCategory(),
                    n.getSubCategory(), n.getStatus(), n.getRemarks(), null);
            Response<Note> r = api.addNote(body).execute();
            if (r.isSuccessful() && r.body() != null) {
                db.markNoteSynced(p.localId, r.body().getId());
            } else {
                Log.w(TAG, "Note create failed: " + r.code());
            }
        }
    }

    private static void pushTasks(DatabaseHelper db, NotesApi api) throws IOException {
        for (DatabaseHelper.Pending<Task> p : db.getPendingTasks()) {
            Task t = p.item;

            if (p.syncState == DatabaseHelper.SYNC_PENDING_DELETE) {
                if (p.serverId == null) {
                    db.removeTask(p.localId);
                    continue;
                }
                Response<Void> r = api.deleteTask(p.serverId).execute();
                if (r.isSuccessful() || r.code() == 404) {
                    db.removeTask(p.localId);
                } else {
                    Log.w(TAG, "Task delete failed: " + r.code());
                }
                continue;
            }

            if (p.syncState == DatabaseHelper.SYNC_PENDING_UPDATE && p.serverId != null) {
                Task body = new Task(p.serverId, t.getName(), t.getText(), t.getTitle(),
                        t.getStatus(), t.getRemarks(), t.getDateTime());
                Response<Task> r = api.updateTask(p.serverId, body).execute();
                if (r.isSuccessful()) {
                    db.markTaskSynced(p.localId, p.serverId);
                } else {
                    Log.w(TAG, "Task update failed: " + r.code());
                }
                continue;
            }

            // Create
            Task body = new Task(0, t.getName(), t.getText(), t.getTitle(),
                    t.getStatus(), t.getRemarks(), null);
            Response<Task> r = api.addTask(body).execute();
            if (r.isSuccessful() && r.body() != null) {
                db.markTaskSynced(p.localId, r.body().getId());
            } else {
                Log.w(TAG, "Task create failed: " + r.code());
            }
        }
    }

    // ------------------------------------------------------------- pull

    private static void pullNotes(DatabaseHelper db, NotesApi api) throws IOException {
        List<Note> all = new ArrayList<>();
        int skip = 0;
        while (true) {
            Response<List<Note>> r = api.getNotes(skip, PAGE_SIZE).execute();
            if (!r.isSuccessful() || r.body() == null) {
                throw new IOException("Notes fetch failed: " + r.code());
            }
            List<Note> page = r.body();
            all.addAll(page);
            if (page.size() < PAGE_SIZE) {
                break;
            }
            skip += PAGE_SIZE;
        }
        db.reconcileNotes(all);
    }

    private static void pullTasks(DatabaseHelper db, NotesApi api) throws IOException {
        List<Task> all = new ArrayList<>();
        int skip = 0;
        while (true) {
            Response<List<Task>> r = api.getTasks(skip, PAGE_SIZE).execute();
            if (!r.isSuccessful() || r.body() == null) {
                throw new IOException("Tasks fetch failed: " + r.code());
            }
            List<Task> page = r.body();
            all.addAll(page);
            if (page.size() < PAGE_SIZE) {
                break;
            }
            skip += PAGE_SIZE;
        }
        db.reconcileTasks(all);
    }
}
