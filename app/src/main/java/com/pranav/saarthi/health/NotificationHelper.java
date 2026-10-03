package com.pranav.saarthi.health;


import com.pranav.saarthi.R;
import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

public class NotificationHelper {

    private static final String TAG = "NotificationHelper";

    private static final String CHANNEL_ID = "health_check_channel";
    private static final String CHANNEL_NAME = "Health Check";
    private static final String CHANNEL_DESCRIPTION =
            "Notifications related to Saarthi API health checks";

    private static final int NOTIFICATION_ID = 1001;

    private NotificationHelper() {
        // Prevent creating instances
    }

    /**
     * Creates the notification channel.
     * Safe to call multiple times.
     */
    public static void createNotificationChannel(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            channel.setDescription(CHANNEL_DESCRIPTION);

            NotificationManager notificationManager =
                    context.getSystemService(NotificationManager.class);

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    public static boolean canPostNotifications(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return NotificationManagerCompat.from(context).areNotificationsEnabled();
    }

    /**
     * Sends the "Checking API Health" notification.
     */
    public static void showHealthCheckStarted(Context context) {
        if (!canPostNotifications(context)) {
            Log.w(TAG, "Skipping health check notification: permission disabled or denied");
            return;
        }

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(android.R.drawable.ic_popup_sync)
                        .setContentTitle("Checking API Health")
                        .setContentText("Checking api health.")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true);

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build());
        } catch (SecurityException e) {
            Log.e(TAG, "Failed to post health check notification", e);
        }
    }
}