package com.pranav.saarthi.sync;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.pranav.saarthi.api.NotesApi;
import com.pranav.saarthi.api.RetrofitClient;
import com.pranav.saarthi.data.DatabaseHelper;
import com.pranav.saarthi.model.Task;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Response;

public class TaskSyncWorker extends Worker {

    private static final String TAG = "TaskSyncWorker";
    private static final int MAX_HEALTH_ATTEMPTS = 5;
    public static final String LOCAL_SYNCED_STATUS = "synced";

    public TaskSyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        int attempt = getRunAttemptCount();
        Log.d(TAG, "Task sync run attempt " + attempt);

        if (uploadLocalTasks(context)) {
            markSuccess(context);
            return Result.success();
        }

        TaskSyncPrefs.setStatus(context, "Sync failed. Checking API health...");
        if (isApiHealthy()) {
            TaskSyncPrefs.setStatus(context, "API online. Restarting sync...");
            if (uploadLocalTasks(context)) {
                markSuccess(context);
                return Result.success();
            }
            TaskSyncPrefs.setStatus(context, "Sync failed while API is online.");
            return Result.failure();
        }

        if (attempt >= MAX_HEALTH_ATTEMPTS) {
            TaskSyncPrefs.setStatus(context, "API stayed offline. Sync paused.");
            Log.w(TAG, "Health check failed " + MAX_HEALTH_ATTEMPTS + " times; stopping");
            return Result.failure();
        }

        TaskSyncPrefs.setStatus(context,
                "API offline. Retrying health check in 2 minutes ("
                        + attempt + "/" + MAX_HEALTH_ATTEMPTS + ")");
        return Result.retry();
    }

    private boolean uploadLocalTasks(Context context) {
        TaskSyncPrefs.setStatus(context, "Copying local tasks to cloud...");
        NotesApi api = RetrofitClient.getClient().create(NotesApi.class);
        DatabaseHelper localDb = new DatabaseHelper(context);

        List<Task> localTasks;
        try {
            localTasks = localDb.getAllTasks();
        } catch (Exception e) {
            Log.e(TAG, "Failed to read local tasks", e);
            return false;
        }

        if (localTasks == null || localTasks.isEmpty()) {
            Log.d(TAG, "No local tasks to sync");
            return true;
        }

        Set<String> existingKeys;
        try {
            existingKeys = cloudTaskKeys(api);
        } catch (Exception e) {
            Log.e(TAG, "Failed to fetch cloud tasks", e);
            return false;
        }

        for (Task local : localTasks) {
            if (existingKeys.contains(taskKey(local))) {
                markLocalSynced(localDb, local);
                continue;
            }
            String originalStatus = local.getStatus();
            if (LOCAL_SYNCED_STATUS.equalsIgnoreCase(originalStatus)) {
                originalStatus = "Pending";
            }
            Task payload = new Task(
                    0,
                    local.getName(),
                    local.getText(),
                    local.getTitle(),
                    originalStatus,
                    local.getRemarks(),
                    local.getDateTime());
            try {
                Response<Task> response = api.addTask(payload).execute();
                if (!response.isSuccessful() || response.body() == null) {
                    Log.e(TAG, "Failed to upload task " + local.getName()
                            + " code=" + response.code());
                    return false;
                }
                existingKeys.add(taskKey(response.body()));
                existingKeys.add(taskKey(local));
                markLocalSynced(localDb, local);
            } catch (Exception e) {
                Log.e(TAG, "Exception uploading task " + local.getName(), e);
                return false;
            }
        }
        return true;
    }

    private Set<String> cloudTaskKeys(NotesApi api) throws Exception {
        Set<String> keys = new HashSet<>();
        Response<List<Task>> response = api.getTasks().execute();
        if (!response.isSuccessful() || response.body() == null) {
            throw new IllegalStateException("Cloud task list failed: " + response.code());
        }
        for (Task task : response.body()) {
            keys.add(taskKey(task));
        }
        return keys;
    }

    private void markLocalSynced(DatabaseHelper localDb, Task local) {
        if (LOCAL_SYNCED_STATUS.equalsIgnoreCase(safe(local.getStatus()))) {
            return;
        }
        try {
            localDb.updateTaskStatus(local.getId(), LOCAL_SYNCED_STATUS);
            local.setStatus(LOCAL_SYNCED_STATUS);
        } catch (Exception e) {
            Log.e(TAG, "Failed to mark local task synced id=" + local.getId(), e);
        }
    }

    private static String taskKey(Task task) {
        return safe(task.getName()) + "|" + safe(task.getText()) + "|"
                + safe(task.getTitle()) + "|" + safe(task.getDateTime());
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isApiHealthy() {
        try {
            Response<Void> response = RetrofitClient.getClient()
                    .create(NotesApi.class)
                    .getHealth()
                    .execute();
            return response.isSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Health check failed", e);
            return false;
        }
    }

    private void markSuccess(Context context) {
        TaskSyncPrefs.setSyncActive(context, false);
        TaskSyncPrefs.setStatus(context, "Tasks synced successfully.");
        Log.d(TAG, "Task sync completed");
    }
}
