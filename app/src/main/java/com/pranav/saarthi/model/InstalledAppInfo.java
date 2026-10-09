package com.pranav.saarthi.model;

import android.graphics.drawable.Drawable;

public class InstalledAppInfo {
    private final String packageName;
    private final String label;
    private final Drawable icon;
    private final boolean recentlyActive;
    private final long lastTimeUsed;

    public InstalledAppInfo(String packageName, String label, Drawable icon,
                            boolean recentlyActive, long lastTimeUsed) {
        this.packageName = packageName;
        this.label = label;
        this.icon = icon;
        this.recentlyActive = recentlyActive;
        this.lastTimeUsed = lastTimeUsed;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getLabel() {
        return label;
    }

    public Drawable getIcon() {
        return icon;
    }

    public boolean isRecentlyActive() {
        return recentlyActive;
    }

    public long getLastTimeUsed() {
        return lastTimeUsed;
    }
}
