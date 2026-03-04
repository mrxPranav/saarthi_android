package com.pranav.saarthi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BackupActivity extends AppCompatActivity {

    private static final String DB_NAME = "SaarthiDB";
    private GoogleSignInClient googleSignInClient;
    private Drive driveService;
    private TextView statusText;
    private ProgressBar progressBar;
    private View btnBackup, btnRestore, btnSignIn;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    handleSignInResult(result.getData());
                } else {
                    Toast.makeText(this, "Sign in cancelled or result not OK", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup);

        statusText = findViewById(R.id.statusText);
        progressBar = findViewById(R.id.progressBar);
        btnBackup = findViewById(R.id.btn_backup);
        btnRestore = findViewById(R.id.btn_restore);
        btnSignIn = findViewById(R.id.btn_google_sign_in);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        btnSignIn.setOnClickListener(v -> signInLauncher.launch(googleSignInClient.getSignInIntent()));
        btnBackup.setOnClickListener(v -> startBackup());
        btnRestore.setOnClickListener(v -> startRestore());

        checkLastSignIn();
    }

    private void checkLastSignIn() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            handleSignInResult(account);
        }
    }

    private void handleSignInResult(Intent data) {
        try {
            GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException.class);
            handleSignInResult(account);
        } catch (ApiException e) {
            String message = "Sign in failed (Code: " + e.getStatusCode() + ")";
            if (e.getStatusCode() == 10) {
                message += ". Likely SHA-1 mismatch or wrong package name in Google Console.";
            }
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            statusText.setText("Status: " + message);
        }
    }

    private void handleSignInResult(GoogleSignInAccount account) {
        statusText.setText("Status: Signed in as " + account.getEmail());
        btnSignIn.setVisibility(View.GONE);
        btnBackup.setEnabled(true);
        btnRestore.setEnabled(true);

        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                this, Collections.singleton(DriveScopes.DRIVE_FILE));
        credential.setSelectedAccount(account.getAccount());
        driveService = new Drive.Builder(
                AndroidHttp.newCompatibleTransport(),
                new GsonFactory(),
                credential)
                .setApplicationName("Saarthi")
                .build();
    }

    private void startBackup() {
        progressBar.setVisibility(View.VISIBLE);
        executor.execute(() -> {
            try {
                java.io.File dbFile = getDatabasePath(DB_NAME);
                java.io.File backupFile = new java.io.File(getExternalFilesDir(null), "SaarthiDB_backup.db");
                
                // 1. Local Backup
                copyFile(dbFile, backupFile);

                // 2. Drive Upload
                File fileMetadata = new File();
                fileMetadata.setName("SaarthiDB_backup.db");
                com.google.api.client.http.FileContent mediaContent = new com.google.api.client.http.FileContent("application/x-sqlite3", backupFile);
                
                driveService.files().create(fileMetadata, mediaContent)
                        .setFields("id")
                        .execute();

                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Backup successful!", Toast.LENGTH_LONG).show();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Backup failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void startRestore() {
        Toast.makeText(this, "Restore functionality requires selecting a specific file from Drive.", Toast.LENGTH_SHORT).show();
    }

    private void copyFile(java.io.File source, java.io.File dest) throws IOException {
        try (FileChannel sourceChannel = new FileInputStream(source).getChannel();
             FileChannel destChannel = new FileOutputStream(dest).getChannel()) {
            destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
        }
    }
}
