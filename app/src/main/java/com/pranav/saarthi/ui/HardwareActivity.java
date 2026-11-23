package com.pranav.saarthi.ui;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pranav.saarthi.R;
import com.pranav.saarthi.hardware.HardwareInventory;
import com.pranav.saarthi.model.HardwareItem;
import com.pranav.saarthi.ui.adapter.HardwareAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HardwareActivity extends AppCompatActivity implements SensorEventListener {

    private HardwareAdapter adapter;
    private TextView tvCount;
    private SensorManager sensorManager;
    private CameraManager cameraManager;
    private CameraManager.AvailabilityCallback cameraCallback;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Map<String, Long> lastSensorUiUpdate = new HashMap<>();
    private static final long SENSOR_UI_INTERVAL_MS = 400L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hardware);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.hardware_title);
        }

        tvCount = findViewById(R.id.tvHardwareCount);
        Button btnRefresh = findViewById(R.id.btnRefreshHardware);
        RecyclerView recyclerView = findViewById(R.id.rvHardware);
        adapter = new HardwareAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);

        btnRefresh.setOnClickListener(v -> reload());
        reload();
    }

    private void reload() {
        unregisterSensors();
        unregisterCameras();
        List<HardwareItem> items = HardwareInventory.load(this);
        adapter.setItems(items);
        tvCount.setText(getString(R.string.hardware_count, items.size()));
        registerSensors();
        registerCameras();
    }

    private void registerSensors() {
        if (sensorManager == null) {
            return;
        }
        for (Sensor sensor : sensorManager.getSensorList(Sensor.TYPE_ALL)) {
            try {
                if (sensor.getMinDelay() < 0) {
                    continue;
                }
                sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
            } catch (Exception ignored) {
            }
        }
    }

    private void unregisterSensors() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    private void registerCameras() {
        if (cameraManager == null) {
            return;
        }
        cameraCallback = new CameraManager.AvailabilityCallback() {
            @Override
            public void onCameraAvailable(@NonNull String cameraId) {
                mainHandler.post(() -> adapter.updateStatus(
                        "camera_" + cameraId, "Available", true));
            }

            @Override
            public void onCameraUnavailable(@NonNull String cameraId) {
                mainHandler.post(() -> adapter.updateStatus(
                        "camera_" + cameraId, "In use / unavailable", false));
            }
        };
        cameraManager.registerAvailabilityCallback(cameraCallback, mainHandler);
    }

    private void unregisterCameras() {
        if (cameraManager != null && cameraCallback != null) {
            cameraManager.unregisterAvailabilityCallback(cameraCallback);
            cameraCallback = null;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        String id = HardwareInventory.sensorId(event.sensor);
        long now = System.currentTimeMillis();
        Long last = lastSensorUiUpdate.get(id);
        if (last != null && now - last < SENSOR_UI_INTERVAL_MS) {
            return;
        }
        lastSensorUiUpdate.put(id, now);
        String reading = HardwareInventory.formatSensorReading(event.sensor, event.values);
        adapter.updateStatus(id, reading, true);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterSensors();
        unregisterCameras();
    }

    @Override
    protected void onResume() {
        super.onResume();
        reload();
    }

    @Override
    protected void onDestroy() {
        unregisterSensors();
        unregisterCameras();
        super.onDestroy();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
