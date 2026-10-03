package com.pranav.saarthi.notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pranav.saarthi.R;
import com.pranav.saarthi.data.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class NotificationTableActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NotificationRowAdapter adapter;
    private DatabaseHelper dbHelper;

    private Button btnPrev, btnNext, btnApplyFilters;
    private TextView tvPageInfo;
    private EditText filterId, filterPackage, filterTitle, filterText, filterTime;

    private int currentPage = 0;
    private static final int PAGE_SIZE = 50;
    private int totalCount = 0;

    private String currentFilterColumn = null;
    private String currentFilterValue = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_table);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Notification Data Grid");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        dbHelper = new DatabaseHelper(this);

        initViews();
        setupListeners();
        try {
            loadData();
        } catch (Exception e) {
            Toast.makeText(this, "Could not load notifications: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void initViews() {
        recyclerView = findViewById(R.id.rvNotificationTable);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationRowAdapter();
        recyclerView.setAdapter(adapter);

        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);
        btnApplyFilters = findViewById(R.id.btnApplyFilters);
        tvPageInfo = findViewById(R.id.tvPageInfo);

        filterId = findViewById(R.id.filterId);
        filterPackage = findViewById(R.id.filterPackage);
        filterTitle = findViewById(R.id.filterTitle);
        filterText = findViewById(R.id.filterText);
        filterTime = findViewById(R.id.filterTime);
    }

    private void setupListeners() {
        btnPrev.setOnClickListener(v -> {
            if (currentPage > 0) {
                currentPage--;
                loadData();
            }
        });

        btnNext.setOnClickListener(v -> {
            if ((currentPage + 1) * PAGE_SIZE < totalCount) {
                currentPage++;
                loadData();
            }
        });

        btnApplyFilters.setOnClickListener(v -> applyFilters());
    }

    private void applyFilters() {
        currentPage = 0;
        currentFilterColumn = null;
        currentFilterValue = null;

        if (!filterId.getText().toString().isEmpty()) {
            currentFilterColumn = DatabaseHelper.COLUMN_ID;
            currentFilterValue = filterId.getText().toString();
        } else if (!filterPackage.getText().toString().isEmpty()) {
            currentFilterColumn = DatabaseHelper.COLUMN_NOTIF_PACKAGE;
            currentFilterValue = filterPackage.getText().toString();
        } else if (!filterTitle.getText().toString().isEmpty()) {
            currentFilterColumn = DatabaseHelper.COLUMN_NOTIF_TITLE;
            currentFilterValue = filterTitle.getText().toString();
        } else if (!filterText.getText().toString().isEmpty()) {
            currentFilterColumn = DatabaseHelper.COLUMN_NOTIF_TEXT;
            currentFilterValue = filterText.getText().toString();
        } else if (!filterTime.getText().toString().isEmpty()) {
            currentFilterColumn = DatabaseHelper.COLUMN_NOTIF_TIME;
            currentFilterValue = filterTime.getText().toString();
        }

        loadData();
    }

    private void loadData() {
        new Thread(() -> {
            totalCount = dbHelper.getTotalNotificationCount(currentFilterColumn, currentFilterValue);
            List<NotificationModel> data = dbHelper.getNotificationsPaged(
                    PAGE_SIZE, currentPage * PAGE_SIZE, currentFilterColumn, currentFilterValue);

            runOnUiThread(() -> {
                adapter.setData(data);
                updatePaginationUI();
            });
        }).start();
    }

    private void updatePaginationUI() {
        int totalPages = (int) Math.ceil((double) totalCount / PAGE_SIZE);
        if (totalPages == 0) {
            totalPages = 1;
        }

        tvPageInfo.setText(String.format(
                "Page %d of %d (Total: %d)", currentPage + 1, totalPages, totalCount));

        btnPrev.setEnabled(currentPage > 0);
        btnNext.setEnabled((currentPage + 1) * PAGE_SIZE < totalCount);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private static class NotificationRowAdapter extends RecyclerView.Adapter<NotificationRowAdapter.RowViewHolder> {
        private final List<NotificationModel> notifications = new ArrayList<>();

        void setData(List<NotificationModel> newData) {
            notifications.clear();
            notifications.addAll(newData);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public RowViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_notification_row, parent, false);
            return new RowViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RowViewHolder holder, int position) {
            NotificationModel model = notifications.get(position);
            holder.tvId.setText(String.valueOf(model.getId()));
            holder.tvPackage.setText(model.getPackageName());
            holder.tvTitle.setText(model.getTitle());
            holder.tvText.setText(model.getText());
            holder.tvBigText.setText(model.getBigText());
            holder.tvTime.setText(model.getPostTime());
            holder.tvReplyStatus.setText(model.getReply() ? "Yes" : "No");
            holder.itemView.setBackgroundColor(position % 2 == 0 ? 0xFFFFFFFF : 0xFFF5F5F5);
        }

        @Override
        public int getItemCount() {
            return notifications.size();
        }

        static class RowViewHolder extends RecyclerView.ViewHolder {
            TextView tvId, tvPackage, tvTitle, tvText, tvBigText, tvTime, tvReplyStatus;

            RowViewHolder(View v) {
                super(v);
                tvId = v.findViewById(R.id.tvId);
                tvPackage = v.findViewById(R.id.tvPackage);
                tvTitle = v.findViewById(R.id.tvTitle);
                tvText = v.findViewById(R.id.tvText);
                tvBigText = v.findViewById(R.id.tvBigText);
                tvTime = v.findViewById(R.id.tvTime);
                tvReplyStatus = v.findViewById(R.id.tvReplyStatus);
            }
        }
    }
}
