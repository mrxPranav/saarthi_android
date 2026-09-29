package com.pranav.saarthi;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class HealthCheckWorker extends Worker {

    private static final String TAG = "HealthCheckWorker";
    private static final String HEALTH_URL = "https://saarthi-api-20ml.onrender.com/health/";
    private static final String REPORT_URL = "https://saarthi-api-20ml.onrender.com/health/checks";
    private static final int TIMEOUT_MS = 180000; // 3 minutes

    public HealthCheckWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Starting health check work");

        long startTime = System.currentTimeMillis();
        String requestTime = DateTimeFormatter.ISO_INSTANT.format(Instant.now());

        try {
            // 1. Perform GET request to wake up the service
            URL url = new URL(HEALTH_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);

            int responseCode = connection.getResponseCode();
            long endTime = System.currentTimeMillis();
            String responseTime = DateTimeFormatter.ISO_INSTANT.format(Instant.now());

            Log.d(TAG, "Health check response code: " + responseCode);

            if (responseCode == 200) {
                double difference = (endTime - startTime) / 1000.0;

                // 2. Report the result
                JSONObject json = new JSONObject();
                json.put("request_time", requestTime);
                json.put("response_time", responseTime);
                json.put("difference", difference);
                json.put("status", "success");

                sendReport(json.toString());
                // Schedule the next 10 AM / 10 PM checks
                HealthCheckScheduler.schedule(getApplication());
                return Result.success();
            } else {
                Log.e(TAG, "Health check failed with code: " + responseCode);
                return Result.retry();
            }

        } catch (Exception e) {
            Log.e(TAG, "Health check failed", e);
            return Result.retry();
        }
    }

    private void sendReport(String jsonPayload) throws Exception {
        URL url = new URL(REPORT_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setConnectTimeout(TIMEOUT_MS);
        connection.setReadTimeout(TIMEOUT_MS);
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonPayload.getBytes("UTF-8");
            os.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();
        Log.d(TAG, "Report response code: " + responseCode);

        if (responseCode < 200 || responseCode >= 300) {
            throw new Exception("Failed to send report. Code: " + responseCode);
        }
    }
}
