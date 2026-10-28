package com.pranav.saarthi.hardware;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.location.LocationManager;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.media.MicrophoneInfo;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.PowerManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.SizeF;

import com.pranav.saarthi.model.HardwareItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class HardwareInventory {

    private static final String TAG = "HardwareInventory";

    private HardwareInventory() {
    }

    public static List<HardwareItem> load(Context context) {
        List<HardwareItem> items = new ArrayList<>();
        addCameras(context, items);
        addMicrophones(context, items);
        addAudioOutputs(context, items);
        addSensors(context, items);
        addConnectivity(context, items);
        addPowerAndSystem(context, items);
        return items;
    }

    private static void addCameras(Context context, List<HardwareItem> items) {
        PackageManager pm = context.getPackageManager();
        boolean hasCamera = pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
                || pm.hasSystemFeature(PackageManager.FEATURE_CAMERA);
        CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        if (cameraManager == null) {
            items.add(new HardwareItem("camera_none", "Camera", "Camera",
                    "Camera service unavailable", hasCamera ? "Present (service missing)" : "Not present",
                    false));
            return;
        }
        try {
            String[] ids = cameraManager.getCameraIdList();
            if (ids.length == 0) {
                items.add(new HardwareItem("camera_none", "Camera", "Camera",
                        "No camera IDs reported", hasCamera ? "Feature declared, none enumerated" : "Not present",
                        false));
                return;
            }
            for (String cameraId : ids) {
                CameraCharacteristics chars = cameraManager.getCameraCharacteristics(cameraId);
                Integer facing = chars.get(CameraCharacteristics.LENS_FACING);
                String facingLabel = facingLabel(facing);
                android.util.Size pixelSize = chars.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE);
                SizeF physicalSize = chars.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE);
                Integer level = chars.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
                Boolean flash = chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                float[] focal = chars.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS);

                StringBuilder details = new StringBuilder();
                details.append("ID: ").append(cameraId);
                details.append("\nFacing: ").append(facingLabel);
                if (pixelSize != null) {
                    details.append("\nSensor pixels: ")
                            .append(pixelSize.getWidth()).append(" x ").append(pixelSize.getHeight());
                }
                if (physicalSize != null) {
                    details.append(String.format(Locale.US, "\nPhysical size: %.2f x %.2f mm",
                            physicalSize.getWidth(), physicalSize.getHeight()));
                }
                if (focal != null && focal.length > 0) {
                    details.append(String.format(Locale.US, "\nFocal length: %.2f mm", focal[0]));
                }
                details.append("\nHardware level: ").append(hardwareLevel(level));
                details.append("\nFlash: ").append(Boolean.TRUE.equals(flash) ? "Available" : "None");

                boolean torchOn = false;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Boolean.TRUE.equals(flash)) {
                    try {
                        torchOn = cameraManager.getCameraCharacteristics(cameraId)
                                .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) != null
                                && isTorchOn(cameraManager, cameraId);
                    } catch (Exception ignored) {
                    }
                }

                String status = "Available";
                if (torchOn) {
                    status = "Available (torch on)";
                }
                items.add(new HardwareItem(
                        "camera_" + cameraId,
                        "Camera",
                        facingLabel + " camera",
                        details.toString(),
                        status,
                        true));
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to list cameras", e);
            items.add(new HardwareItem("camera_error", "Camera", "Camera",
                    "Error: " + e.getMessage(), "Error reading cameras", false));
        }
    }

    private static boolean isTorchOn(CameraManager cameraManager, String cameraId) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return false;
        }
        try {
            // Camera2 has no public torch-on query for all APIs; treat as unknown unless exception.
            cameraManager.getCameraIdList();
        } catch (Exception ignored) {
        }
        return false;
    }

    private static String facingLabel(Integer facing) {
        if (facing == null) {
            return "Unknown";
        }
        if (facing == CameraCharacteristics.LENS_FACING_FRONT) {
            return "Front";
        }
        if (facing == CameraCharacteristics.LENS_FACING_BACK) {
            return "Rear";
        }
        if (facing == CameraCharacteristics.LENS_FACING_EXTERNAL) {
            return "External";
        }
        return "Unknown";
    }

    private static String hardwareLevel(Integer level) {
        if (level == null) {
            return "Unknown";
        }
        switch (level) {
            case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY:
                return "Legacy";
            case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED:
                return "Limited";
            case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL:
                return "Full";
            case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3:
                return "Level 3";
            case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_EXTERNAL:
                return "External";
            default:
                return String.valueOf(level);
        }
    }

    private static void addMicrophones(Context context, List<HardwareItem> items) {
        PackageManager pm = context.getPackageManager();
        boolean hasMic = pm.hasSystemFeature(PackageManager.FEATURE_MICROPHONE);
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        boolean muted = audioManager != null && audioManager.isMicrophoneMute();
        String mode = audioMode(audioManager);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && audioManager != null) {
            try {
                List<MicrophoneInfo> mics = audioManager.getMicrophones();
                if (mics != null && !mics.isEmpty()) {
                    int index = 0;
                    for (MicrophoneInfo mic : mics) {
                        StringBuilder details = new StringBuilder();
                        details.append("Description: ").append(safe(mic.getDescription()));
                        details.append("\nType: ").append(mic.getType());
                        details.append("\nAddress: ").append(safe(mic.getAddress()));
                        details.append("\nLocation: ").append(micLocation(mic.getLocation()));
                        details.append("\nDirectionality: ").append(mic.getDirectionality());
                        details.append(String.format(Locale.US, "\nSensitivity: %.1f dBFS",
                                mic.getSensitivity()));
                        details.append(String.format(Locale.US, "\nMax SPL: %.1f dB",
                                mic.getMaxSpl()));
                        details.append("\nAudio mode: ").append(mode);
                        items.add(new HardwareItem(
                                "mic_" + index + "_" + mic.getDescription(),
                                "Microphone",
                                micName(mic, index),
                                details.toString(),
                                muted ? "Muted" : "Ready",
                                !muted));
                        index++;
                    }
                    return;
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to list microphones", e);
            }
        }

        if (audioManager != null) {
            AudioDeviceInfo[] inputs = audioManager.getDevices(AudioManager.GET_DEVICES_INPUTS);
            boolean foundMic = false;
            int index = 0;
            for (AudioDeviceInfo device : inputs) {
                if (device.getType() == AudioDeviceInfo.TYPE_BUILTIN_MIC
                        || device.getType() == AudioDeviceInfo.TYPE_WIRED_HEADSET
                        || device.getType() == AudioDeviceInfo.TYPE_USB_DEVICE
                        || device.getType() == AudioDeviceInfo.TYPE_USB_HEADSET
                        || device.getType() == AudioDeviceInfo.TYPE_BLUETOOTH_SCO
                        || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                        && device.getType() == AudioDeviceInfo.TYPE_BLE_HEADSET)) {
                    foundMic = true;
                    String details = "Product: " + safe(device.getProductName().toString())
                            + "\nType: " + audioDeviceType(device.getType())
                            + "\nID: " + device.getId()
                            + "\nAudio mode: " + mode;
                    items.add(new HardwareItem(
                            "mic_dev_" + device.getId(),
                            "Microphone",
                            micLabel(device, index++),
                            details,
                            muted ? "Muted" : "Ready",
                            !muted));
                }
            }
            if (foundMic) {
                return;
            }
        }

        items.add(new HardwareItem("mic_generic", "Microphone", "Microphone",
                "System feature: " + (hasMic ? "yes" : "no") + "\nAudio mode: " + mode,
                hasMic ? (muted ? "Muted" : "Ready") : "Not present",
                hasMic && !muted));
    }

    private static String micName(MicrophoneInfo mic, int index) {
        String desc = mic.getDescription();
        if (desc != null && !desc.trim().isEmpty()) {
            return desc;
        }
        return "Microphone " + (index + 1);
    }

    private static String micLabel(AudioDeviceInfo device, int index) {
        CharSequence product = device.getProductName();
        if (product != null && product.length() > 0) {
            return product.toString();
        }
        return "Microphone " + (index + 1);
    }

    private static String micLocation(int location) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            return "Unknown";
        }
        switch (location) {
            case MicrophoneInfo.LOCATION_MAINBODY:
                return "Main body";
            case MicrophoneInfo.LOCATION_MAINBODY_MOVABLE:
                return "Movable";
            case MicrophoneInfo.LOCATION_PERIPHERAL:
                return "Peripheral";
            default:
                return "Unknown";
        }
    }

    private static String audioMode(AudioManager audioManager) {
        if (audioManager == null) {
            return "Unknown";
        }
        switch (audioManager.getMode()) {
            case AudioManager.MODE_NORMAL:
                return "Normal";
            case AudioManager.MODE_RINGTONE:
                return "Ringtone";
            case AudioManager.MODE_IN_CALL:
                return "In call";
            case AudioManager.MODE_IN_COMMUNICATION:
                return "Communication";
            default:
                return "Mode " + audioManager.getMode();
        }
    }

    private static String audioDeviceType(int type) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                && type == AudioDeviceInfo.TYPE_BLE_HEADSET) {
            return "BLE headset";
        }
        switch (type) {
            case AudioDeviceInfo.TYPE_BUILTIN_MIC:
                return "Built-in mic";
            case AudioDeviceInfo.TYPE_WIRED_HEADSET:
                return "Wired headset";
            case AudioDeviceInfo.TYPE_USB_DEVICE:
                return "USB";
            case AudioDeviceInfo.TYPE_USB_HEADSET:
                return "USB headset";
            case AudioDeviceInfo.TYPE_BLUETOOTH_SCO:
                return "Bluetooth SCO";
            case AudioDeviceInfo.TYPE_BUILTIN_SPEAKER:
                return "Built-in speaker";
            case AudioDeviceInfo.TYPE_BUILTIN_EARPIECE:
                return "Earpiece";
            case AudioDeviceInfo.TYPE_WIRED_HEADPHONES:
                return "Headphones";
            case AudioDeviceInfo.TYPE_BLUETOOTH_A2DP:
                return "Bluetooth A2DP";
            default:
                return "Type " + type;
        }
    }

    private static void addAudioOutputs(Context context, List<HardwareItem> items) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager == null) {
            return;
        }
        boolean speakerOn = audioManager.isSpeakerphoneOn();
        boolean musicActive = audioManager.isMusicActive();
        AudioDeviceInfo[] outputs = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS);
        for (AudioDeviceInfo device : outputs) {
            if (device.getType() != AudioDeviceInfo.TYPE_BUILTIN_SPEAKER
                    && device.getType() != AudioDeviceInfo.TYPE_BUILTIN_EARPIECE
                    && device.getType() != AudioDeviceInfo.TYPE_WIRED_HEADPHONES
                    && device.getType() != AudioDeviceInfo.TYPE_WIRED_HEADSET
                    && device.getType() != AudioDeviceInfo.TYPE_BLUETOOTH_A2DP
                    && device.getType() != AudioDeviceInfo.TYPE_USB_HEADSET) {
                continue;
            }
            String details = "Product: " + safe(String.valueOf(device.getProductName()))
                    + "\nType: " + audioDeviceType(device.getType())
                    + "\nID: " + device.getId()
                    + "\nSpeakerphone: " + (speakerOn ? "on" : "off")
                    + "\nMusic active: " + (musicActive ? "yes" : "no");
            items.add(new HardwareItem(
                    "audio_out_" + device.getId(),
                    "Audio",
                    audioDeviceType(device.getType()),
                    details,
                    musicActive ? "In use" : "Idle",
                    true));
        }
    }

    private static void addSensors(Context context, List<HardwareItem> items) {
        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager == null) {
            items.add(new HardwareItem("sensor_none", "Sensor", "Sensors",
                    "Sensor service unavailable", "Not available", false));
            return;
        }
        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        if (sensors == null || sensors.isEmpty()) {
            items.add(new HardwareItem("sensor_none", "Sensor", "Sensors",
                    "No sensors reported", "None", false));
            return;
        }
        for (Sensor sensor : sensors) {
            String details = "Type: " + sensorTypeName(sensor)
                    + "\nVendor: " + safe(sensor.getVendor())
                    + "\nVersion: " + sensor.getVersion()
                    + String.format(Locale.US, "\nPower: %.2f mA", sensor.getPower())
                    + String.format(Locale.US, "\nResolution: %s", String.valueOf(sensor.getResolution()))
                    + String.format(Locale.US, "\nMax range: %s", String.valueOf(sensor.getMaximumRange()))
                    + "\nMin delay: " + sensor.getMinDelay() + " µs";
            items.add(new HardwareItem(
                    sensorId(sensor),
                    "Sensor",
                    sensor.getName(),
                    details,
                    "Present (waiting for reading)",
                    true));
        }
    }

    public static String sensorId(Sensor sensor) {
        return "sensor_" + sensor.getType() + "_" + sensor.getName();
    }

    public static String formatSensorReading(Sensor sensor, float[] values) {
        if (values == null || values.length == 0) {
            return "No reading";
        }
        StringBuilder sb = new StringBuilder("Reading: ");
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(String.format(Locale.US, "%.3f", values[i]));
        }
        switch (sensor.getType()) {
            case Sensor.TYPE_LIGHT:
                sb.append(" lx");
                break;
            case Sensor.TYPE_PROXIMITY:
                sb.append(" cm");
                break;
            case Sensor.TYPE_PRESSURE:
                sb.append(" hPa");
                break;
            case Sensor.TYPE_AMBIENT_TEMPERATURE:
                sb.append(" °C");
                break;
            case Sensor.TYPE_RELATIVE_HUMIDITY:
                sb.append(" %");
                break;
            default:
                break;
        }
        return sb.toString();
    }

    private static String sensorTypeName(Sensor sensor) {
        switch (sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                return "Accelerometer";
            case Sensor.TYPE_GYROSCOPE:
                return "Gyroscope";
            case Sensor.TYPE_MAGNETIC_FIELD:
                return "Magnetometer";
            case Sensor.TYPE_LIGHT:
                return "Light";
            case Sensor.TYPE_PROXIMITY:
                return "Proximity";
            case Sensor.TYPE_PRESSURE:
                return "Pressure";
            case Sensor.TYPE_GRAVITY:
                return "Gravity";
            case Sensor.TYPE_LINEAR_ACCELERATION:
                return "Linear acceleration";
            case Sensor.TYPE_ROTATION_VECTOR:
                return "Rotation vector";
            case Sensor.TYPE_RELATIVE_HUMIDITY:
                return "Humidity";
            case Sensor.TYPE_AMBIENT_TEMPERATURE:
                return "Ambient temperature";
            case Sensor.TYPE_STEP_COUNTER:
                return "Step counter";
            case Sensor.TYPE_HEART_RATE:
                return "Heart rate";
            default:
                return "Type " + sensor.getType();
        }
    }

    private static void addConnectivity(Context context, List<HardwareItem> items) {
        PackageManager pm = context.getPackageManager();

        LocationManager locationManager =
                (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean gpsFeature = pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
        boolean gpsOn = locationManager != null
                && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean networkOn = locationManager != null
                && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        items.add(new HardwareItem("gps", "Location", "GPS",
                "Feature: " + yesNo(gpsFeature)
                        + "\nGPS provider: " + (gpsOn ? "enabled" : "disabled")
                        + "\nNetwork location: " + (networkOn ? "enabled" : "disabled"),
                gpsOn ? "On" : (gpsFeature ? "Off" : "Not present"),
                gpsOn));

        BluetoothManager bluetoothManager =
                (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter adapter = bluetoothManager != null ? bluetoothManager.getAdapter() : null;
        boolean btFeature = pm.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH);
        boolean btOn = adapter != null && adapter.isEnabled();
        String btDetails = "Feature: " + yesNo(btFeature)
                + "\nAdapter: " + (adapter != null ? "present" : "missing")
                + "\nEnabled: " + yesNo(btOn);
        if (adapter != null && Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            try {
                btDetails += "\nName: " + safe(adapter.getName());
                btDetails += "\nAddress: " + safe(adapter.getAddress());
            } catch (SecurityException ignored) {
            }
        }
        items.add(new HardwareItem("bluetooth", "Connectivity", "Bluetooth",
                btDetails, btOn ? "On" : (adapter != null ? "Off" : "Not present"), btOn));

        WifiManager wifiManager = (WifiManager) context.getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
        boolean wifiFeature = pm.hasSystemFeature(PackageManager.FEATURE_WIFI);
        boolean wifiOn = wifiManager != null && wifiManager.isWifiEnabled();
        items.add(new HardwareItem("wifi", "Connectivity", "Wi-Fi",
                "Feature: " + yesNo(wifiFeature)
                        + "\nEnabled: " + yesNo(wifiOn),
                wifiOn ? "On" : (wifiFeature ? "Off" : "Not present"),
                wifiOn));

        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(context);
        boolean nfcFeature = pm.hasSystemFeature(PackageManager.FEATURE_NFC);
        boolean nfcOn = nfcAdapter != null && nfcAdapter.isEnabled();
        items.add(new HardwareItem("nfc", "Connectivity", "NFC",
                "Feature: " + yesNo(nfcFeature)
                        + "\nAdapter: " + (nfcAdapter != null ? "present" : "missing"),
                nfcOn ? "On" : (nfcAdapter != null ? "Off" : "Not present"),
                nfcOn));

        TelephonyManager telephony =
                (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        boolean telephonyFeature = pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
        String simState = simState(telephony);
        items.add(new HardwareItem("cellular", "Connectivity", "Cellular radio",
                "Feature: " + yesNo(telephonyFeature)
                        + "\nSIM state: " + simState
                        + "\nPhone type: " + phoneType(telephony),
                telephonyFeature ? simState : "Not present",
                telephonyFeature && telephony != null
                        && telephony.getSimState() == TelephonyManager.SIM_STATE_READY));
    }

    private static String simState(TelephonyManager telephony) {
        if (telephony == null) {
            return "Unknown";
        }
        switch (telephony.getSimState()) {
            case TelephonyManager.SIM_STATE_READY:
                return "SIM ready";
            case TelephonyManager.SIM_STATE_ABSENT:
                return "No SIM";
            case TelephonyManager.SIM_STATE_PIN_REQUIRED:
                return "PIN required";
            case TelephonyManager.SIM_STATE_PUK_REQUIRED:
                return "PUK required";
            case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
                return "Network locked";
            default:
                return "Unknown";
        }
    }

    private static String phoneType(TelephonyManager telephony) {
        if (telephony == null) {
            return "Unknown";
        }
        switch (telephony.getPhoneType()) {
            case TelephonyManager.PHONE_TYPE_GSM:
                return "GSM";
            case TelephonyManager.PHONE_TYPE_CDMA:
                return "CDMA";
            case TelephonyManager.PHONE_TYPE_SIP:
                return "SIP";
            case TelephonyManager.PHONE_TYPE_NONE:
                return "None";
            default:
                return "Unknown";
        }
    }

    private static void addPowerAndSystem(Context context, List<HardwareItem> items) {
        PackageManager pm = context.getPackageManager();
        Intent battery = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = battery != null ? battery.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : -1;
        int scale = battery != null ? battery.getIntExtra(BatteryManager.EXTRA_SCALE, -1) : -1;
        int status = battery != null ? battery.getIntExtra(BatteryManager.EXTRA_STATUS, -1) : -1;
        int plugged = battery != null ? battery.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) : 0;
        int pct = (level >= 0 && scale > 0) ? Math.round(level * 100f / scale) : -1;
        String charge = batteryStatus(status);
        String plug = plugType(plugged);
        items.add(new HardwareItem("battery", "Power", "Battery",
                "Level: " + (pct >= 0 ? pct + "%" : "Unknown")
                        + "\nStatus: " + charge
                        + "\nPower source: " + plug,
                pct >= 0 ? pct + "% · " + charge : charge,
                pct > 0));

        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        boolean interactive = powerManager != null && powerManager.isInteractive();
        boolean powerSave = powerManager != null && powerManager.isPowerSaveMode();
        items.add(new HardwareItem("power", "Power", "Power manager",
                "Screen interactive: " + yesNo(interactive)
                        + "\nBattery saver: " + yesNo(powerSave),
                interactive ? "Awake" : "Asleep",
                interactive));

        items.add(featureItem("touch", "Input", "Touchscreen",
                pm.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN)));
        items.add(featureItem("fingerprint", "Biometrics", "Fingerprint",
                pm.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)));
        boolean face = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                && pm.hasSystemFeature(PackageManager.FEATURE_FACE);
        items.add(featureItem("face", "Biometrics", "Face unlock hardware", face));
        items.add(featureItem("accelerometer_feat", "Input", "Accelerometer feature",
                pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER)));
        items.add(featureItem("gyro_feat", "Input", "Gyroscope feature",
                pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_GYROSCOPE)));
        items.add(featureItem("compass_feat", "Input", "Compass feature",
                pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_COMPASS)));
        items.add(featureItem("light_feat", "Input", "Light sensor feature",
                pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_LIGHT)));
        items.add(featureItem("proximity_feat", "Input", "Proximity sensor feature",
                pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_PROXIMITY)));
        items.add(featureItem("barometer_feat", "Input", "Barometer feature",
                pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_BAROMETER)));
        items.add(featureItem("step_feat", "Input", "Step counter feature",
                pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_COUNTER)));
        items.add(featureItem("vr", "Other", "VR / high-perf graphics",
                pm.hasSystemFeature(PackageManager.FEATURE_VR_MODE)
                        || pm.hasSystemFeature(PackageManager.FEATURE_OPENGLES_EXTENSION_PACK)));
        items.add(featureItem("usb_host", "Other", "USB host",
                pm.hasSystemFeature(PackageManager.FEATURE_USB_HOST)));
        items.add(featureItem("usb_accessory", "Other", "USB accessory",
                pm.hasSystemFeature(PackageManager.FEATURE_USB_ACCESSORY)));
    }

    private static HardwareItem featureItem(String id, String category, String name, boolean present) {
        return new HardwareItem(id, category, name,
                "System feature declared: " + yesNo(present),
                present ? "Present" : "Not present",
                present);
    }

    private static String batteryStatus(int status) {
        switch (status) {
            case BatteryManager.BATTERY_STATUS_CHARGING:
                return "Charging";
            case BatteryManager.BATTERY_STATUS_DISCHARGING:
                return "Discharging";
            case BatteryManager.BATTERY_STATUS_FULL:
                return "Full";
            case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                return "Not charging";
            default:
                return "Unknown";
        }
    }

    private static String plugType(int plugged) {
        switch (plugged) {
            case BatteryManager.BATTERY_PLUGGED_AC:
                return "AC";
            case BatteryManager.BATTERY_PLUGGED_USB:
                return "USB";
            case BatteryManager.BATTERY_PLUGGED_WIRELESS:
                return "Wireless";
            default:
                return "Unplugged";
        }
    }

    private static String yesNo(boolean value) {
        return value ? "yes" : "no";
    }

    private static String safe(String value) {
        return value == null || value.trim().isEmpty() ? "—" : value;
    }
}
