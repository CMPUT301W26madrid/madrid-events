package com.example.eventlottery.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.eventlottery.R;
import com.example.eventlottery.models.AppNotification;
import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    public interface OnNotifClickListener {
        void onClick(AppNotification notif);
    }

    private List<AppNotification> notifications = new ArrayList<>();
    private final OnNotifClickListener listener;

    public NotificationAdapter(OnNotifClickListener listener) {
        this.listener = listener;
    }

    public void setNotifications(List<AppNotification> list) {
        this.notifications = new ArrayList<>(list);
        notifyDataSetChanged();
    }

    public boolean isEmpty() { return notifications.isEmpty(); }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        AppNotification n = notifications.get(position);

        // Icon by type
        String icon;
        switch (n.getType()) {
            case AppNotification.TYPE_WIN:         icon = "🏆"; break;
            case AppNotification.TYPE_LOSS:        icon = "❌"; break;
            case AppNotification.TYPE_INVITATION:  icon = "✉️"; break;
            case AppNotification.TYPE_CO_ORGANIZER:icon = "👥"; break;
            case AppNotification.TYPE_CANCELLED:   icon = "🚫"; break;
            default:                               icon = "📢"; break;
        }
        h.tvIcon.setText(icon);

        h.tvTitle.setText(n.getTitle());
        h.tvMessage.setText(n.getMessage());
        h.tvTime.setText(n.getRelativeTime());

        h.tvActionRequired.setVisibility(n.isActionRequired() ? View.VISIBLE : View.GONE);
        h.vUnread.setVisibility(n.isRead() ? View.GONE : View.VISIBLE);

        // Slightly dim background if read
        h.itemView.setAlpha(n.isRead() ? 0.75f : 1.0f);

        h.itemView.setOnClickListener(v -> listener.onClick(n));
    }

    @Override public int getItemCount() { return notifications.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvIcon, tvTitle, tvMessage, tvTime, tvActionRequired;
        View vUnread;

        ViewHolder(View v) {
            super(v);
            tvIcon          = v.findViewById(R.id.tv_icon);
            tvTitle         = v.findViewById(R.id.tv_title);
            tvMessage       = v.findViewById(R.id.tv_message);
            tvTime          = v.findViewById(R.id.tv_time);
            tvActionRequired = v.findViewById(R.id.tv_action_required);
            vUnread         = v.findViewById(R.id.v_unread);
        }
    }
}
