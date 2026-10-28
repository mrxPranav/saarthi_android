package com.pranav.saarthi.ui;



import com.pranav.saarthi.R;
import com.pranav.saarthi.api.NotesApi;
import com.pranav.saarthi.api.RetrofitClient;
import com.pranav.saarthi.notifications.NotificationHistoryActivity;
import com.pranav.saarthi.notifications.NotificationTableActivity;
import com.pranav.saarthi.sync.TaskSyncPrefs;
import com.pranav.saarthi.sync.TaskSyncScheduler;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import androidx.core.view.GravityCompat;

public class LandingActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private SwitchMaterial switchSyncTasks;
    private TextView tvSyncStatus;
    private boolean suppressSyncToggleCallback;

    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (!granted) {
                    Toast.makeText(this, R.string.notification_permission_denied, Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

        requestNotificationPermissionIfNeeded();

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_saved_notes) {
                startActivity(new Intent(LandingActivity.this, SavedNotesActivity.class));
            } else if (id == R.id.nav_backup_restore) {
                startActivity(new Intent(LandingActivity.this, BackupActivity.class));
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(LandingActivity.this, SettingsActivity.class));
            } else if (id == R.id.nav_ai_update) {
                startActivity(new Intent(LandingActivity.this, UpdateTitleActivity.class));
            } else if (id == R.id.nav_health_stats) {
                startActivity(new Intent(LandingActivity.this, HealthCheckActivity.class));
            } else if (id == R.id.nav_notifications) {
                startActivity(new Intent(LandingActivity.this, NotificationHistoryActivity.class));
            } else if (id == R.id.nav_notification_table) {
                startActivity(new Intent(LandingActivity.this, NotificationTableActivity.class));
            } else if (id == R.id.nav_hardware) {
                startActivity(new Intent(LandingActivity.this, HardwareActivity.class));
            } else if (id == R.id.nav_bot) {
                startActivity(new Intent(LandingActivity.this, BotActivity.class));
            }
            drawerLayout.closeDrawer(GravityCompat.END);
            return true;
        });

        MaterialCardView cardCreateNote = findViewById(R.id.cardCreateNote);
        MaterialCardView cardViewNotes = findViewById(R.id.cardViewNotes);

        cardCreateNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LandingActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        cardViewNotes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LandingActivity.this, SavedNotesActivity.class);
                startActivity(intent);
            }
        });

        MaterialCardView cardAddTask = findViewById(R.id.cardAddTask);
        MaterialCardView cardViewTasks = findViewById(R.id.cardViewTasks);

        cardAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LandingActivity.this, AddTaskActivity.class);
                startActivity(intent);
            }
        });

        cardViewTasks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LandingActivity.this, TasksListActivity.class);
                startActivity(intent);
            }
        });

        setupTaskSyncToggle();
        checkApiStatus();

        findViewById(R.id.tvApiStatus).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkApiStatus();
            }
        });
    }

    private void setupTaskSyncToggle() {
        switchSyncTasks = findViewById(R.id.switchSyncTasks);
        tvSyncStatus = findViewById(R.id.tvSyncStatus);

        suppressSyncToggleCallback = true;
        switchSyncTasks.setChecked(TaskSyncPrefs.isSyncActive(this));
        suppressSyncToggleCallback = false;
        tvSyncStatus.setText(TaskSyncPrefs.getStatus(this));

        switchSyncTasks.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (suppressSyncToggleCallback) {
                return;
            }
            if (isChecked) {
                Toast.makeText(this, R.string.sync_tasks_started, Toast.LENGTH_SHORT).show();
                TaskSyncScheduler.start(this);
            } else {
                TaskSyncScheduler.cancel(this);
                tvSyncStatus.setText(R.string.sync_tasks_cancelled);
                Toast.makeText(this, R.string.sync_tasks_cancelled, Toast.LENGTH_SHORT).show();
            }
        });

        WorkManager.getInstance(this)
                .getWorkInfosForUniqueWorkLiveData(TaskSyncScheduler.UNIQUE_WORK_NAME)
                .observe(this, workInfos -> {
                    tvSyncStatus.setText(TaskSyncPrefs.getStatus(this));
                    boolean shouldBeOn = TaskSyncPrefs.isSyncActive(this);
                    if (workInfos != null && !workInfos.isEmpty()) {
                        WorkInfo info = workInfos.get(0);
                        if (info.getState() == WorkInfo.State.SUCCEEDED) {
                            shouldBeOn = false;
                        }
                    }
                    if (switchSyncTasks.isChecked() != shouldBeOn) {
                        suppressSyncToggleCallback = true;
                        switchSyncTasks.setChecked(shouldBeOn);
                        suppressSyncToggleCallback = false;
                    }
                });
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
    }

    private void checkApiStatus() {
        android.widget.TextView tvStatus = findViewById(R.id.tvApiStatus);
        tvStatus.setText("API Status: Checking...");
        tvStatus.setTextColor(android.graphics.Color.GRAY);

        RetrofitClient.getClient().create(NotesApi.class).getHealth().enqueue(new retrofit2.Callback<Void>() {
            @Override
            public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                if (response.isSuccessful()) {
                    tvStatus.setText("API Status: Online");
                    tvStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50")); // Green is usually okay, but
                                                                                         // let's be safe
                } else {
                    tvStatus.setText("API Status: Error (" + response.code() + ")");
                    tvStatus.setTextColor(android.graphics.Color.RED);
                }
            }

            @Override
            public void onFailure(retrofit2.Call<Void> call, Throwable t) {
                tvStatus.setText("API Status: Offline");
                tvStatus.setTextColor(android.graphics.Color.RED);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@androidx.annotation.NonNull android.view.MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_drawer) {
            if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                drawerLayout.closeDrawer(GravityCompat.END);
            } else {
                drawerLayout.openDrawer(GravityCompat.END);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END);
        } else {
            super.onBackPressed();
        }
    }
}
