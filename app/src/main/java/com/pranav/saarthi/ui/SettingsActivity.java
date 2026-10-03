package com.pranav.saarthi.ui;


import com.pranav.saarthi.R;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private RadioGroup dbTypeGroup, themeGroup;
    private RadioButton rbSQLite, rbPostgreSQL, rbSystem, rbLight, rbDark;
    private View pgConfigLayout;
    private EditText etHost, etPort, etDbName, etUser, etPassword;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Settings");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        dbTypeGroup = findViewById(R.id.dbTypeGroup);
        rbSQLite = findViewById(R.id.rbSQLite);
        rbPostgreSQL = findViewById(R.id.rbPostgreSQL);

        themeGroup = findViewById(R.id.themeGroup);
        rbSystem = findViewById(R.id.rbSystem);
        rbLight = findViewById(R.id.rbLight);
        rbDark = findViewById(R.id.rbDark);

        pgConfigLayout = findViewById(R.id.pgConfigLayout);
        etHost = findViewById(R.id.etHost);
        etPort = findViewById(R.id.etPort);
        etDbName = findViewById(R.id.etDbName);
        etUser = findViewById(R.id.etUser);
        etPassword = findViewById(R.id.etPassword);
        btnSave = findViewById(R.id.btnSaveSettings);

        // Load DB Prefs
        SharedPreferences dbPrefs = getSharedPreferences("DatabasePrefs", MODE_PRIVATE);
        String currentType = dbPrefs.getString("db_type", "sqlite");

        if ("postgres".equals(currentType)) {
            rbPostgreSQL.setChecked(true);
        } else {
            rbSQLite.setChecked(true);
        }

        // Load Theme Prefs
        SharedPreferences themePrefs = getSharedPreferences("ThemePrefs", MODE_PRIVATE);
        int currentTheme = themePrefs.getInt("theme_mode",
                androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        if (currentTheme == androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES) {
            rbDark.setChecked(true);
        } else if (currentTheme == androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO) {
            rbLight.setChecked(true);
        } else {
            rbSystem.setChecked(true);
        }

        dbTypeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            // No config needed for Cloud API currently
            pgConfigLayout.setVisibility(View.GONE);
        });

        btnSave.setOnClickListener(v -> saveSettings());
    }

    private void saveSettings() {
        // Save DB Prefs
        SharedPreferences.Editor dbEditor = getSharedPreferences("DatabasePrefs", MODE_PRIVATE).edit();
        if (rbPostgreSQL.isChecked()) {
            dbEditor.putString("db_type", "postgres");
        } else {
            dbEditor.putString("db_type", "sqlite");
        }
        dbEditor.apply();

        // Save Theme Prefs
        int themeMode = androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        if (rbDark.isChecked()) {
            themeMode = androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;
        } else if (rbLight.isChecked()) {
            themeMode = androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
        }

        SharedPreferences.Editor themeEditor = getSharedPreferences("ThemePrefs", MODE_PRIVATE).edit();
        themeEditor.putInt("theme_mode", themeMode);
        themeEditor.apply();

        // Apply theme
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(themeMode);

        Toast.makeText(this, "Settings saved.", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
