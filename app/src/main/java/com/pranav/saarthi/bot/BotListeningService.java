package com.pranav.saarthi.bot;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.pranav.saarthi.R;
import com.pranav.saarthi.ui.BotActivity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BotListeningService extends Service {

    public static final String ACTION_START = "com.pranav.saarthi.bot.START";
    public static final String ACTION_STOP = "com.pranav.saarthi.bot.STOP";
    public static final String ACTION_STATE = "com.pranav.saarthi.bot.STATE";
    public static final String EXTRA_RUNNING = "running";
    public static final String EXTRA_FILE = "file";

    private static final String TAG = "BotListeningService";
    private static final String CHANNEL_ID = "bot_listening_channel";
    private static final int NOTIFICATION_ID = 2101;

    private static volatile boolean running;
    private static volatile String lastFilePath;

    private MediaRecorder recorder;
    private PowerManager.WakeLock wakeLock;
    private String currentFilePath;

    public static boolean isRunning() {
        return running;
    }

    public static String getLastFilePath() {
        return lastFilePath;
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, BotListeningService.class);
        intent.setAction(ACTION_START);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    public static void stop(Context context) {
        Intent intent = new Intent(context, BotListeningService.class);
        intent.setAction(ACTION_STOP);
        context.startService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent != null ? intent.getAction() : ACTION_START;
        if (ACTION_STOP.equals(action)) {
            stopListening();
            return START_NOT_STICKY;
        }

        if (!running) {
            Notification notification = buildNotification("Listening with microphone");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(NOTIFICATION_ID, notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE);
            } else {
                startForeground(NOTIFICATION_ID, notification);
            }
            if (!startRecording()) {
                stopListening();
                return START_NOT_STICKY;
            }
        } else {
            startForeground(NOTIFICATION_ID, buildNotification("Listening with microphone"));
        }
        return START_STICKY;
    }

    private boolean startRecording() {
        File dir = new File(getExternalFilesDir(null), "bot_recordings");
        if (!dir.exists() && !dir.mkdirs()) {
            Log.e(TAG, "Could not create recording directory");
            return false;
        }
        String name = "bot_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(new Date()) + ".m4a";
        File out = new File(dir, name);
        currentFilePath = out.getAbsolutePath();

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                recorder = new MediaRecorder(this);
            } else {
                recorder = new MediaRecorder();
            }
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            recorder.setAudioEncodingBitRate(128000);
            recorder.setAudioSamplingRate(44100);
            recorder.setOutputFile(currentFilePath);
            recorder.prepare();
            recorder.start();
            running = true;
            lastFilePath = currentFilePath;
            acquireWakeLock();
            broadcastState();
            Log.d(TAG, "Recording started: " + currentFilePath);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to start recording", e);
            releaseRecorder();
            return false;
        }
    }

    private void stopListening() {
        running = false;
        releaseRecorder();
        releaseWakeLock();
        lastFilePath = currentFilePath;
        broadcastState();
        stopForeground(true);
        stopSelf();
        Log.d(TAG, "Recording stopped");
    }

    private void releaseRecorder() {
        if (recorder == null) {
            return;
        }
        try {
            recorder.stop();
        } catch (Exception ignored) {
        }
        try {
            recorder.reset();
        } catch (Exception ignored) {
        }
        recorder.release();
        recorder = null;
    }

    private void acquireWakeLock() {
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        if (pm == null) {
            return;
        }
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "saarthi:bot_listen");
        wakeLock.setReferenceCounted(false);
        wakeLock.acquire();
    }

    private void releaseWakeLock() {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
        wakeLock = null;
    }

    private void broadcastState() {
        Intent intent = new Intent(ACTION_STATE);
        intent.setPackage(getPackageName());
        intent.putExtra(EXTRA_RUNNING, running);
        intent.putExtra(EXTRA_FILE, currentFilePath);
        sendBroadcast(intent);
    }

    private Notification buildNotification(String text) {
        Intent open = new Intent(this, BotActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(
                this, 0, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent stop = new Intent(this, BotListeningService.class);
        stop.setAction(ACTION_STOP);
        PendingIntent stopIntent = PendingIntent.getService(
                this, 1, stop,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.bot_title))
                .setContentText(text)
                .setSmallIcon(android.R.drawable.ic_btn_speak_now)
                .setOngoing(true)
                .setContentIntent(contentIntent)
                .addAction(android.R.drawable.ic_media_pause, getString(R.string.bot_stop), stopIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                getString(R.string.bot_channel_name),
                NotificationManager.IMPORTANCE_LOW);
        channel.setDescription(getString(R.string.bot_channel_desc));
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onDestroy() {
        if (running) {
            stopListening();
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
