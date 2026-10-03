package com.pranav.saarthi.notifications;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.pranav.saarthi.R;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private List<NotificationModel> notifications;
    private final OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onNotificationClick(NotificationModel notification);
    }

    public NotificationAdapter(List<NotificationModel> notifications, OnNotificationClickListener listener) {
        this.notifications = notifications;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationModel notification = notifications.get(position);
        holder.tvPackage.setText(notification.getPackageName());
        holder.tvTitle.setText(notification.getTitle());
        holder.tvText.setText(notification.getText());
        holder.tvTime.setText(notification.getPostTime());

        if (notification.getBigText() != null && !notification.getBigText().isEmpty()) {
            holder.tvBigText.setText(notification.getBigText());
            holder.tvBigText.setVisibility(View.VISIBLE);
        } else {
            holder.tvBigText.setVisibility(View.GONE);
        }

        if (notification.getReply()) {
            holder.tvReply.setText("Replyable");
            holder.tvReply.setTextColor(ContextCompat.getColor(
                    holder.itemView.getContext(), android.R.color.holo_green_dark));
        } else {
            holder.tvReply.setText("Not Replyable");
            holder.tvReply.setTextColor(ContextCompat.getColor(
                    holder.itemView.getContext(), android.R.color.holo_red_dark));
        }
        holder.tvReply.setVisibility(View.VISIBLE);

        holder.itemView.setOnClickListener(v -> {
            if (notification.getReply() && listener != null) {
                listener.onNotificationClick(notification);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public void updateList(List<NotificationModel> newList) {
        this.notifications = newList;
        notifyDataSetChanged();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView tvPackage, tvTitle, tvText, tvBigText, tvTime, tvReply;

        NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPackage = itemView.findViewById(R.id.tvNotifPackage);
            tvTitle = itemView.findViewById(R.id.tvNotifTitle);
            tvText = itemView.findViewById(R.id.tvNotifText);
            tvBigText = itemView.findViewById(R.id.tvNotifBigText);
            tvTime = itemView.findViewById(R.id.tvNotifTime);
            tvReply = itemView.findViewById(R.id.tvNotifReply);
        }
    }
}
