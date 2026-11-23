package com.pranav.saarthi.sync;

import android.content.Context;

import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public final class TaskSyncScheduler {

    public static final String UNIQUE_WORK_NAME = "saarthi_task_sync";

    private TaskSyncScheduler() {
    }

    public static void start(Context context) {
        TaskSyncPrefs.setSyncActive(context, true);
        TaskSyncPrefs.setStatus(context, "Starting task sync...");

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(TaskSyncWorker.class)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.LINEAR, 2, TimeUnit.MINUTES)
                .build();

        WorkManager.getInstance(context).enqueueUniqueWork(
                UNIQUE_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                request);
    }

    public static void cancel(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME);
        TaskSyncPrefs.setSyncActive(context, false);
        TaskSyncPrefs.setStatus(context, "");
    }
}
