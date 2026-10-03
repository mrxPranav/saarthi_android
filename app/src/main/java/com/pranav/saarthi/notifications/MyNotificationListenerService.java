package com.pranav.saarthi.notifications;

import android.app.Notification;
import android.app.RemoteInput;
import android.content.Intent;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.pranav.saarthi.data.DatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

public class MyNotificationListenerService extends NotificationListenerService {

    public static final String ACTION_NOTIFICATION_CAPTURED =
            "com.pranav.saarthi.NOTIFICATION_LISTENER_EXAMPLE";

    private static final String TAG = "NotifListener";
    public static final ConcurrentHashMap<String, Notification.Action> replyActions =
            new ConcurrentHashMap<>();

    private DatabaseHelper dbHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        dbHelper = new DatabaseHelper(this);
    }

    private Notification.Action getReplyAction(Notification notification) {
        if (notification.actions == null) {
            return null;
        }
        for (Notification.Action action : notification.actions) {
            RemoteInput[] remoteInputs = action.getRemoteInputs();
            if (remoteInputs != null && remoteInputs.length > 0) {
                return action;
            }
        }
        return null;
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        String packageName = sbn.getPackageName();
        Notification notification = sbn.getNotification();
        Bundle extras = notification.extras;

        String title = extras.getString(Notification.EXTRA_TITLE);
        CharSequence textChar = extras.getCharSequence(Notification.EXTRA_TEXT);
        String text = textChar != null ? textChar.toString() : "";

        CharSequence bigTextChar = extras.getCharSequence(Notification.EXTRA_BIG_TEXT);
        String bigText = bigTextChar != null ? bigTextChar.toString() : "";

        String postTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date(sbn.getPostTime()));
        Notification.Action replyAction = getReplyAction(notification);
        boolean reply = replyAction != null;
        String notificationKey = sbn.getKey();

        if (reply) {
            replyActions.put(notificationKey, replyAction);
        }

        NotificationModel model = new NotificationModel(
                packageName, title, text, bigText, postTime, reply, notificationKey);
        try {
            dbHelper.addNotification(model);
        } catch (Exception e) {
            Log.e(TAG, "Error saving notification: " + e.getMessage());
        }

        Log.d(TAG, "Notification captured from: " + packageName);

        Intent intent = new Intent(ACTION_NOTIFICATION_CAPTURED);
        intent.putExtra("package", packageName);
        intent.putExtra("title", title);
        intent.putExtra("text", text);
        sendBroadcast(intent);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        replyActions.remove(sbn.getKey());
    }

    public static void replyToNotification(
            android.content.Context context, String notificationKey, String message) {
        Notification.Action action = replyActions.get(notificationKey);
        if (action == null) {
            Log.e(TAG, "No reply action found for key: " + notificationKey);
            return;
        }

        RemoteInput[] remoteInputs = action.getRemoteInputs();
        if (remoteInputs == null || remoteInputs.length == 0) {
            return;
        }

        RemoteInput remoteInput = remoteInputs[0];
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putCharSequence(remoteInput.getResultKey(), message);
        RemoteInput.addResultsToIntent(remoteInputs, intent, bundle);

        try {
            action.actionIntent.send(context, 0, intent);
            Log.d(TAG, "Reply sent successfully for key: " + notificationKey);
        } catch (android.app.PendingIntent.CanceledException e) {
            Log.e(TAG, "Error sending reply: " + e.getMessage());
        }
    }
}
