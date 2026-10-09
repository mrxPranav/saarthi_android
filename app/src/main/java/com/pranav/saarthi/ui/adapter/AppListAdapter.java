package com.pranav.saarthi.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pranav.saarthi.R;
import com.pranav.saarthi.model.InstalledAppInfo;

import java.util.Collections;
import java.util.List;

public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.ViewHolder> {

    private List<InstalledAppInfo> apps = Collections.emptyList();

    public void setApps(List<InstalledAppInfo> apps) {
        this.apps = apps;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_app_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        InstalledAppInfo app = apps.get(position);
        holder.tvAppName.setText(app.getLabel());
        holder.tvPackageName.setText(app.getPackageName());
        holder.ivIcon.setImageDrawable(app.getIcon());
        holder.viewActiveDot.setVisibility(app.isRecentlyActive() ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        return apps.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        View viewActiveDot;
        TextView tvAppName;
        TextView tvPackageName;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivAppIcon);
            viewActiveDot = itemView.findViewById(R.id.viewActiveDot);
            tvAppName = itemView.findViewById(R.id.tvAppName);
            tvPackageName = itemView.findViewById(R.id.tvAppPackage);
        }
    }
}
