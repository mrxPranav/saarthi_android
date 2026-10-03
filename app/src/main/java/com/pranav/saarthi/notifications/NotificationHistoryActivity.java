package com.pranav.saarthi.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pranav.saarthi.R;
import com.pranav.saarthi.data.DatabaseHelper;

import java.util.List;

public class NotificationHistoryActivity extends AppCompatActivity
        implements NotificationAdapter.OnNotificationClickListener {

    private RecyclerView rvNotifications;
    private NotificationAdapter adapter;
    private DatabaseHelper dbHelper;
    private BroadcastReceiver notificationReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_history);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Notification History");
        }

        dbHelper = new DatabaseHelper(this);
        rvNotifications = findViewById(R.id.rvNotifications);
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));

        try {
            loadNotifications();
        } catch (Exception e) {
            Toast.makeText(this, "Could not load notifications: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        if (!isNotificationServiceEnabled()) {
            showPermissionDialog();
        }

        notificationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                loadNotifications();
            }
        };
        IntentFilter filter = new IntentFilter(MyNotificationListenerService.ACTION_NOTIFICATION_CAPTURED);
        ContextCompat.registerReceiver(
                this, notificationReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    private void loadNotifications() {
        List<NotificationModel> notifications = dbHelper.getAllNotifications();
        if (adapter == null) {
            adapter = new NotificationAdapter(notifications, this);
            rvNotifications.setAdapter(adapter);
        } else {
            adapter.updateList(notifications);
        }
    }

    private boolean isNotificationServiceEnabled() {
        String pkgName = getPackageName();
        final String flat = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
        if (flat != null && !flat.isEmpty()) {
            final String[] names = flat.split(":");
            for (String name : names) {
                if (name.contains(pkgName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void showPermissionDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Permission Required")
                .setMessage("Please enable Notification Access for Saarthi to capture notifications.")
                .setPositiveButton("Settings", (dialog, which) ->
                        startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")))
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (notificationReceiver != null) {
            unregisterReceiver(notificationReceiver);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public void onNotificationClick(NotificationModel notification) {
        final EditText etReply = new EditText(this);
        etReply.setHint("Type your reply...");

        new AlertDialog.Builder(this)
                .setTitle("Reply to " + notification.getTitle())
                .setView(etReply)
                .setPositiveButton("Send", (dialog, which) -> {
                    String message = etReply.getText().toString().trim();
                    if (!message.isEmpty()) {
                        MyNotificationListenerService.replyToNotification(
                                this, notification.getNotificationKey(), message);
                        Toast.makeText(this, "Reply sent", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
