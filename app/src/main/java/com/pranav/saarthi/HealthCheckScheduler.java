package com.pranav.saarthi;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class HealthCheckScheduler {

    private static final String MORNING_WORK = "HealthCheckMorning";
    private static final String EVENING_WORK = "HealthCheckEvening";

    public static void schedule(Context context) {
        scheduleNext(context, 10, MORNING_WORK);
        scheduleNext(context, 22, EVENING_WORK);
    }

    private static void scheduleNext(
            Context context,
            int hour,
            String workName) {

        long delay = getDelayUntilNextOccurrence(hour);

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest request =
                new OneTimeWorkRequest.Builder(HealthCheckWorker.class)
                        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                        .setConstraints(constraints)
                        .build();

        WorkManager.getInstance(context)
                .enqueueUniqueWork(
                        workName,
                        ExistingWorkPolicy.KEEP,
                        request
                );
    }

    private static long getDelayUntilNextOccurrence(int hour) {

        Calendar now = Calendar.getInstance();
        Calendar next = Calendar.getInstance();

        next.set(Calendar.HOUR_OF_DAY, hour);
        next.set(Calendar.MINUTE, 0);
        next.set(Calendar.SECOND, 0);
        next.set(Calendar.MILLISECOND, 0);

        if (!next.after(now)) {
            next.add(Calendar.DAY_OF_YEAR, 1);
        }

        return next.getTimeInMillis() - now.getTimeInMillis();
    }
}