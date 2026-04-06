package com.example.eventlottery.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.eventlottery.R;
import com.example.eventlottery.models.Registration;
import com.example.eventlottery.utils.DateUtils;
import java.util.ArrayList;
import java.util.List;
/**
 * RecyclerView adapter for the entrant's personal event history and registration status list.
 *
 * <p>Role in application: shows the events associated with the current user, including
 * registration status, join date, and a lightweight status filter for browsing past and
 * current event participation.</p>
 *
 * <p>Outstanding issues: filtering currently depends on fixed display strings, so changes to
 * filter labels must remain synchronized with the UI controls that invoke this adapter.</p>
 */
public class MyEventAdapter extends RecyclerView.Adapter<MyEventAdapter.ViewHolder> {
    /**
     * Callback invoked when a registration history item is selected.
     */
    public interface OnItemClickListener {
        /**
         * Opens details for the selected registration.
         *
         * @param registration the selected registration record
         */
        void onClick(Registration registration);
    }
    /**
     * Simple view model combining a registration with event metadata required by the list.
     */
    public static class RegistrationWithTitle {
        public final Registration registration;
        public final String eventTitle;
        public final long eventStartDate;
        /**
         * Creates a new combined registration and event summary object.
         *
         * @param r the registration record
         * @param title the related event title
         * @param date the event start date in milliseconds
         */
        public RegistrationWithTitle(Registration r, String title, long date) {
            this.registration   = r;
            this.eventTitle     = title;
            this.eventStartDate = date;
        }
    }

    private List<RegistrationWithTitle> allItems = new ArrayList<>();
    private List<RegistrationWithTitle> filteredItems = new ArrayList<>();
    private OnItemClickListener listener;
    /**
     * Registers the click listener for list rows.
     *
     * @param l listener used when an item is tapped
     */

    public void setListener(OnItemClickListener l) { this.listener = l; }
    /**
     * Replaces the registration history dataset and resets the filter.
     *
     * @param list the history items to display
     */
    public void setItems(List<RegistrationWithTitle> list) {
        this.allItems = new ArrayList<>(list);
        applyFilter("All Status");
    }
    /**
     * Applies the requested status filter to the event history list.
     *
     * @param statusFilter the selected status label
     */
    public void filter(String statusFilter) {
        applyFilter(statusFilter);
    }

    private void applyFilter(String statusFilter) {
        if ("All Status".equals(statusFilter)) {
            filteredItems = new ArrayList<>(allItems);
        } else {
            filteredItems = new ArrayList<>();
            for (RegistrationWithTitle item : allItems) {
                String regStatus = item.registration.getStatus();
                if (matchesFilter(regStatus, statusFilter)) {
                    filteredItems.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    private boolean matchesFilter(String regStatus, String filter) {
        if (regStatus == null) return false;
        switch (filter) {
            case "Invited": return Registration.STATUS_INVITED.equalsIgnoreCase(regStatus);
            case "Waiting": return Registration.STATUS_WAITING.equalsIgnoreCase(regStatus);
            case "Selected": return Registration.STATUS_SELECTED.equalsIgnoreCase(regStatus);
            case "Accepted": return Registration.STATUS_ACCEPTED.equalsIgnoreCase(regStatus);
            case "Declined": return Registration.STATUS_DECLINED.equalsIgnoreCase(regStatus);
            case "Cancelled": return Registration.STATUS_CANCELLED.equalsIgnoreCase(regStatus);
            default: return false;
        }
    }
    /**
     * Indicates whether the filtered history list is empty.
     *
     * @return {@code true} if no items match the current filter
     */
    public boolean isEmpty() { return filteredItems.isEmpty(); }
    /**
     * Inflates a row for the user's event history list.
     *
     * @param parent the parent RecyclerView
     * @param viewType the requested view type
     * @return a holder for one event history row
     */
    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_my_event, parent, false);
        return new ViewHolder(v);
    }
    /**
     * Binds one registration history item to the supplied row.
     *
     * @param h the holder receiving row data
     * @param position the adapter position being displayed
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        RegistrationWithTitle item = filteredItems.get(position);
        Registration r = item.registration;

        h.tvEventTitle.setText(item.eventTitle);
        h.tvEventDate.setText(DateUtils.formatDateShort(item.eventStartDate));
        h.tvJoinedDate.setText("Joined " + DateUtils.formatDateShort(r.getJoinedAt()));

        String icon;
        int badgeRes;
        switch (r.getStatus()) {
            case Registration.STATUS_INVITED:
                icon = "📩"; badgeRes = R.drawable.bg_badge_accent; break;
            case Registration.STATUS_WAITING:
                icon = "⏳"; badgeRes = R.drawable.bg_badge_accent; break;
            case Registration.STATUS_SELECTED:
                icon = "🎉"; badgeRes = R.drawable.bg_badge_green; break;
            case Registration.STATUS_ACCEPTED:
                icon = "✅"; badgeRes = R.drawable.bg_badge_green; break;
            case Registration.STATUS_DECLINED:
                icon = "❌"; badgeRes = R.drawable.bg_badge_red; break;
            case Registration.STATUS_CANCELLED:
                icon = "🚫"; badgeRes = R.drawable.bg_badge_grey; break;
            default:
                icon = "?"; badgeRes = R.drawable.bg_badge_grey;
        }
        h.tvStatusIcon.setText(icon);
        h.tvStatusBadge.setText(capitalize(r.getStatus()));
        h.tvStatusBadge.setBackgroundResource(badgeRes);

        h.itemView.setOnClickListener(v -> { if (listener != null) listener.onClick(r); });
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
    /**
     * Returns the number of filtered history items.
     *
     * @return the current visible item count
     */
    @Override public int getItemCount() { return filteredItems.size(); }
    /**
     * Holds view references for a single "My Events" row.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStatusIcon, tvEventTitle, tvEventDate, tvJoinedDate, tvStatusBadge;
        /**
         * Creates a holder for one event history item view.
         *
         * @param v the inflated item view
         */
        ViewHolder(View v) {
            super(v);
            tvStatusIcon  = v.findViewById(R.id.tv_status_icon);
            tvEventTitle  = v.findViewById(R.id.tv_event_title);
            tvEventDate   = v.findViewById(R.id.tv_event_date);
            tvJoinedDate  = v.findViewById(R.id.tv_joined_date);
            tvStatusBadge = v.findViewById(R.id.tv_status_badge);
        }
    }
}
