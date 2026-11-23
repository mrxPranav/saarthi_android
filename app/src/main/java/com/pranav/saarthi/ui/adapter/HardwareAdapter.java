package com.pranav.saarthi.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pranav.saarthi.R;
import com.pranav.saarthi.model.HardwareItem;

import java.util.ArrayList;
import java.util.List;

public class HardwareAdapter extends RecyclerView.Adapter<HardwareAdapter.ViewHolder> {

    private final List<HardwareItem> items = new ArrayList<>();

    public void setItems(List<HardwareItem> next) {
        items.clear();
        items.addAll(next);
        notifyDataSetChanged();
    }

    public void updateStatus(String id, String status, boolean available) {
        for (int i = 0; i < items.size(); i++) {
            HardwareItem item = items.get(i);
            if (item.getId().equals(id)) {
                item.setStatus(status);
                item.setAvailable(available);
                notifyItemChanged(i);
                return;
            }
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_hardware, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HardwareItem item = items.get(position);
        holder.tvCategory.setText(item.getCategory());
        holder.tvName.setText(item.getName());
        holder.tvDetails.setText(item.getDetails());
        holder.tvStatus.setText(item.getStatus());
        holder.tvStatus.setTextColor(item.isAvailable() ? 0xFF2E7D32 : 0xFFC62828);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategory;
        TextView tvName;
        TextView tvDetails;
        TextView tvStatus;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tvHardwareCategory);
            tvName = itemView.findViewById(R.id.tvHardwareName);
            tvDetails = itemView.findViewById(R.id.tvHardwareDetails);
            tvStatus = itemView.findViewById(R.id.tvHardwareStatus);
        }
    }
}
