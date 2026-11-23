package com.pranav.saarthi.sync;

import android.content.Context;
import android.content.SharedPreferences;

public final class TaskSyncPrefs {

    private static final String PREFS_NAME = "TaskSyncPrefs";
    private static final String KEY_SYNC_ACTIVE = "sync_active";
    private static final String KEY_STATUS = "sync_status";

    private TaskSyncPrefs() {
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static boolean isSyncActive(Context context) {
        return prefs(context).getBoolean(KEY_SYNC_ACTIVE, false);
    }

    public static void setSyncActive(Context context, boolean active) {
        prefs(context).edit().putBoolean(KEY_SYNC_ACTIVE, active).apply();
    }

    public static String getStatus(Context context) {
        return prefs(context).getString(KEY_STATUS, "");
    }

    public static void setStatus(Context context, String status) {
        prefs(context).edit().putString(KEY_STATUS, status).apply();
    }
}
