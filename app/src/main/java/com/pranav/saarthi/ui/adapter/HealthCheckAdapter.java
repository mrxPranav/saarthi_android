package com.pranav.saarthi.ui.adapter;



import com.pranav.saarthi.R;
import com.pranav.saarthi.model.HealthCheck;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class HealthCheckAdapter extends RecyclerView.Adapter<HealthCheckAdapter.ViewHolder> {

    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm:ss", Locale.getDefault())
                    .withZone(ZoneId.systemDefault());

    private List<HealthCheck> healthChecks = Collections.emptyList();

    public void setHealthChecks(List<HealthCheck> healthChecks) {
        this.healthChecks = healthChecks;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_health_check, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HealthCheck item = healthChecks.get(position);

        holder.tvStatus.setText(item.getStatus());
        if ("success".equalsIgnoreCase(item.getStatus())) {
            holder.tvStatus.setTextColor(Color.parseColor("#4CAF50"));
        } else {
            holder.tvStatus.setTextColor(Color.RED);
        }

        holder.tvDifference.setText(String.format("%.2f s", item.getDifference()));
        holder.tvRequestTime.setText("Request: " + formatForDisplay(item.getRequestTime()));
        holder.tvResponseTime.setText("Response: " + formatForDisplay(item.getResponseTime()));
    }

    private static String formatForDisplay(String isoTimestamp) {
        if (isoTimestamp == null || isoTimestamp.isEmpty()) {
            return "";
        }
        try {
            return DISPLAY_FORMAT.format(Instant.parse(isoTimestamp));
        } catch (Exception e) {
            return isoTimestamp.replace("T", " ").replace("Z", "");
        }
    }

    @Override
    public int getItemCount() {
        return healthChecks.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStatus, tvDifference, tvRequestTime, tvResponseTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvDifference = itemView.findViewById(R.id.tvDifference);
            tvRequestTime = itemView.findViewById(R.id.tvRequestTime);
            tvResponseTime = itemView.findViewById(R.id.tvResponseTime);
        }
    }
}
