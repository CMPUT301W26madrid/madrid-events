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
/**
 * RecyclerView adapter for displaying in-app notifications and their available actions.
 *
 * <p>Role in application: presents {@link AppNotification} items to entrants, organizers,
 * or admins and exposes accept, decline, and delete actions for actionable notifications
 * such as invitations or co-organizer requests.</p>
 *
 * <p>Outstanding issues: delete is currently triggered through a long press only, which may
 * not be obvious to all users without supporting UI hints.</p>
 */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
    /**
     * Callback interface for user actions on notification items.
     */
    public interface OnNotificationActionListener {
        /**
         * Handles acceptance of an actionable notification.
         *
         * @param notification the accepted notification
         */
        void onAccept(AppNotification notification);
        /**
         * Handles rejection of an actionable notification.
         *
         * @param notification the declined notification
         */
        void onDecline(AppNotification notification);
        /**
         * Removes or dismisses a notification.
         *
         * @param notification the notification selected for deletion
         */
        void onDelete(AppNotification notification);
    }

    private List<AppNotification> notifications = new ArrayList<>();
    private OnNotificationActionListener listener;
    /**
     * Registers the listener used for notification actions.
     *
     * @param listener callback handler for accept, decline, and delete events
     */
    public void setActionListener(OnNotificationActionListener listener) {
        this.listener = listener;
    }
    /**
     * Replaces the current notification dataset.
     *
     * @param list the notifications to display
     */
    public void setNotifications(List<AppNotification> list) {
        this.notifications = new ArrayList<>(list);
        notifyDataSetChanged();
    }
    /**
     * Indicates whether any notifications are currently available.
     *
     * @return {@code true} if the adapter has no notifications
     */
    public boolean isEmpty() {
        return notifications.isEmpty();
    }
    /**
     * Inflates a single notification row.
     *
     * @param parent the parent RecyclerView
     * @param viewType the requested view type
     * @return a holder for one notification row
     */
    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(v);
    }
    /**
     * Binds notification content and action buttons to the supplied holder.
     *
     * @param h the holder receiving notification data
     * @param position the adapter position being displayed
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        AppNotification n = notifications.get(position);
        h.tvTitle.setText(n.getTitle());
        h.tvMessage.setText(n.getMessage());
        h.tvTime.setText(n.getRelativeTime());
        h.vUnread.setVisibility(n.isRead() ? View.GONE : View.VISIBLE);

        if (n.isActionRequired() || AppNotification.TYPE_CO_ORGANIZER.equals(n.getType())) {
            h.llActions.setVisibility(View.VISIBLE);
            h.btnAccept.setOnClickListener(v -> {
                if (listener != null) listener.onAccept(n);
            });
            h.btnDecline.setOnClickListener(v -> {
                if (listener != null) listener.onDecline(n);
            });
        } else {
            h.llActions.setVisibility(View.GONE);
        }

        h.itemView.setOnLongClickListener(v -> {
            if (listener != null) listener.onDelete(n);
            return true;
        });
    }
    /**
     * Returns the number of visible notifications.
     *
     * @return the current notification count
     */
    @Override public int getItemCount() { return notifications.size(); }
    /**
     * Holds view references for a single notification item.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvMessage, tvTime;
        View vUnread, llActions;
        View btnAccept, btnDecline;
        /**
         * Creates a holder for one notification item view.
         *
         * @param v the inflated item view
         */
        ViewHolder(View v) {
            super(v);
            tvTitle   = v.findViewById(R.id.tv_notif_title);
            tvMessage = v.findViewById(R.id.tv_notif_message);
            tvTime    = v.findViewById(R.id.tv_notif_time);
            vUnread   = v.findViewById(R.id.v_unread_dot);
            llActions = v.findViewById(R.id.ll_actions);
            btnAccept = v.findViewById(R.id.btn_accept);
            btnDecline = v.findViewById(R.id.btn_decline);
        }
    }
}
