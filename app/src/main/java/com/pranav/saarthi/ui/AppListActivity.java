package com.pranav.saarthi.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pranav.saarthi.R;
import com.pranav.saarthi.apps.AppListLoader;
import com.pranav.saarthi.model.InstalledAppInfo;
import com.pranav.saarthi.ui.adapter.AppListAdapter;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppListActivity extends AppCompatActivity {

    private AppListAdapter adapter;
    private TextView tvAppCount;
    private ProgressBar progressBar;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_list);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.app_list_title);
        }

        tvAppCount = findViewById(R.id.tvAppCount);
        progressBar = findViewById(R.id.progressBarAppList);
        RecyclerView recyclerView = findViewById(R.id.rvAppList);
        Button btnRefresh = findViewById(R.id.btnRefreshAppList);

        adapter = new AppListAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        btnRefresh.setOnClickListener(v -> refreshAppList());

        if (!AppListLoader.hasUsageAccess(this)) {
            showUsageAccessDialog();
        }
        refreshAppList();
    }

    private void refreshAppList() {
        progressBar.setVisibility(View.VISIBLE);
        executor.execute(() -> {
            List<InstalledAppInfo> apps = AppListLoader.load(this);
            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                adapter.setApps(apps);
                tvAppCount.setText(getString(R.string.app_list_count, apps.size()));
            });
        });
    }

    private void showUsageAccessDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.usage_access_title)
                .setMessage(R.string.usage_access_message)
                .setPositiveButton(R.string.open_settings, (d, w) ->
                        startActivity(AppListLoader.usageAccessSettingsIntent()))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (AppListLoader.hasUsageAccess(this)) {
            refreshAppList();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
