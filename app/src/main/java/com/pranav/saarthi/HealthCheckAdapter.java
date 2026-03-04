package com.pranav.saarthi;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;

public class HealthCheckAdapter extends RecyclerView.Adapter<HealthCheckAdapter.ViewHolder> {

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
        holder.tvRequestTime.setText("Request: " + item.getRequestTime().replace("T", " ").replace("Z", ""));
        holder.tvResponseTime.setText("Response: " + item.getResponseTime().replace("T", " ").replace("Z", ""));
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
