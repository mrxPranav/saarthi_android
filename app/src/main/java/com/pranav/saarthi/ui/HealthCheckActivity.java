package com.pranav.saarthi.ui;





import com.pranav.saarthi.R;
import com.pranav.saarthi.api.NotesApi;
import com.pranav.saarthi.api.RetrofitClient;
import com.pranav.saarthi.model.HealthCheck;
import com.pranav.saarthi.ui.adapter.HealthCheckAdapter;
import android.os.Bundle;

import android.util.Log;

import android.view.MenuItem;

import android.view.View;

import android.widget.AdapterView;

import android.widget.ArrayAdapter;

import android.widget.ProgressBar;

import android.widget.Spinner;

import android.widget.TextView;

import android.widget.Toast;



import androidx.annotation.NonNull;

import androidx.appcompat.app.AppCompatActivity;

import androidx.recyclerview.widget.LinearLayoutManager;

import androidx.recyclerview.widget.RecyclerView;



import java.time.Instant;

import java.time.YearMonth;

import java.time.ZoneId;

import java.time.format.DateTimeFormatter;

import java.util.ArrayList;

import java.util.Comparator;

import java.util.LinkedHashSet;

import java.util.List;

import java.util.Locale;

import java.util.Set;



import retrofit2.Call;

import retrofit2.Callback;

import retrofit2.Response;



public class HealthCheckActivity extends AppCompatActivity {



    private static final String TAG = "HealthCheckActivity";

    private static final int PAGE_SIZE = 500;



    private RecyclerView recyclerView;

    private HealthCheckAdapter adapter;

    private ProgressBar progressBar;

    private TextView tvHealthCheckCount;

    private Spinner spinnerMonth;

    private Spinner spinnerSort;



    private final List<HealthCheck> allHealthChecks = new ArrayList<>();

    private final List<YearMonth> monthFilters = new ArrayList<>();

    private boolean sortNewestFirst = true;

    private boolean suppressSpinnerCallbacks;



    private final NotesApi notesApi = RetrofitClient.getClient().create(NotesApi.class);

    private final DateTimeFormatter monthLabelFormatter =

            DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault());



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

        tvHealthCheckCount = findViewById(R.id.tvHealthCheckCount);

        spinnerMonth = findViewById(R.id.spinnerMonth);

        spinnerSort = findViewById(R.id.spinnerSort);



        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new HealthCheckAdapter();

        recyclerView.setAdapter(adapter);



        setupSortSpinner();

        fetchAllHealthChecks(0, new ArrayList<>());

    }



    private void setupSortSpinner() {

        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(

                this,

                android.R.layout.simple_spinner_item,

                new String[]{"Newest first", "Oldest first"});

        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerSort.setAdapter(sortAdapter);

        spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override

            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (suppressSpinnerCallbacks) {

                    return;

                }

                sortNewestFirst = position == 0;

                applyFiltersAndSort();

            }



            @Override

            public void onNothingSelected(AdapterView<?> parent) {

            }

        });

    }



    private void setupMonthSpinner() {

        Set<YearMonth> months = new LinkedHashSet<>();

        for (HealthCheck check : allHealthChecks) {

            YearMonth month = parseYearMonth(check.getRequestTime());

            if (month != null) {

                months.add(month);

            }

        }

        monthFilters.clear();

        monthFilters.addAll(months);

        monthFilters.sort(Comparator.reverseOrder());



        List<String> labels = new ArrayList<>();

        labels.add("All months");

        for (YearMonth month : monthFilters) {

            labels.add(monthLabelFormatter.format(month));

        }



        suppressSpinnerCallbacks = true;

        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(

                this,

                android.R.layout.simple_spinner_item,

                labels);

        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerMonth.setAdapter(monthAdapter);

        spinnerMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override

            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (suppressSpinnerCallbacks) {

                    return;

                }

                applyFiltersAndSort();

            }



            @Override

            public void onNothingSelected(AdapterView<?> parent) {

            }

        });

        suppressSpinnerCallbacks = false;

    }



    private void fetchAllHealthChecks(int skip, List<HealthCheck> accumulator) {

        progressBar.setVisibility(View.VISIBLE);

        notesApi.getHealthChecks(PAGE_SIZE, skip).enqueue(new Callback<List<HealthCheck>>() {

            @Override

            public void onResponse(@NonNull Call<List<HealthCheck>> call,

                                   @NonNull Response<List<HealthCheck>> response) {

                if (!response.isSuccessful() || response.body() == null) {

                    progressBar.setVisibility(View.GONE);

                    Toast.makeText(HealthCheckActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();

                    return;

                }



                List<HealthCheck> batch = response.body();

                accumulator.addAll(batch);

                if (batch.size() >= PAGE_SIZE) {

                    fetchAllHealthChecks(skip + batch.size(), accumulator);

                    return;

                }



                allHealthChecks.clear();

                allHealthChecks.addAll(accumulator);

                progressBar.setVisibility(View.GONE);

                setupMonthSpinner();

                applyFiltersAndSort();

                Log.d(TAG, "Loaded " + allHealthChecks.size() + " health checks");

            }



            @Override

            public void onFailure(@NonNull Call<List<HealthCheck>> call, @NonNull Throwable t) {

                progressBar.setVisibility(View.GONE);

                Toast.makeText(HealthCheckActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();

                Log.e(TAG, "Error fetching health checks", t);

            }

        });

    }



    private void applyFiltersAndSort() {

        List<HealthCheck> filtered = new ArrayList<>(allHealthChecks);



        int monthPosition = spinnerMonth.getSelectedItemPosition();

        if (monthPosition > 0 && monthPosition <= monthFilters.size()) {

            YearMonth selected = monthFilters.get(monthPosition - 1);

            List<HealthCheck> monthOnly = new ArrayList<>();

            for (HealthCheck check : filtered) {

                YearMonth month = parseYearMonth(check.getRequestTime());

                if (selected.equals(month)) {

                    monthOnly.add(check);

                }

            }

            filtered = monthOnly;

        }



        Comparator<HealthCheck> byRequestTime = Comparator.comparing(

                check -> parseInstant(check.getRequestTime()),

                Comparator.nullsLast(Comparator.naturalOrder()));

        if (sortNewestFirst) {

            filtered.sort(byRequestTime.reversed());

        } else {

            filtered.sort(byRequestTime);

        }



        adapter.setHealthChecks(filtered);

        tvHealthCheckCount.setText(getString(

                R.string.health_check_count_format,

                filtered.size(),

                allHealthChecks.size()));

    }



    private static Instant parseInstant(String requestTime) {

        if (requestTime == null || requestTime.isEmpty()) {

            return null;

        }

        try {

            return Instant.parse(requestTime);

        } catch (Exception e) {

            return null;

        }

    }



    private static YearMonth parseYearMonth(String requestTime) {

        Instant instant = parseInstant(requestTime);

        if (instant == null) {

            return null;

        }

        return YearMonth.from(instant.atZone(ZoneId.systemDefault()));

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


