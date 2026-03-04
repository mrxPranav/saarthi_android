package com.pranav.saarthi;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HealthCheckActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private HealthCheckAdapter adapter;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_check);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Server Health Stats");
        }

        recyclerView = findViewById(R.id.recyclerViewHealthChecks);
        progressBar = findViewById(R.id.progressBar);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HealthCheckAdapter();
        recyclerView.setAdapter(adapter);

        fetchHealthChecks();
    }

    private void fetchHealthChecks() {
        progressBar.setVisibility(View.VISIBLE);
        RetrofitClient.getClient().create(NotesApi.class).getHealthChecks().enqueue(new Callback<List<HealthCheck>>() {
            @Override
            public void onResponse(Call<List<HealthCheck>> call, Response<List<HealthCheck>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setHealthChecks(response.body());
                } else {
                    Toast.makeText(HealthCheckActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<HealthCheck>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(HealthCheckActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("HealthCheckActivity", "Error fetching health checks", t);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
