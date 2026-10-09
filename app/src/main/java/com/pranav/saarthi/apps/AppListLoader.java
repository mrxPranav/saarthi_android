package com.pranav.saarthi.apps;

import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Process;
import android.provider.Settings;
import android.util.Log;

import com.pranav.saarthi.model.InstalledAppInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class AppListLoader {

    private static final String TAG = "AppListLoader";

    /** Apps used in the foreground within this window are marked active. */
    private static final long RECENT_ACTIVE_WINDOW_MS = 5 * 60 * 1000L;

    private AppListLoader() {
    }

    public static boolean hasUsageAccess(Context context) {
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        if (appOps == null) {
            return false;
        }
        int mode = appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.getPackageName());
        if (mode == AppOpsManager.MODE_ALLOWED) {
            return true;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return mode == AppOpsManager.MODE_FOREGROUND;
        }
        return false;
    }

    public static Intent usageAccessSettingsIntent() {
        return new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
    }

    public static List<InstalledAppInfo> load(Context context) {
        PackageManager pm = context.getPackageManager();
        Map<String, ApplicationInfo> merged = collectAllApplicationInfo(context, pm);
        Map<String, Long> lastUsedByPackage = queryLastUsedTimes(context);

        List<InstalledAppInfo> apps = new ArrayList<>();
        long now = System.currentTimeMillis();

        for (ApplicationInfo info : merged.values()) {
            CharSequence labelCs = info.loadLabel(pm);
            String label = labelCs != null ? labelCs.toString().trim() : "";
            if (label.isEmpty()) {
                label = info.packageName;
            }

            long lastUsed = lastUsedByPackage.getOrDefault(info.packageName, 0L);
            boolean active = lastUsed > 0 && (now - lastUsed) <= RECENT_ACTIVE_WINDOW_MS;

            apps.add(new InstalledAppInfo(
                    info.packageName,
                    label,
                    info.loadIcon(pm),
                    active,
                    lastUsed));
        }

        Collections.sort(apps, new Comparator<InstalledAppInfo>() {
            @Override
            public int compare(InstalledAppInfo a, InstalledAppInfo b) {
                if (a.isRecentlyActive() != b.isRecentlyActive()) {
                    return a.isRecentlyActive() ? -1 : 1;
                }
                if (a.isRecentlyActive() && b.isRecentlyActive()) {
                    return Long.compare(b.getLastTimeUsed(), a.getLastTimeUsed());
                }
                return a.getLabel().compareToIgnoreCase(b.getLabel());
            }
        });

        return apps;
    }

    /**
     * Merges unique packages from installed applications, installed packages,
     * launcher resolve, and usage-stats package names.
     */
    private static Map<String, ApplicationInfo> collectAllApplicationInfo(
            Context context, PackageManager pm) {
        Map<String, ApplicationInfo> byPackage = new HashMap<>();
        Set<String> extraPackageNames = new HashSet<>();

        mergeApplications(byPackage, queryInstalledApplications(pm));
        mergeFromPackages(byPackage, queryInstalledPackages(pm), pm);

        Set<String> launchable = getLaunchablePackages(pm);
        extraPackageNames.addAll(launchable);
        extraPackageNames.addAll(queryUsagePackageNames(context));

        for (String packageName : extraPackageNames) {
            if (byPackage.containsKey(packageName)) {
                continue;
            }
            ApplicationInfo info = getApplicationInfoSafe(pm, packageName);
            if (info != null) {
                byPackage.put(packageName, info);
            }
        }

        Log.d(TAG, "Merged unique packages: " + byPackage.size());
        return byPackage;
    }

    private static void mergeApplications(
            Map<String, ApplicationInfo> target, List<ApplicationInfo> source) {
        for (ApplicationInfo info : source) {
            if (info != null && info.packageName != null) {
                target.put(info.packageName, info);
            }
        }
    }

    private static void mergeFromPackages(
            Map<String, ApplicationInfo> target, List<PackageInfo> packages, PackageManager pm) {
        for (PackageInfo pkg : packages) {
            if (pkg == null || pkg.packageName == null) {
                continue;
            }
            if (pkg.applicationInfo != null) {
                target.put(pkg.packageName, pkg.applicationInfo);
            } else {
                ApplicationInfo info = getApplicationInfoSafe(pm, pkg.packageName);
                if (info != null) {
                    target.put(pkg.packageName, info);
                }
            }
        }
    }

    private static ApplicationInfo getApplicationInfoSafe(PackageManager pm, String packageName) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                return pm.getApplicationInfo(
                        packageName,
                        PackageManager.ApplicationInfoFlags.of(packageInfoMatchFlags()));
            }
            return pm.getApplicationInfo(packageName, packageInfoMatchFlags());
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    private static List<ApplicationInfo> queryInstalledApplications(PackageManager pm) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return pm.getInstalledApplications(
                    PackageManager.ApplicationInfoFlags.of(packageInfoMatchFlags()));
        }
        return pm.getInstalledApplications(packageInfoMatchFlags());
    }

    private static List<PackageInfo> queryInstalledPackages(PackageManager pm) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return pm.getInstalledPackages(
                    PackageManager.PackageInfoFlags.of(packageInfoMatchFlags()));
        }
        return pm.getInstalledPackages(packageInfoMatchFlags());
    }

    private static int packageInfoMatchFlags() {
        int flags = PackageManager.MATCH_DISABLED_COMPONENTS;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            flags |= PackageManager.MATCH_UNINSTALLED_PACKAGES;
        }
        return flags;
    }

    private static Set<String> getLaunchablePackages(PackageManager pm) {
        Intent main = new Intent(Intent.ACTION_MAIN, null);
        main.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> activities;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            activities = pm.queryIntentActivities(
                    main,
                    PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_ALL));
        } else {
            activities = pm.queryIntentActivities(main, PackageManager.MATCH_ALL);
        }
        Set<String> packages = new HashSet<>();
        for (ResolveInfo ri : activities) {
            if (ri.activityInfo != null) {
                packages.add(ri.activityInfo.packageName);
            }
        }
        return packages;
    }

    private static Set<String> queryUsagePackageNames(Context context) {
        Set<String> names = new HashSet<>();
        if (!hasUsageAccess(context)) {
            return names;
        }
        UsageStatsManager usageStatsManager =
                (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        if (usageStatsManager == null) {
            return names;
        }
        long end = System.currentTimeMillis();
        long start = end - 24 * 60 * 60 * 1000L;
        List<UsageStats> stats =
                usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, end);
        if (stats == null) {
            return names;
        }
        for (UsageStats stat : stats) {
            if (stat.getPackageName() != null) {
                names.add(stat.getPackageName());
            }
        }
        return names;
    }

    private static Map<String, Long> queryLastUsedTimes(Context context) {
        Map<String, Long> lastUsed = new HashMap<>();
        if (!hasUsageAccess(context)) {
            return lastUsed;
        }

        UsageStatsManager usageStatsManager =
                (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        if (usageStatsManager == null) {
            return lastUsed;
        }

        long end = System.currentTimeMillis();
        long start = end - 24 * 60 * 60 * 1000L;
        List<UsageStats> stats =
                usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, end);
        if (stats == null) {
            return lastUsed;
        }

        for (UsageStats stat : stats) {
            if (stat.getLastTimeUsed() > 0) {
                Long existing = lastUsed.get(stat.getPackageName());
                if (existing == null || stat.getLastTimeUsed() > existing) {
                    lastUsed.put(stat.getPackageName(), stat.getLastTimeUsed());
                }
            }
        }
        return lastUsed;
    }
}
