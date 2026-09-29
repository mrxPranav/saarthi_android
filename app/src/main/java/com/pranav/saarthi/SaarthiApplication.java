package com.pranav.saarthi;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

public class SaarthiApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        SharedPreferences prefs =
                getSharedPreferences("ThemePrefs", MODE_PRIVATE);

        int themeMode = prefs.getInt(
                "theme_mode",
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        );

        AppCompatDelegate.setDefaultNightMode(themeMode);

        HealthCheckScheduler.schedule(this);
    }
}