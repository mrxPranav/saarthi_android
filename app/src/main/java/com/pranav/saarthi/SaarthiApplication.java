package com.pranav.saarthi;

import android.app.Application;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

public class SaarthiApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        SharedPreferences prefs = getSharedPreferences("ThemePrefs", MODE_PRIVATE);
        int themeMode = prefs.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(themeMode);

        scheduleHealthCheck();
    }

    private void scheduleHealthCheck() {
        long next10AM = getNextOccurrence(10);
        long next10PM = getNextOccurrence(22);

        long nextExecution = Math.min(next10AM, next10PM);
        long initialDelay = nextExecution - System.currentTimeMillis();

        androidx.work.PeriodicWorkRequest healthCheckRequest = new androidx.work.PeriodicWorkRequest.Builder(
                HealthCheckWorker.class, 12, java.util.concurrent.TimeUnit.HOURS)
                .setInitialDelay(initialDelay, java.util.concurrent.TimeUnit.MILLISECONDS)
                .build();

        androidx.work.WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "HealthCheckWork",
                androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                healthCheckRequest);
    }

    private long getNextOccurrence(int hour) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.HOUR_OF_DAY, hour);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);

        if (cal.getTimeInMillis() <= System.currentTimeMillis()) {
            cal.add(java.util.Calendar.DAY_OF_YEAR, 1);
        }
        return cal.getTimeInMillis();
    }
}
