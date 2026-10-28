package com.pranav.saarthi.ui;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.pranav.saarthi.R;
import com.pranav.saarthi.bot.BotListeningService;

public class BotActivity extends AppCompatActivity {

    private Button btnListen;
    private TextView tvStatus;
    private TextView tvFile;

    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean mic = result.get(Manifest.permission.RECORD_AUDIO);
                if (mic != null && mic) {
                    startListening();
                } else {
                    Toast.makeText(this, R.string.bot_mic_permission_denied, Toast.LENGTH_LONG).show();
                }
            });

    private final BroadcastReceiver stateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            refreshUi();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bot);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.bot_title);
        }

        btnListen = findViewById(R.id.btnStartListening);
        tvStatus = findViewById(R.id.tvBotStatus);
        tvFile = findViewById(R.id.tvBotFile);

        btnListen.setOnClickListener(v -> {
            if (BotListeningService.isRunning()) {
                BotListeningService.stop(this);
            } else {
                requestPermissionsAndStart();
            }
        });

        refreshUi();
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(BotListeningService.ACTION_STATE);
        ContextCompat.registerReceiver(this, stateReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
        refreshUi();
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            unregisterReceiver(stateReceiver);
        } catch (Exception ignored) {
        }
    }

    private void requestPermissionsAndStart() {
        boolean micGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED;
        boolean notifGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
                || ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;

        if (micGranted && notifGranted) {
            startListening();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(new String[]{
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.POST_NOTIFICATIONS
            });
        } else {
            permissionLauncher.launch(new String[]{Manifest.permission.RECORD_AUDIO});
        }
    }

    private void startListening() {
        BotListeningService.start(this);
        Toast.makeText(this, R.string.bot_listening_started, Toast.LENGTH_SHORT).show();
        refreshUi();
    }

    private void refreshUi() {
        boolean listening = BotListeningService.isRunning();
        tvStatus.setText(listening ? R.string.bot_status_listening : R.string.bot_status_idle);
        tvStatus.setTextColor(listening ? 0xFF2E7D32 : 0xFF616161);
        btnListen.setText(listening ? R.string.bot_stop : R.string.bot_start_listening);
        String path = BotListeningService.getLastFilePath();
        if (path != null && !path.isEmpty()) {
            tvFile.setText(getString(R.string.bot_file, path));
        } else {
            tvFile.setText(R.string.bot_file_none);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
